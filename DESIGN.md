# Design Philosophy & Visual Language

FitScan uses a "Luxury Technical" aesthetic, combining precision-focused data with a premium utility experience.

## 1. Visual Identity

- **Primary (CharcoalDark)**: `#121212`. Deep neutral background to make data and overlays pop.
- **Accent (WarmGold)**: `#D4AF37`. Primary action buttons, key metrics, and skeletal joints.
- **Secondary (SurfaceContainerDark)**: `#1E1E1E`. Subtle elevation for cards and containers.
- **Success (SoftGreen)**: `#4CAF50`. Analysis complete states and high-confidence metrics.

## 2. Interface Principles

- **Clarity Over Clutter**: The camera screen focuses on body guidance, pose feedback, and one primary action.
- **Bento Grid Layout**: Preferences and history use a clean grid for dense information.
- **Real-Time Feedback**: Skeletal overlays and camera-status messaging show the local AI pipeline working.

## 3. User Experience

- **Single Source of Truth**: Android owns the production measurement pipeline; backend code is reference-only.
- **Calibration Transparency**: Results should identify whether measurement used a reference object, camera intrinsics, or user-height fallback.
- **Offline Reliability**: Scans should not depend on a cloud upload or server response.
- **Luxury Finish**: Subtle gradients, rounded corners, and restrained typography keep the app premium without hiding measurement state.

## 4. Components

- **ConfidenceBadge**: Circular indicator summarizing detection clarity and calibration quality.
- **MeasurementChips**: Compact chips for body metrics.
- **PoseOverlayCanvas**: Custom skeletal view that serves as both functional guide and high-tech visual centerpiece.
