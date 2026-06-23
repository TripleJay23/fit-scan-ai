# FitScan: Offline-First Clothing Size Estimation

FitScan is an Android application for clothing size recommendations using on-device computer vision. The production source of truth is the Android app: body analysis, calibration, measurement, sizing, and scan history all run locally without a cloud backend.

## Key Features

- **On-Device AI Inference**: Uses MediaPipe's high-fidelity Pose Landmarker for real-time body tracking.
- **Offline-First Architecture**: Production analysis runs in the Android app. Photos and body measurements are not uploaded.
- **Reference Calibration**: Supports user-height fallback plus local reference-object calibration for A4 paper and credit cards when visible.
- **Camera Intrinsics**: Reads Camera2 focal length, physical sensor size, pixel-array size, intrinsic calibration, and distortion metadata when the device exposes them.
- **Precise Body Metrics**: Calculates shoulder width, chest circumference, waist circumference, hip circumference, arm length, torso height, and inseam using calibrated geometry.
- **Smart Size Mapping**: Automatically maps body measurements to international clothing standards (EU, US, UK) for T-Shirts, Shirts, Trousers, and Jackets.
- **Comprehensive History**: Securely stores scan history in a local Room (SQLite) database for reference.

## Technology Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Architecture**: MVVM (Model-View-ViewModel) + Clean Architecture
- **AI/ML**: MediaPipe Tasks Vision (Pose Landmarker Full)
- **Camera**: CameraX with Camera2 intrinsic metadata
- **Database**: Room (SQLite)
- **Navigation**: Jetpack Compose Navigation

## How It Works

1. **Calibration**: The app prefers a detected physical reference object, then camera-intrinsic-assisted measurement, then user height as fallback.
2. **Capture**: In the Camera screen, the app uses the rear camera to detect 33 skeletal landmarks. A skeletal overlay provides instant visual feedback.
3. **Analysis**: Upon tapping "Lock & Analyze", the app captures the current frame and applies local anatomical algorithms to estimate body dimensions.
4. **Recommendation**: Results are mapped to size charts and stored in the local database.

## Source of Truth

FitScan is offline-first. The Android app owns the production measurement algorithm.

The `backend/` directory is deprecated and retained only as historical reference while Android parity is completed. Do not add new production behavior to the Python backend. Any measurement, calibration, detection, or sizing change must land in the Android app first.

## Installation & Setup

1. Clone the repository.
2. Ensure `pose_landmarker_full.task` exists in `app/src/main/assets/`.
3. Optional: place a converted `yolov8n.tflite` model in `app/src/main/assets/` when enabling edge-native person detection.
4. Open the project in Android Studio.
5. Build and run the `:app` module on an Android device (API 24+).

---

*FitScan - precision sizing, powered by your phone.*
