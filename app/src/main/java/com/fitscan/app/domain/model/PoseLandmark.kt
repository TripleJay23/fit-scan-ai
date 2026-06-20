package com.fitscan.app.domain.model

data class PoseLandmark(
    val index: Int,
    val x: Float,
    val y: Float,
    val z: Float,
    val visibility: Float
)
