package com.fitscan.app.domain.model

data class BodyMeasurements(
    val shoulderWidth: Float,
    val chestCirc: Float,
    val waistCirc: Float,
    val hipCirc: Float,
    val armLength: Float,
    val hipWidth: Float,
    val heightCm: Float,
    val torsoHeight: Float = 0f,
    val inseam: Float = 0f,
    val scaleFactorCmPerPx: Float = 0f,
    val calibrationMethod: String = "Unknown",
    val calibrationSource: String = "unknown",
    val cameraCalibrationUsed: Boolean = false
)
