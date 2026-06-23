# Technical Skills & Domain Expertise

The development of FitScan requires mobile engineering, computer vision, calibration geometry, and local data management.

## 1. Advanced Android Engineering

- **Jetpack Compose**: Declarative UI patterns, canvas drawing for skeletal overlays, and state management via `StateFlow`.
- **Clean Architecture**: Domain-oriented use cases and repositories that separate business logic from technical details.
- **Concurrency**: Kotlin Coroutines for AI math, database work, and responsive camera interactions.

## 2. Computer Vision & ML Integration

- **MediaPipe Intelligence**: On-device pose inference with MediaPipe Tasks Vision and TFLite-based assets.
- **Edge-Native Detection**: Porting backend model behavior to Android-friendly runtimes and keeping production CV offline.
- **Reference Calibration**: Detecting physical references such as A4 paper and credit cards to improve pixel-to-cm scaling.
- **Coordinate Transformation**: Mapping normalized ML output to image pixels, corrected camera coordinates, and real-world centimeters.
- **Camera Optimization**: Managing CameraX lifecycle, Camera2 intrinsic metadata, and `ImageAnalysis` throughput.

## 3. Computational Geometry

- **Euclidean Spatial Math**: Distance and midpoint formulas in corrected image space.
- **Camera Calibration**: Using focal length, sensor physical size, pixel-array size, intrinsic calibration, and lens distortion metadata where available.
- **Anatomical Modeling**: Translating skeletal points into body estimates using calibrated scaling and physiological constants.

## 4. Local Persistence

- **Room/SQLite**: Time-series measurement schemas, calibration metadata persistence, and efficient history retrieval.
