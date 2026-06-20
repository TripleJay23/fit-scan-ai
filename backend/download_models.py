"""
FitScan Model Downloader and Exporter
Downloads MediaPipe Pose Landmarker Full task models, and downloads plus exports
YOLOv8n weights into high-speed portable ONNX runtime structures.
"""

import os
import sys
import urllib.request
import logging
import shutil

logging.basicConfig(level=logging.INFO, format="%(asctime)s [%(levelname)s] %(message)s")
logger = logging.getLogger("fitscan.download")

MEDIAPIPE_POSE_URL = (
    "https://storage.googleapis.com/mediapipe-models/pose_landmarker/"
    "pose_landmarker_full/float16/latest/pose_landmarker_full.task"
)

def download_file_with_progress(url: str, output_path: str):
    """Downloads a file from url to output_path showing simple download logs."""
    logger.info(f"Downloading from: {url}")
    logger.info(f"Saving to: {output_path}")
    
    def _progress(block_num, block_size, total_size):
        read_so_far = block_num * block_size
        if total_size > 0:
            percent = min(100, (read_so_far * 100) / total_size)
            sys.stdout.write(f"\rDownloading: {percent:.1f}% ({read_so_far}/{total_size} bytes)")
            sys.stdout.flush()
        else:
            sys.stdout.write(f"\rDownloading: {read_so_far} bytes reading...")
            sys.stdout.flush()

    try:
        urllib.request.urlretrieve(url, output_path, reporthook=_progress)
        print("\nDownload completed successfully.")
    except Exception as e:
        logger.error(f"\nFailed to download file: {e}")
        raise

def main():
    # 1. Create directory structure
    models_dir = "models"
    os.makedirs(models_dir, exist_ok=True)
    logger.info(f"Target models folder verified/created: {models_dir}")

    # 2. Download MediaPipe Pose Landmarker
    mp_target = os.path.join(models_dir, "pose_landmarker_full.task")
    if os.path.exists(mp_target):
        logger.info(f"MediaPipe Pose Task file already exists at {mp_target}. Skipping download.")
    else:
        logger.info("Initializing MediaPipe Pose Landmarker download...")
        try:
            download_file_with_progress(MEDIAPIPE_POSE_URL, mp_target)
        except Exception as e:
            logger.error(f"Failed to acquire MediaPipe Pose assets: {e}")

    # 3. Download and export YOLOv8n
    yolo_target_pt = os.path.join(models_dir, "yolov8n.pt")
    yolo_target_onnx = os.path.join(models_dir, "yolov8n.onnx")

    if os.path.exists(yolo_target_onnx):
        logger.info(f"YOLOv8n ONNX model already exists at {yolo_target_onnx}. Skipping download and export.")
    else:
        logger.info("Initializing Ultralytics YOLOv8n setup and ONNX export...")
        try:
            from ultralytics import YOLO
            
            # This downloads yolov8n.pt programmatically from Ultralytics servers to the root working dir if missing
            logger.info("Loading yolov8n.pt. If missing, Ultralytics will auto-fetch weights...")
            model = YOLO("yolov8n.pt")
            
            # Export the model programmatically to high-speed ONNX format
            logger.info("Exporting YOLOv8n model to ONNX format...")
            exported_path = model.export(format="onnx", imgsz=640, optimize=True)
            
            # After export, move pt and onnx models into our local models/ directory
            if os.path.exists("yolov8n.pt") and not os.path.exists(yolo_target_pt):
                shutil.move("yolov8n.pt", yolo_target_pt)
                logger.info(f"Moved PyTorch weights to: {yolo_target_pt}")
            
            if exported_path and os.path.exists(exported_path):
                # If exported_path is not already equal to models/yolov8n.onnx, move it
                if os.path.abspath(exported_path) != os.path.abspath(yolo_target_onnx):
                    # Ultralytics writes by default to root 'yolov8n.onnx' or subdirectory. Let's find and copy it.
                    shutil.move(exported_path, yolo_target_onnx)
                logger.info(f"YOLOv8n ONNX export completed and placed in: {yolo_target_onnx}")
            elif os.path.exists("yolov8n.onnx"):
                shutil.move("yolov8n.onnx", yolo_target_onnx)
                logger.info(f"YOLOv8n ONNX export completed and placed in: {yolo_target_onnx}")
            else:
                logger.warning("YOLOv8n exported ONNX path not found in typical output directions. Checking default locations...")
                
        except ImportError:
            logger.error("Ultralytics library missing from execution scope. Cannot run YOLO ONNX exports. Run 'pip install -r requirements.txt'.")
        except Exception as e:
            logger.error(f"Skeletal YOLO detection weights processing failed: {e}")

    logger.info("All FitScan models setup actions completed.")

if __name__ == "__main__":
    main()
