package com.fitscan.app.ml

import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Build
import com.fitscan.app.domain.model.CameraCalibration
import com.fitscan.app.domain.model.PixelArraySize
import com.fitscan.app.domain.model.SensorPhysicalSize

class CameraCalibrationProvider(private val context: Context) {

    fun getBackCameraCalibration(): CameraCalibration? {
        val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        val cameraId = cameraManager.cameraIdList.firstOrNull { id ->
            val characteristics = cameraManager.getCameraCharacteristics(id)
            characteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK
        } ?: return null

        return getCalibration(cameraId)
    }

    fun getCalibration(cameraId: String): CameraCalibration? {
        val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        val characteristics = cameraManager.getCameraCharacteristics(cameraId)
        val physicalSize = characteristics.get(CameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE)
        val pixelArraySize = characteristics.get(CameraCharacteristics.SENSOR_INFO_PIXEL_ARRAY_SIZE)
        val focalLengths = characteristics.get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS)
            ?.toList()
            .orEmpty()
        val intrinsicCalibration = characteristics.get(CameraCharacteristics.LENS_INTRINSIC_CALIBRATION)
            ?.toList()
            .orEmpty()
        val lensDistortion = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            characteristics.get(CameraCharacteristics.LENS_DISTORTION)?.toList().orEmpty()
        } else {
            emptyList()
        }

        return CameraCalibration(
            cameraId = cameraId,
            focalLengthsMm = focalLengths,
            sensorPhysicalSize = physicalSize?.let {
                SensorPhysicalSize(widthMm = it.width, heightMm = it.height)
            },
            pixelArraySize = pixelArraySize?.let {
                PixelArraySize(widthPx = it.width, heightPx = it.height)
            },
            intrinsicCalibration = intrinsicCalibration,
            lensDistortion = lensDistortion
        )
    }
}
