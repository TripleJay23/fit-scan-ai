"""
FitScan - Fast API Backend Router
This acts as the core entry service exposed over API endpoints, managing lazy model 
intialization, image base64 streaming decoders, and full multi-stage AI CV pipeline processing.
"""

import os
import io
import base64
import logging
from typing import Optional
from contextlib import asynccontextmanager

from fastapi import FastAPI, File, UploadFile, Form, HTTPException, status
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel, Field
import numpy as np
import cv2
from PIL import Image

# Import pipeline elements
from pipeline.detector import PersonDetector, PersonDetectionError
from pipeline.pose_estimator import PoseEstimator, PoseEstimationError
from pipeline.measurement_calculator import MeasurementCalculator
from pipeline.size_mapper import SizeMapper
from pipeline.confidence_calculator import ConfidenceCalculator

# Setup elegant formatting logger
logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s [%(levelname)s] %(name)s: %(message)s"
)
logger = logging.getLogger("fitscan.api")

# Lazy Initialized Global Singletons to prevent cold startup bottlenecks
detector_instance: Optional[PersonDetector] = None
estimator_instance: Optional[PoseEstimator] = None
mapper_instance: Optional[SizeMapper] = None

def get_pipeline():
    """Lazy initializing the computer vision engines safely on-demand."""
    global detector_instance, estimator_instance, mapper_instance
    try:
        if detector_instance is None:
            logger.info("Lazily loading YOLOv8 Person Detector...")
            detector_instance = PersonDetector()
        if estimator_instance is None:
            logger.info("Lazily loading MediaPipe Pose Estimator...")
            estimator_instance = PoseEstimator()
        if mapper_instance is None:
            logger.info("Lazily loading Clothing Size Mapper...")
            mapper_instance = SizeMapper()
        return detector_instance, estimator_instance, mapper_instance
    except Exception as e:
        logger.error(f"Failed to initialize computer vision models: {e}")
        raise HTTPException(
            status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
            detail=f"Local CV pipeline initialization failing: {str(e)}. Run download_models.py first."
        )

# Define schemas for JSON payloads
class Base64FrameRequest(BaseModel):
    frame_base64: str = Field(..., description="JPEG/PNG image encoded as a Base64 string")
    height_cm: Optional[float] = Field(None, description="Actual physical height of user in centimeters")
    ref_object_type: Optional[str] = Field(None, description="Type of reference calibration physical object, e.g. 'credit_card' or 'a4'")
    ref_object_pixels: Optional[float] = Field(None, description="Estimated pixel width of reference card/paper in original frame")

@asynccontextmanager
async def lifespan(app: FastAPI):
    # App startup event hook - print local directories and confirm models state
    os.makedirs("models", exist_ok=True)
    logger.info("FitScan FastAPI Backend booting up. Models loaded lazily upon first query to optimize cold start.")
    yield
    logger.info("FitScan FastAPI Backend shutting down.")

app = FastAPI(
    title="FitScan — AI Cloth Size Estimation API",
    description="Locally-runnable computer vision endpoint evaluating physical body measures and assigning tailored garments sizes.",
    version="1.0.0",
    lifespan=lifespan
)

# Enable CORS for smooth mobile app access and cross-domain standard browser requests
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


# Helper: Decode Base64 string to OpenCV BGR image
def decode_base64_image(b64_str: str) -> np.ndarray:
    try:
        # Strip potential HTML prefix e.g., "data:image/jpeg;base64,"
        if "," in b64_str:
            b64_str = b64_str.split(",")[1]
        
        img_bytes = base64.b64decode(b64_str)
        nparr = np.frombuffer(img_bytes, np.uint8)
        image = cv2.imdecode(nparr, cv2.IMREAD_COLOR)
        if image is None:
            raise ValueError("Decoded image numpy array is empty.")
        return image
    except Exception as e:
        logger.error(f"Image decoding failure: {e}")
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail=f"Failed to decode base64 file string. Ensure valid image binary layout. Error: {str(e)}"
        )


