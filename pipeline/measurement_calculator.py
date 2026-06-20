"""
FitScan - Stage 3: Measurement Calculation Engine
Provides 2D/3D Euclidean distance functions, midpoints, body scale calibration 
(via user height or reference object dimensions), and calculates precise metrics using NumPy.
"""

import logging
from typing import Dict, Any, Tuple, Optional
import numpy as np

logger = logging.getLogger("fitscan.measure")

# --- SIZING CALIBRATION CONFIGURATION CONSTANTS ---
CHEST_CIRCUMFERENCE_MULTIPLIER = 2.15
WAIST_CIRCUMFERENCE_MULTIPLIER = 1.85
HIP_CIRCUMFERENCE_MULTIPLIER = 2.40

# Reference width values in centimeters for physical objects
REFERENCE_CATALOG = {
    "a4": 21.00,          # Width of short side of A4 paper in cm
    "credit_card": 8.56,  # Standard width of ISO/IEC 7810 ID-1 card in cm
}

def euclidean_distance_2d(p1: Tuple[float, float], p2: Tuple[float, float]) -> float:
    """Calculates 2D Euclidean distance on the image/sensor coordinate plane using NumPy."""
    v1 = np.array(p1)
    v2 = np.array(p2)
    return float(np.linalg.norm(v1 - v2))

def euclidean_distance_3d(p1: Tuple[float, float, float], p2: Tuple[float, float, float]) -> float:
    """Calculates 3D Euclidean distance incorporating depth/z estimation if desired using NumPy."""
    v1 = np.array(p1)
    v2 = np.array(p2)
    return float(np.linalg.norm(v1 - v2))

def midpoint_2d(p1: Tuple[float, float], p2: Tuple[float, float]) -> Tuple[float, float]:
    """Retrieves the exact spatial midpoint between two points using NumPy."""
    v1 = np.array(p1)
    v2 = np.array(p2)
    mid = (v1 + v2) / 2.0
    return float(mid[0]), float(mid[1])

