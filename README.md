# FitScan: AI-Powered Clothing Size Estimation

FitScan is a modern Android application designed to provide accurate clothing size recommendations using on-device computer vision. By leveraging high-fidelity pose estimation, the app calculates precise body measurements in real-time, ensuring users find their perfect fit without the need for manual measurements or external servers.

## Key Features

-   **On-Device AI Inference**: Uses MediaPipe's high-fidelity Pose Landmarker for real-time body tracking.
*   **Privacy-First Architecture**: All body scans and measurements are processed locally on the phone. Photos are never stored or uploaded.
*   **Offline Functionality**: Works 100% offline, eliminating the need for a backend server or internet connection during the scanning process.
*   **Precise Body Metrics**: Calculates shoulder width, chest circumference, waist circumference, hip circumference, and arm length using Euclidean geometry and anatomical scaling.
*   **Smart Size Mapping**: Automatically maps body measurements to international clothing standards (EU, US, UK) for various categories like T-Shirts, Shirts, Trousers, and Jackets.
*   **Comprehensive History**: Securely stores scan history in a local Room (SQLite) database for easy reference.

## Technology Stack

-   **Language**: Kotlin
-   **UI Framework**: Jetpack Compose
-   **Architecture**: MVVM (Model-View-ViewModel) + Clean Architecture
-   **AI/ML**: MediaPipe Tasks Vision (Pose Landmarker Full)
-   **Camera**: CameraX
-   **Database**: Room (SQLite)
-   **Navigation**: Jetpack Compose Navigation

## How It Works

1.  **Calibration**: Users set their height in the Profile settings, which serves as a reference point for scaling pixel measurements to centimeters.
2.  **Capture**: In the Camera screen, the app uses the rear camera to detect 33 skeletal landmarks. A skeletal overlay provides instant visual feedback.
3.  **Analysis**: Upon tapping "Lock & Analyze", the app captures the current landmarks and applies anatomical algorithms to estimate body volume and dimensions.
4.  **Recommendation**: The results are instantly mapped to size charts and stored in the local database.

## Installation & Setup

1.  Clone the repository.
2.  Ensure you have the `pose_landmarker_full.task` file in `app/src/main/assets/`.
3.  Open the project in Android Studio.
4.  Build and run the `:app` module on an Android device (API 24+).

---
*FitScan — Precision sizing, powered by your phone.*