# Helper: Process whole AI pipeline
def run_fitscan_pipeline(
    image: np.ndarray,
    height_cm: Optional[float],
    ref_object_type: Optional[str],
    ref_object_pixels: Optional[float]
) -> dict:
    # 1. Lazy load models
    detector, estimator, mapper = get_pipeline()
    
    # 2. Stage 1 - Person detection
    try:
        cropped_person, detect_meta = detector.detect_and_crop(image)
    except PersonDetectionError as pde:
        logger.warning(f"Detection error raised: {pde}")
        raise HTTPException(
            status_code=status.HTTP_422_UNPROCESSABLE_ENTITY,
            detail=str(pde)
        )

    # Extract crop bounding details to map landmarks back
    crop_x1, crop_y1 = detect_meta["bbox"][0], detect_meta["bbox"][1]

    # 3. Stage 2 - Pose Estimation
    try:
        landmarks = estimator.estimate_pose(cropped_person, bbox_offset=(crop_x1, crop_y1))
    except PoseEstimationError as pee:
        logger.warning(f"Pose Estimation failure: {pee}")
        raise HTTPException(
            status_code=status.HTTP_422_UNPROCESSABLE_ENTITY,
            detail=str(pee)
        )

    # Validate landmark quality
    is_valid_landmarks, validation_err = estimator.validate_landmarks(landmarks)
    if not is_valid_landmarks:
        logger.warning(f"Landmarks validation failed: {validation_err}")
        raise HTTPException(
            status_code=status.HTTP_422_UNPROCESSABLE_ENTITY,
            detail=validation_err
        )

    # 4. Stage 3 - Measurement Calculations
    try:
        measurements = MeasurementCalculator.calculate_measurements(
            landmarks=landmarks,
            user_height_cm=height_cm,
            ref_object_type=ref_object_type,
            ref_object_pixels=ref_object_pixels
        )
    except Exception as me:
        logger.error(f"Sizing estimations math failure: {me}")
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Anatomical estimation math failure: {str(me)}"
        )

    # 5. Stage 4 - Outfit Sizing Maps
    recommended_sizes = mapper.map_to_sizes(measurements)
    fit_notes = mapper.fit_notes_generator(measurements)

    # 6. Stage 5 - Calculate Reliability Confidence Score
    confidence_score = ConfidenceCalculator.calculate(landmarks, measurements)

    # Construct the flat response block to match Android DTO
    return {
        "status": "success",
        "confidence": confidence_score,
        "shoulder_width": measurements["shoulder_width"],
        "torso_height": measurements["torso_height"],
        "hip_width": measurements["hip_width"],
        "arm_length": measurements["arm_length"],
        "chest_circumference": measurements["chest_circumference"],
        "waist_circumference": measurements["waist_circumference"],
        "hip_circumference": measurements["hip_circumference"],
        "inseam": measurements["inseam"],
        "height_used": measurements["height_used"],
        "estimated_height_cm": measurements["estimated_height_cm"],
        "calibration_method": measurements["calibration_method"],
        "scale_factor_cm_per_px": measurements["scale_factor_cm_per_px"],
        "model_used": "mediapipe-pose-full + yolov8n"
    }


# GET /health - Monitor local server and check weights files
@app.get("/health", summary="Get model states and system diagnostics")
def health_check():
    detector_loaded = detector_instance is not None
    estimator_loaded = estimator_instance is not None
    mapper_loaded = mapper_instance is not None
    
    # Check if models exist in folder
    yolo_exists = os.path.exists("models/yolov8n.pt") or os.path.exists("models/yolov8n.onnx")
    mediapipe_exists = os.path.exists("models/pose_landmarker_full.task")
    mlp_exists = os.path.exists("models/mlp_size_regressor.joblib")

    return {
        "status": "healthy",
        "server_time_iso": "2026-06-13T04:38:00", # Dev timestamp
        "models_in_memory": {
            "yolov8n_detector": detector_loaded,
            "mediapipe_pose": estimator_loaded,
            "size_mapper_loaded": mapper_loaded
        },
        "local_model_files": {
            "yolov8n_present": yolo_exists,
            "mediapipe_task_present": mediapipe_exists,
            "mlp_regressor_present": mlp_exists
        },
        "privacy_guarantee": "All inference images are parsed strictly in memory and deleted directly after extraction."
    }


# POST /analyze - Traditional files uploading or form request
@app.post("/analyze", summary="Analyze full-size gallery upload image")
async def analyze_image(
    image_file: Optional[UploadFile] = File(None),
    image_base64: Optional[str] = Form(None),
    height_cm: Optional[float] = Form(None),
    ref_object_type: Optional[str] = Form(None),
    ref_object_pixels: Optional[float] = Form(None)
):
    # Verify input availability
    if image_file is None and image_base64 is None:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Requires either 'image_file' multi-part upload or an 'image_base64' string."
        )

    # 1. Parse Image to CV2 NDArray
    if image_file is not None:
        try:
            contents = await image_file.read()
            nparr = np.frombuffer(contents, np.uint8)
            image_bgr = cv2.imdecode(nparr, cv2.IMREAD_COLOR)
            if image_bgr is None:
                raise ValueError("cv2 decoded image represents absolute None.")
        except Exception as e:
            logger.error(f"Failed to read raw upload: {e}")
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="Provided 'image_file' is corrupted or has an invalid image compression scheme."
            )
    else:
        # Fallback to base64 decoder
        image_bgr = decode_base64_image(image_base64)

    # 2. Process
    result = run_fitscan_pipeline(image_bgr, height_cm, ref_object_type, ref_object_pixels)
    return result


# POST /analyze/frame - Light Base64 JSON stream for real-time video frames
@app.post("/analyze/frame", summary="Analyze rapid camera frame from real-time stream")
def analyze_frame(request: Base64FrameRequest):
    # Process base64 frame
    image_bgr = decode_base64_image(request.frame_base64)
    result = run_fitscan_pipeline(
        image=image_bgr,
        height_cm=request.height_cm,
        ref_object_type=request.ref_object_type,
        ref_object_pixels=request.ref_object_pixels
    )
    return result

if __name__ == "__main__":
    import uvicorn
    from dotenv import load_dotenv
    
    # Load environment variables from .env file
    load_dotenv()
    
    # Fetch port from environment, fallback to 8000 if not found
    port = int(os.getenv("APP_PORT", 8000))
    
    # Bind to 0.0.0.0 to allow incoming connections from the Android phone
    uvicorn.run("main:app", host="0.0.0.0", port=port, reload=True)
