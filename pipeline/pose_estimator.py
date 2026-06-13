"""
FitScan - Stage 2: Body Pose Estimation (MediaPipe Pose Landmarker)
Extracts 33 body landmarks, focuses on the primary measuring landmarks,
converts from normalized coordinates to pixel/absolute values, and validates visibility.
"""

import os
import logging
from typing import Dict, Any, Tuple, List, Optional
import cv2
import numpy as np
import mediapipe as mp

logger = logging.getLogger("fitscan.pose")

# Standard MediaPipe Key Landmarks indices mapped to readable names
KEY_LANDMARK_MAP = {
    0: "NOSE",
    11: "LEFT_SHOULDER",
    12: "RIGHT_SHOULDER",
    13: "LEFT_ELBOW",
    14: "RIGHT_ELBOW",
    15: "LEFT_WRIST",
    16: "RIGHT_WRIST",
    23: "LEFT_HIP",
    24: "RIGHT_HIP",
    25: "LEFT_KNEE",
    26: "RIGHT_KNEE",
    27: "LEFT_ANKLE",
    28: "RIGHT_ANKLE"
}

class PoseEstimationError(Exception):
    """Exception raised when pose estimation fails or landmark visibility is too low."""
    pass

class PoseEstimator:
    def __init__(self, model_path: Optional[str] = None):
        """
        Initializes the MediaPipe Pose Estimator.
        Supports both modern Tasks API (.task file) and legacy Solutions as a safe fallback.
        """
        self.model_path = model_path or "models/pose_landmarker_full.task"
        self.use_tasks_api = True

        # Check if the .task file exists. If not, we will rely on mp.solutions.pose fallback
        if not os.path.exists(self.model_path):
            logger.warning(
                f"MediaPipe task file '{self.model_path}' not found. "
                f"Falling back to legacy MediaPipe Solutions SDK."
            )
            self.use_tasks_api = False

        if self.use_tasks_api:
            try:
                from mediapipe.tasks import python
                from mediapipe.tasks.python import vision
                
                base_options = python.BaseOptions(model_asset_path=self.model_path)
                options = vision.PoseLandmarkerOptions(
                    base_options=base_options,
                    running_mode=vision.RunningMode.IMAGE,
                    num_poses=1
                )
                self.landmarker = vision.PoseLandmarker.create_from_options(options)
                logger.info(f"Loaded modern MediaPipe Pose Landmarker from {self.model_path}")
            except Exception as e:
                logger.error(f"Failed to load modern Tasks API: {e}. Falling back to legacy Solutions API.")
                self.use_tasks_api = False

        if not self.use_tasks_api:
            # Set up legacy mp.solutions
            self.mp_pose = mp.solutions.pose
            self.pose_solution = self.mp_pose.Pose(
                static_image_mode=True,
                model_complexity=2, # Heavy (Full) layout model equivalent
                min_detection_confidence=0.5,
                min_tracking_confidence=0.5
            )
            logger.info("Loaded legacy MediaPipe Solutions Pose estimator.")

    def estimate_pose(self, cropped_image: np.ndarray, bbox_offset: Tuple[int, int] = (0, 0)) -> Dict[str, Dict[str, Any]]:
        """
        Estimates the core 33 body pose landmarks.
        
        Args:
            cropped_image: Cropped BGR OpenCV image of the person from Stage 1.
            bbox_offset: (x_offset, y_offset) containing the upper left corner of the crop box
                         on the original full-frame coordinate system.

        Returns:
            Dict of landmark_name -> {
                'x_px_crop': float, (relative to cropped image in pixels)
                'y_px_crop': float, (relative to cropped image in pixels)
                'x_px_orig': float, (mapped back to original screen coordinates)
                'y_px_orig': float, (mapped back to original screen coordinates)
                'z': float,         (depth relative to hip)
                'visibility': float
            }
        """
        if cropped_image is None or cropped_image.size == 0:
            raise ValueError("Empty input image passed to Pose Estimator.")

        h, w = cropped_image.shape[:2]
        rgb_image = cv2.cvtColor(cropped_image, cv2.COLOR_BGR2RGB)
        landmarks_list = []

        if self.use_tasks_api:
            mp_image = mp.Image(image_format=mp.ImageFormat.SRGB, data=rgb_image)
            result = self.landmarker.detect(mp_image)
            
            if result and result.pose_landmarks and len(result.pose_landmarks) > 0:
                landmarks_list = result.pose_landmarks[0]
        else:
            result = self.pose_solution.process(rgb_image)
            if result and result.pose_landmarks:
                landmarks_list = result.pose_landmarks.landmark

        if not landmarks_list:
            raise PoseEstimationError(
                "Failed to detect body landmarks. Ensure your full body is visible with good lighting."
            )

        # Parse and translate landmarks
        parsed_landmarks = {}
        for idx, name in KEY_LANDMARK_MAP.items():
            if idx < len(landmarks_list):
                lm = landmarks_list[idx]
                
                # Convert normalized [0, 1] coords to crop-relative pixels
                x_px_crop = lm.x * w
                y_px_crop = lm.y * h
                
                # Translate back to the original image's coordinates using bounding box offset
                x_px_orig = x_px_crop + bbox_offset[0]
                y_px_orig = y_px_crop + bbox_offset[1]
                
                parsed_landmarks[name] = {
                    "x_px_crop": float(x_px_crop),
                    "y_px_crop": float(y_px_crop),
                    "x_px_orig": float(x_px_orig),
                    "y_px_orig": float(y_px_orig),
                    "z": float(lm.z),
                    "visibility": float(lm.visibility if hasattr(lm, "visibility") else 1.0)
                }

        return parsed_landmarks

    def validate_landmarks(self, landmarks: Dict[str, Dict[str, Any]], min_visibility: float = 0.6) -> Tuple[bool, Optional[str]]:
        """
        Validates whether key body parts are adequately visible.
        
        Args:
            landmarks: Dictionary of key landmarks returned from estimate_pose.
            min_visibility: Minimum threshold.

        Returns:
            Tuple: (is_valid, error_reason)
        """
        # We enforce strict visibility checks for crucial body sizing points.
        # If any of these are missing or below 0.6 visibility, sizing will be inaccurate.
        critical_groups = {
            "shoulders": ["LEFT_SHOULDER", "RIGHT_SHOULDER"],
            "hips": ["LEFT_HIP", "RIGHT_HIP"],
            "arms": ["LEFT_ELBOW", "LEFT_WRIST", "RIGHT_ELBOW", "RIGHT_WRIST"],
            "legs/knees/ankles": ["LEFT_KNEE", "LEFT_ANKLE", "RIGHT_KNEE", "RIGHT_ANKLE"]
        }

        failed_points = []
        for group_name, pts in critical_groups.items():
            for pt in pts:
                if pt not in landmarks:
                    failed_points.append(pt)
                else:
                    vis = landmarks[pt]["visibility"]
                    if vis < min_visibility:
                        failed_points.append(f"{pt} (vis={vis:.2f})")

        if failed_points:
            failed_str = ", ".join(failed_points[:3])
            if len(failed_points) > 3:
                failed_str += f" and {len(failed_points) - 3} more"
            
            error_msg = (
                f"Ensure good lighting and face the camera directly. "
                f"The following landmarks are blocked or poorly lit: {failed_str}."
            )
            return False, error_msg

        return True, None
