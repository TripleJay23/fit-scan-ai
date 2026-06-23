# Backend Deprecated

The FitScan team has chosen an offline-first product direction.

The Android app is now the production source of truth for body analysis, measurement calibration, and clothing size mapping. This Python backend is retained only as reference material during migration of historical measurement logic.

Do not add new production measurement behavior here. Port needed behavior into the Android app instead.

Current Android migration targets:

- Person detection and crop selection must be edge-native.
- Reference-object calibration must run locally.
- Camera2 intrinsic metadata must be used when available.
- Backend dependencies must not be required for normal app scanning.
