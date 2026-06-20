"""
FitScan - Stage 1: Person Detection (YOLOv8n)
This module detects the person in the input frame, filters for class 0 (person),
and crops the bound box region for subsequent pose estimation.
"""

import os
import logging
from typing import Tuple, Optional, Dict, Any
import numpy as np
import cv2
from ultralytics import YOLO

logger = logging.getLogger("fitscan.detector")

class PersonDetectionError(Exception):
    """Exception raised when person detection fails."""
    pass

class PersonDetector:
    def __init__(self, model_path: Optional[str] = None, conf_threshold: float = 0.6):
        """
        Initializes the YOLOv8 person detector.
        
        Args:
            model_path: Path to the YOLOv8n model (.pt or .onnx). 
                        If None, looks in models/ directory or retrieves yolov8n.pt.
            conf_threshold: Minimum confidence for a detection to be considered valid.
        """
        self.conf_threshold = conf_threshold
        
        # Determine best available model path
        if model_path is None:
            possible_paths = [
                "models/yolov8n.onnx",
                "models/yolov8n.pt",
                "yolov8n.onnx",
                "yolov8n.pt"
            ]
            for path in possible_paths:
                if os.path.exists(path):
                    model_path = path
                    break
            if model_path is None:
                # Default fallback which lets ultralytics download it automatically
                model_path = "models/yolov8n.pt"
                os.makedirs("models", exist_ok=True)
                logger.info(f"No existing local YOLOv8 weights found. Defaulting to: {model_path}")

        logger.info(f"Loading YOLOv8 detector from: {model_path}")
        try:
            self.model = YOLO(model_path)
        except Exception as e:
            logger.error(f"Failed to load YOLO model: {e}. Falling back to standard 'yolov8n.pt'")
            self.model = YOLO("yolov8n.pt")

    def detect_and_crop(self, image: np.ndarray) -> Tuple[np.ndarray, Dict[str, Any]]:
        """
        Detects a person in the frame and returns the cropped bounding box.
        If multiple persons are detected, returns the one with the largest bounding box area.

        Args:
            image: OpenCV image (numpy array, BGR format).

        Returns:
            Tuple containing:
                - numpy array: Cropped image of the detected person
                - dict: Information containing:
                    - 'bbox': [x1, y1, x2, y2] absolute pixel coordinates
                    - 'confidence': float (0.0 to 1.0)
                    - 'original_shape': (height, width, channels)
                    
        Raises:
            PersonDetectionError: If no person is detected or confidence is below threshold.
        """
        if image is None or image.size == 0:
            raise ValueError("Input image is empty or invalid.")

        h, w = image.shape[:2]
        
        # Run inference (specifically filtering for class 0 = 'person' in COCO)
        results = self.model.predict(
            source=image,
            conf=self.conf_threshold,
            classes=[0],  # Class 0 is person
            verbose=False
        )

        if not results or len(results[0].boxes) == 0:
            raise PersonDetectionError("No person found in frame. Please step back so your full body is visible.")

        best_box = None
        max_area = -1
        best_conf = 0.0

        for box in results[0].boxes:
            # Get box coordinates [x1, y1, x2, y2]
            xyxy = box.xyxy[0].cpu().numpy()
            conf = float(box.conf[0].cpu().item())
            
            # Area-based selection of primary subject to avoid background interferences
            x1, y1, x2, y2 = map(int, xyxy)
            area = (x2 - x1) * (y2 - y1)
            
            if area > max_area:
                max_area = area
                best_box = (x1, y1, x2, y2)
                best_conf = conf

        if best_box is None:
            raise PersonDetectionError("No person found in frame. Please step back so your full body is visible.")

        x1, y1, x2, y2 = best_box
        
        # Padding crop area slightly to preserve body contours and context details
        pad_x = int((x2 - x1) * 0.05)
        pad_y = int((y2 - y1) * 0.05)
        
        crop_x1 = max(0, x1 - pad_x)
        crop_y1 = max(0, y1 - pad_y)
        crop_x2 = min(w, x2 + pad_x)
        crop_y2 = min(h, y2 + pad_y)

        cropped_img = image[crop_y1:crop_y2, crop_x1:crop_x2]
        
        metadata = {
            "bbox": [crop_x1, crop_y1, crop_x2, crop_y2],
            "confidence": best_conf,
            "original_shape": (h, w, image.shape[2] if len(image.shape) > 2 else 1)
        }

        logger.info(f"Person detected with confidence {best_conf:.2f}. Bounding box: {metadata['bbox']}")
        return cropped_img, metadata