class MeasurementCalculator:
    @staticmethod
    def calculate_scale_factor(
        landmarks: Dict[str, Dict[str, Any]], 
        user_height_cm: Optional[float] = None,
        ref_object_type: Optional[str] = None,
        ref_object_pixels: Optional[float] = None
    ) -> Tuple[float, str]:
        """
        Calculates the real-world scale factor (cm per pixel).
        Can calibrate dynamically from reference objects (A4, credit card) or static height inputs.
        
        Returns:
            Tuple containing:
                - float: Scale factor representing cm per pixel
                - str: Description of the calibration option applied
        """
        # Option A: Calibration using visual Reference Object
        if ref_object_pixels is not None and ref_object_pixels > 0:
            ref_type = (ref_object_type or "credit_card").lower()
            ref_cm = REFERENCE_CATALOG.get(ref_type, 8.56)  # Default standard credit card
            scale = ref_cm / ref_object_pixels
            return scale, f"Reference Object Calibration ({ref_type}: {ref_cm}cm = {ref_object_pixels:.1f}px)"

        # Option B: Calibration using User Height (Fallback method)
        if user_height_cm is None or user_height_cm <= 0:
            # Absolute default fallback height (e.g., 170cm) if nothing is supplied
            user_height_cm = 170.0
            logger.warning("No height or reference object provided. Defaulting height to 170.0 cm.")

        # Extract landmarks to find pixel body height
        nose_pt = (landmarks["NOSE"]["x_px_crop"], landmarks["NOSE"]["y_px_crop"])
        l_ankle = (landmarks["LEFT_ANKLE"]["x_px_crop"], landmarks["LEFT_ANKLE"]["y_px_crop"])
        r_ankle = (landmarks["RIGHT_ANKLE"]["x_px_crop"], landmarks["RIGHT_ANKLE"]["y_px_crop"])

        ankles_midpoint = midpoint_2d(l_ankle, r_ankle)
        
        # Calculate pixel distance from nose to the floor (ankles)
        nose_to_ankle_pixels = euclidean_distance_2d(nose_pt, ankles_midpoint)
        
        if nose_to_ankle_pixels <= 0:
            raise ValueError("Calculated pixel body height is zero or invalid.")
            
        # Anatomical correction: Nose to ankle is ~90% of total height
        actual_pixel_height = nose_to_ankle_pixels / 0.90
        
        scale = user_height_cm / actual_pixel_height
        
        return scale, f"User Height Calibration ({user_height_cm}cm = {actual_pixel_height:.1f}px)"

    @classmethod
    def calculate_measurements(
        cls, 
        landmarks: Dict[str, Dict[str, Any]], 
        user_height_cm: Optional[float] = None,
        ref_object_type: Optional[str] = None,
        ref_object_pixels: Optional[float] = None
    ) -> Dict[str, Any]:
        """
        Calculates anatomical metrics based on parsed landmarks. All sizes returned are in cm.
        """
        # Compute scaling metrics
        scale, calibration_method = cls.calculate_scale_factor(
            landmarks=landmarks, 
            user_height_cm=user_height_cm,
            ref_object_type=ref_object_type,
            ref_object_pixels=ref_object_pixels
        )

        # Coordinate maps for ease of access
        l_shoulder = (landmarks["LEFT_SHOULDER"]["x_px_crop"], landmarks["LEFT_SHOULDER"]["y_px_crop"])
        r_shoulder = (landmarks["RIGHT_SHOULDER"]["x_px_crop"], landmarks["RIGHT_SHOULDER"]["y_px_crop"])
        
        l_hip = (landmarks["LEFT_HIP"]["x_px_crop"], landmarks["LEFT_HIP"]["y_px_crop"])
        r_hip = (landmarks["RIGHT_HIP"]["x_px_crop"], landmarks["RIGHT_HIP"]["y_px_crop"])

        l_elbow = (landmarks["LEFT_ELBOW"]["x_px_crop"], landmarks["LEFT_ELBOW"]["y_px_crop"])
        l_wrist = (landmarks["LEFT_WRIST"]["x_px_crop"], landmarks["LEFT_WRIST"]["y_px_crop"])
        
        r_elbow = (landmarks["RIGHT_ELBOW"]["x_px_crop"], landmarks["RIGHT_ELBOW"]["y_px_crop"])
        r_wrist = (landmarks["RIGHT_WRIST"]["x_px_crop"], landmarks["RIGHT_WRIST"]["y_px_crop"])

        l_knee = (landmarks["LEFT_KNEE"]["x_px_crop"], landmarks["LEFT_KNEE"]["y_px_crop"])
        l_ankle = (landmarks["LEFT_ANKLE"]["x_px_crop"], landmarks["LEFT_ANKLE"]["y_px_crop"])
        
        r_knee = (landmarks["RIGHT_KNEE"]["x_px_crop"], landmarks["RIGHT_KNEE"]["y_px_crop"])
        r_ankle = (landmarks["RIGHT_ANKLE"]["x_px_crop"], landmarks["RIGHT_ANKLE"]["y_px_crop"])

        # 1. Shoulder Width = distance(L_SHOULDER, R_SHOULDER) * scale
        px_shoulder_width = euclidean_distance_2d(l_shoulder, r_shoulder)
        shoulder_width = px_shoulder_width * scale

        # 2. Torso Height = distance(midpoint(SHOULDERS), midpoint(HIPS)) * scale
        shoulders_mid = midpoint_2d(l_shoulder, r_shoulder)
        hips_mid = midpoint_2d(l_hip, r_hip)
        px_torso_height = euclidean_distance_2d(shoulders_mid, hips_mid)
        torso_height = px_torso_height * scale

        # 3. Hip Width = distance(L_HIP, R_HIP) * scale
        px_hip_width = euclidean_distance_2d(l_hip, r_hip)
        hip_width = px_hip_width * scale

        # 4. Arm Length = (distance(L_SHOULDER, L_ELBOW) + distance(L_ELBOW, L_WRIST)) * scale
        px_l_arm = euclidean_distance_2d(l_shoulder, l_elbow) + euclidean_distance_2d(l_elbow, l_wrist)
        px_r_arm = euclidean_distance_2d(r_shoulder, r_elbow) + euclidean_distance_2d(r_elbow, r_wrist)
        arm_length = ((px_l_arm + px_r_arm) / 2.0) * scale

        # 5. Circumference Estimates
        chest_circumference = shoulder_width * CHEST_CIRCUMFERENCE_MULTIPLIER
        
        # Adjustable waist modifier based on the waist-to-hip ratio which handles visual silhouettes
        waist_circ_mod = WAIST_CIRCUMFERENCE_MULTIPLIER
        hip_to_shoulder_ratio = hip_width / (shoulder_width + 1e-5)
        if hip_to_shoulder_ratio > 0.95:
            # Pear/Hourglass shape configuration expansion
            waist_circ_mod += 0.05
        elif hip_to_shoulder_ratio < 0.82:
            # Athletic V-shape/Inverted triangle shape configuration
            waist_circ_mod -= 0.05
            
        waist_circumference = hip_width * waist_circ_mod
        hip_circumference = hip_width * HIP_CIRCUMFERENCE_MULTIPLIER

        # 6. Inseam: knee-to-ankle distance * scale * 2 (averaged for legs)
        px_l_leg = euclidean_distance_2d(l_knee, l_ankle)
        px_r_leg = euclidean_distance_2d(r_knee, r_ankle)
        inseam_cm = ((px_l_leg + px_r_leg) / 2.0) * scale * 2.0

        # --- CORRECTED HEIGHT ESTIMATION BLOCK ---
        # Calculate height from landmarker estimation
        nose_pt = (landmarks["NOSE"]["x_px_crop"], landmarks["NOSE"]["y_px_crop"])
        ankles_mid = midpoint_2d(l_ankle, r_ankle)
        pixel_height_est = euclidean_distance_2d(nose_pt, ankles_mid)
        
        # Apply the 90% anatomical correction to define the missing variable
        estimated_height_cm = (pixel_height_est / 0.90) * scale

        # Package raw and processed dimensions
        return {
            "shoulder_width": round(shoulder_width, 2),
            "torso_height": round(torso_height, 2),
            "hip_width": round(hip_width, 2),
            "arm_length": round(arm_length, 2),
            "chest_circumference": round(chest_circumference, 2),
            "waist_circumference": round(waist_circumference, 2),
            "hip_circumference": round(hip_circumference, 2),
            "inseam": round(inseam_cm, 2),
            "height_used": round(user_height_cm if user_height_cm else estimated_height_cm, 1),
            "estimated_height_cm": round(estimated_height_cm, 1),
            "calibration_method": calibration_method,
            "scale_factor_cm_per_px": float(scale),
            "raw_pixels": {
                "shoulder_width": float(px_shoulder_width),
                "torso_height": float(px_torso_height),
                "hip_width": float(px_hip_width),
                "l_arm_tot": float(px_l_arm),
                "r_arm_tot": float(px_r_arm),
                "legs_avg": float((px_l_leg + px_r_leg) / 2.0),
                "body_height": float(pixel_height_est)
            }
        }