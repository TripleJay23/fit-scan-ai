package com.fitscan.app.domain.model

data class ScanResult(
    val id: Int = 0,
    val timestamp: Long,
    val heightCm: Float,
    val measurements: BodyMeasurements,
    val recommendedSize: String,
    val clothingSizes: List<ClothingSize>,
    val confidenceScore: Int,
    val imagePath: String? = null
)
