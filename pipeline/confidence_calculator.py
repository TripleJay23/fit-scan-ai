"""
FitScan - Stage 5: Confidence Calculation
Computes an index (0 to 100) scoring model inference reliability based on:
1. Average landmark detection visibility/clarity
2. User standing symmetry (checking if they are tilted or rotated away from camera)
3. Calibration method used (rigid physical object references score higher than estimated heights)
"""

import logging
from typing import Dict, Any

logger = logging.getLogger("fitscan.confidence")

class ConfidenceCalculator:
    @staticmethod
    def calculate(landmarks: Dict[str, Dict[str, Any]], measurements: Dict[str, Any]) -> int:
        """
        Calculates a confidence score between 0 and 100.
        
        Args:
            landmarks: Core dictionary of key bones landmarks.
            measurements: Computed dimensions dictionary containing scale factor info.
            
        Returns:
            int: Confidence score (0 to 100)
        """
        score = 100.0

        # --- 1. Landmark Visibility (Max impact: -30 pts) ---
        # Calculate average visibility for key body lines
        key_nodes = [
            "LEFT_SHOULDER", "RIGHT_SHOULDER", 
            "LEFT_HIP", "RIGHT_HIP",
            "LEFT_KNEE", "RIGHT_KNEE",
            "LEFT_ANKLE", "RIGHT_ANKLE"
        ]
        
        visibilities = [landmarks[node]["visibility"] for node in key_nodes if node in landmarks]
        if visibilities:
            avg_vis = sum(visibilities) / len(visibilities)
            # If average visibility is below 0.9, penalize proportionally
            if avg_vis < 0.95:
                penalty = (0.95 - avg_vis) * 100.0  # Max penalty approx 30 points
                score -= min(30.0, penalty)
                logger.debug(f"Visibility penalty applied: -{min(30.0, penalty):.1f} (Avg: {avg_vis:.2f})")
        else:
            score -= 40.0

        # --- 2. Symmetry & Rotation Check (Max impact: -30 pts) ---
        # If the user is rotated away from the camera, 2D calculations of width suffer from 
        # perspective foreshortening. We can detect this rotation by comparing:
        # a) Z-depth differences between shoulders and hips
        # b) Relative symmetry of shoulder/hip bones to the nose midline (horizontal symmetry check)
        
        try:
            l_shoulder = landmarks["LEFT_SHOULDER"]
            r_shoulder = landmarks["RIGHT_SHOULDER"]
            l_hip = landmarks["LEFT_HIP"]
            r_hip = landmarks["RIGHT_HIP"]
            nose = landmarks["NOSE"]

            # check Z depth difference (MediaPipe estimates Z relative to pelvis center, smaller is closer)
            shoulder_z_diff = abs(l_shoulder["z"] - r_shoulder["z"])
            hip_z_diff = abs(l_hip["z"] - r_hip["z"])
            
            # Penalize heavily if shoulders or hips are rotated (z distance differences > 0.15 indicates skew)
            if shoulder_z_diff > 0.12:
                shoulder_rot_penalty = min(15.0, (shoulder_z_diff - 0.12) * 100.0)
                score -= shoulder_rot_penalty
                logger.debug(f"Shoulder rotation penalty: -{shoulder_rot_penalty:.1f} (Z-diff: {shoulder_z_diff:.3f})")

            if hip_z_diff > 0.12:
                hip_rot_penalty = min(15.0, (hip_z_diff - 0.12) * 100.0)
                score -= hip_rot_penalty
                logger.debug(f"Hip rotation penalty: -{hip_rot_penalty:.1f} (Z-diff: {hip_z_diff:.3f})")

            # Symmetry check: Distance on X-axis from nose to left/right shoulder should be equal
            # of standing front-facing and centered.
            mid_shoulder_x = (l_shoulder["x_px_crop"] + r_shoulder["x_px_crop"]) / 2.0
            nose_offset = abs(nose["x_px_crop"] - mid_shoulder_x)
            shoulder_half_width = abs(l_shoulder["x_px_crop"] - r_shoulder["x_px_crop"]) / 2.0
            
            # If nose is offset by > 15% of half-shoulder-width, the torso is likely angled
            symmetry_skew = nose_offset / (shoulder_half_width + 1e-5)
            if symmetry_skew > 0.15:
                asymmetry_penalty = min(15.0, (symmetry_skew - 0.15) * 50.0)
                score -= asymmetry_penalty
                logger.debug(f"Asymmetry penalty: -{asymmetry_penalty:.1f} (Skew: {symmetry_skew:.2f})")
                
        except Exception as e:
            logger.warning(f"Failed to calculate rotation/symmetry checks: {e}")
            score -= 15.0

        # --- 3. Calibration Accuracy Check (Max impact: -10 pts) ---
        cal_method = measurements.get("calibration_method", "")
        if "User Height" in cal_method:
            # Sizing from estimated height can vary based on whether user lands properly on their heels,
            # stands perfectly upright, and camera tilt. Deduct nominal score points for fallback height.
            score -= 5.0
            logger.debug("Deducted 5 points for using static height calibration instead of rigid object.")
        elif "Reference Object" in cal_method:
            # Rigid objects are highly reliable but require precise crop. If ref card is used, score stays high!
            pass

        # Ensure final score remains bounded safely between 0 and 100
        final_score = int(max(10, min(100, round(score))))
        return final_score
