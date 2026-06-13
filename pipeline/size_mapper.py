"""
FitScan - Stage 4: Size Mapping
Maps computed physical measurements to retail size guides (Intl, EU, US, UK) 
and handles fitting notifications or ML-supported size prediction overrides.
"""

import os
import logging
from typing import Dict, Any, Optional

logger = logging.getLogger("fitscan.mapper")

# Rule-Based Size Lookup Map based on Chest Circumference
SIZE_LOOKUP_TABLE = [
    {"max_chest": 86.0,  "intl": "XS",  "eu": 44, "us": "XS",  "uk": "XS",  "shirt_us": "14.5"},
    {"max_chest": 92.0,  "intl": "S",   "eu": 46, "us": "S",   "uk": "S",   "shirt_us": "15.0"},
    {"max_chest": 98.0,  "intl": "M",   "eu": 48, "us": "M",   "uk": "M",   "shirt_us": "15.5"},
    {"max_chest": 104.0, "intl": "L",   "eu": 50, "us": "L",   "uk": "L",   "shirt_us": "16.0"},
    {"max_chest": 110.0, "intl": "XL",  "eu": 52, "us": "XL",  "uk": "XL",  "shirt_us": "16.5"},
    {"max_chest": 116.0, "intl": "XXL", "eu": 54, "us": "XXL", "uk": "XXL", "shirt_us": "17.5"},
    {"max_chest": float('inf'), "intl": "XXXL", "eu": 56, "us": "XXXL", "uk": "XXXL", "shirt_us": "18.5"}
]

class SizeMapper:
    def __init__(self, model_dir: str = "models"):
        """
        Initializes the SizeMapper. 
        Attempts to load an MLP regressor model if present in models directory.
        """
        self.mlp_model_path = os.path.join(model_dir, "mlp_size_regressor.joblib")
        self.mlp_model = None
        
        self.load_mlp_model()

    def load_mlp_model(self):
        """Attempts to load a pre-trained tiny MLP model if it is exported."""
        if os.path.exists(self.mlp_model_path):
            try:
                import joblib
                self.mlp_model = joblib.load(self.mlp_model_path)
                logger.info(f"Loaded ML size correction model from {self.mlp_model_path}")
            except Exception as e:
                logger.warning(f"Could not load MLP model at {self.mlp_model_path}: {e}")
        else:
            logger.debug("No custom MLP size model found. Running rule-based inference.")

    def map_to_sizes(self, measurements: Dict[str, Any]) -> Dict[str, Any]:
        """
        Maps physical dimensions in cm to international recommended sizing specifications.
        """
        chest = measurements["chest_circumference"]
        shoulder = measurements["shoulder_width"]
        waist = measurements["waist_circumference"]
        inseam = measurements["inseam"]

        # 1. Base Size Matching from Lookup Table
        matched_row = None
        for row in SIZE_LOOKUP_TABLE:
            if chest < row["max_chest"]:
                matched_row = row
                break

        # Safety Fallback
        if not matched_row:
            matched_row = SIZE_LOOKUP_TABLE[-1]

        # 2. Extract and format outputs
        intl_val = matched_row["intl"]
        eu_val = matched_row["eu"]
        us_val = matched_row["us"]
        uk_val = matched_row["uk"]
        shirt_us_val = matched_row["shirt_us"]

        # Option: Overwrite EU size with MLP Model estimate if present
        if self.mlp_model is not None:
            try:
                # Features: [shoulder, chest, waist, hip, height]
                features = [
                    shoulder, 
                    chest, 
                    waist, 
                    measurements["hip_circumference"], 
                    measurements["height_used"]
                ]
                predicted_eu = self.mlp_model.predict([features])[0]
                # Map recommended EU output back to nearest even integer sizes
                adjusted_eu = int(round(predicted_eu / 2.0) * 2.0)
                adjusted_eu = max(44, min(56, adjusted_eu))
                
                # Align lookup row with MLP predictions
                for row in SIZE_LOOKUP_TABLE:
                    if row["eu"] == adjusted_eu:
                        matched_row = row
                        intl_val = matched_row["intl"]
                        eu_val = matched_row["eu"]
                        us_val = matched_row["us"]
                        uk_val = matched_row["uk"]
                        shirt_us_val = matched_row["shirt_us"]
                        break
                logger.info(f"ML Override activated. Model selected EU Size: {adjusted_eu}")
            except Exception as e:
                logger.error(f"Failed to estimate size with MLP: {e}. Defaulting to lookup.")

        # 3. Trouser Mapping
        # Waist in inches = waist circumference in cm / 2.54
        waist_in_raw = waist / 2.54
        # Even size rounding (standard retail trouser US waist sizes: 28 to 42)
        waist_in = int(round(waist_in_raw / 2.0) * 2.0)
        waist_in = max(28, min(42, waist_in))

        # Inseam in inches = Inseam in cm / 2.54
        inseam_in_raw = inseam / 2.54
        inseam_in = int(round(inseam_in_raw))
        inseam_in = max(26, min(36, inseam_in))

        # EU trouser size = waist inches + 16 (standard European trouser scaling)
        eu_trouser = waist_in + 16

        recommended_sizes = {
            "tshirt": {
                "intl": intl_val,
                "eu": eu_val,
                "us": us_val,
                "uk": uk_val
            },
            "shirt": {
                "intl": intl_val,
                "eu": eu_val,
                "us": shirt_us_val,
                "uk": uk_val
            },
            "trousers": {
                "waist_in": waist_in,
                "inseam_in": inseam_in,
                "eu": eu_trouser
            },
            "jacket": {
                "intl": intl_val,
                "eu": eu_val
            }
        }

        return recommended_sizes

    def fit_notes_generator(self, measurements: Dict[str, Any]) -> str:
        """
        Generates tailored, smart design-advice fit notes based on skeletal proportion checks.
        """
        notes = []
        shoulder = measurements["shoulder_width"]
        chest = measurements["chest_circumference"]
        waist = measurements["waist_circumference"]
        hip = measurements["hip_circumference"]
        height = measurements["height_used"]

        # Broad Shoulders relative to chest circumference checks
        # Typically shoulder_width * 2.15 predicts chest. If actual shoulder width is broad, notify the customer
        expected_shoulders = chest / 2.15
        if shoulder > (expected_shoulders + 2.5):
            notes.append("Broad shoulders detected. Consider sizing up for structured jackets/outerwear to ensure clean sleeve drape.")
        
        # Athletic proportions adjustment
        waist_to_hip_ratio = waist / (hip + 1e-5)
        if waist_to_hip_ratio < 0.75:
            notes.append("Significant taper at the waist detected. Tailored-fit shirts or athletic-cut trousers are highly recommended.")
        elif waist_to_hip_ratio > 0.95:
            notes.append("Straight/relaxed torso silhouette detected. Standard/relaxed cuts will provide superior comfort around the waist.")

        # Height consideration
        if height > 185:
            notes.append("Tall stature. You may need 'Tall' size variations to preserve torso and sleeve length.")
        elif height < 165:
            notes.append("Short stature or petite frame. You may need shorter trouser hems or 'Short' sleeve configurations.")

        # Default fallback note if none apply
        if not notes:
            notes.append("Balanced core proportions. Stick to standard fit-charts for optimal comfort.")

        return " ".join(notes)
