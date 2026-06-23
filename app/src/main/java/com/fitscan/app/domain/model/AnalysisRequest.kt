package com.fitscan.app.domain.model

import android.graphics.Bitmap

data class AnalysisRequest(
    val bitmap: Bitmap,
    val userHeightCm: Float? = null,
    val referenceObjectType: ReferenceObjectType? = null,
    val cameraCalibration: CameraCalibration? = null
)
