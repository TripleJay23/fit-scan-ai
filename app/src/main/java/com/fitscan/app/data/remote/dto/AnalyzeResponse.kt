package com.fitscan.app.data.remote.dto

import com.google.gson.annotations.SerializedName

// This precisely matches the JSON dictionary returned by measurement_calculator.py
data class AnalyzeResponse(
    @SerializedName("shoulder_width") val shoulderWidth: Float,
    @SerializedName("torso_height") val torsoHeight: Float,
    @SerializedName("hip_width") val hipWidth: Float,
    @SerializedName("arm_length") val armLength: Float,
    @SerializedName("chest_circumference") val chestCircumference: Float,
    @SerializedName("waist_circumference") val waistCircumference: Float,
    @SerializedName("hip_circumference") val hipCircumference: Float,
    @SerializedName("inseam") val inseam: Float,
    @SerializedName("height_used") val heightUsed: Float,
    @SerializedName("estimated_height_cm") val estimatedHeightCm: Float,
    @SerializedName("calibration_method") val calibrationMethod: String,
    @SerializedName("scale_factor_cm_per_px") val scaleFactorCmPerPx: Float
)