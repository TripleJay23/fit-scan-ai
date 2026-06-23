# Documentation: Agent Profiles

This document outlines the specialized internal modules that drive the FitScan ecosystem. FitScan is offline-first; the Android app is the production source of truth for scanning, calibration, measurement, sizing, and persistence.

## 1. Vision Engine (VisionAgent)

- **Role**: High-speed perception, person detection, and landmark localization.
- **Implementation**: `PersonDetector.kt`, `PoseDetector.kt`, `PoseAnalyzer.kt`
- **Responsibilities**:
- Manage the lifecycle of local detection models.
- Keep person detection and crop selection edge-native.
- Transform raw camera buffers (`ImageProxy`) into skeletal coordinate maps.
- Handle fallback logic and model initialization states.

## 2. Anatomical Calculator (MetricAgent)

- **Role**: Mathematical translation of pixels to physical metrics.
- **Implementation**: `MeasurementCalculator.kt`, `ReferenceObjectDetector.kt`, `CameraCalibrationProvider.kt`
- **Responsibilities**:
- Calculate real-world scale factors from reference objects, Camera2 metadata, and user-height fallback.
- Execute Euclidean distance algorithms between corrected body landmarks.
- Apply physiological multipliers to estimate body circumferences.
- Persist calibration method metadata for confidence scoring and auditability.

## 3. Size Strategist (SizingAgent)

- **Role**: Mapping physical metrics to commercial clothing standards.
- **Implementation**: `SizeMapper.kt`
- **Responsibilities**:
- Maintain lookup tables for international sizing (EU, US, UK).
- Generate category-specific fit notes based on body proportions.
- Determine standard sizes for T-shirts, jackets, and trousers.

## 4. State Orchestrator (CoordinationAgent)

- **Role**: Managing user intent and data flow.
- **Implementation**: `AnalyzeImageUseCase.kt`, `CameraViewModel.kt`, `UploadViewModel.kt`
- **Responsibilities**:
- Coordinate the transition from live-viewing or upload selection to deep analysis.
- Ensure data persistence into the Room database.
- Handle error states and provide user feedback during calculation.
- Treat backend code as reference-only.
