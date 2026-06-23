# Backend Deprecation Notice

This backend module is now **DEPRECATED** and serves as a reference-only archive.

### Reason for Deprecation
To ensure FitScan is a truly **Edge-Native** and **Offline-First** application, all computer vision, anatomical math, and sizing algorithms have been ported to the Android frontend.

### Final Production Flow (Android App)
1. **Pose Detection**: MediaPipe Tasks Vision (On-Device).
2. **Person Detection**: YOLOv8 TFLite (On-Device).
3. **Anatomical Math**: `MeasurementCalculator.kt` (Kotlin).
4. **Calibration**: Camera2 Intrinsics + Reference Objects (On-Device).

### Reference Use Only
Developers should use this folder only to understand the legacy Python-based prototype logic. New sizing rules or model updates should be implemented directly in the Android Kotlin codebase.

---
*Date: 2026-06-24*
