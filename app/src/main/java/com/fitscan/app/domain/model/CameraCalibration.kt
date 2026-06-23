package com.fitscan.app.domain.model

data class SensorPhysicalSize(
    val widthMm: Float,
    val heightMm: Float
)

data class PixelArraySize(
    val widthPx: Int,
    val heightPx: Int
)

data class CameraCalibration(
    val cameraId: String,
    val focalLengthsMm: List<Float> = emptyList(),
    val sensorPhysicalSize: SensorPhysicalSize? = null,
    val pixelArraySize: PixelArraySize? = null,
    val intrinsicCalibration: List<Float> = emptyList(),
    val lensDistortion: List<Float> = emptyList()
) {
    val activeFocalLengthMm: Float?
        get() = focalLengthsMm.firstOrNull()

    val hasIntrinsicGeometry: Boolean
        get() = activeFocalLengthMm != null && sensorPhysicalSize != null && pixelArraySize != null

    val hasDistortionCorrection: Boolean
        get() = intrinsicCalibration.size >= 4 && lensDistortion.size >= 3
}
