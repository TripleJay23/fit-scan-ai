package com.fitscan.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.fitscan.app.domain.model.BodyMeasurements
import com.fitscan.app.domain.model.ClothingSize
import com.fitscan.app.domain.model.ScanResult

@Entity(tableName = "measurements_history")
data class MeasurementEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val confidenceScore: Int,
    val shoulderWidth: Float,
    val chestCirc: Float,
    val waistCirc: Float,
    val hipWidth: Float,
    val hipCirc: Float,
    val armLength: Float,
    val torsoHeight: Float,
    val inseam: Float,
    val scaleFactorCmPerPx: Float,
    val calibrationMethod: String,
    val calibrationSource: String,
    val cameraCalibrationUsed: Boolean,
    val heightCm: Float,
    val sizeCategory: String,
    val sizeText: String,
    val sizeEu: String, // Changed to String to match the domain model
    val sizeUs: String,
    val sizeUk: String,
    val fitNotes: String
) {
    fun toScanResult(): ScanResult {
        return ScanResult(
            id = id.toInt(), // Safely cast Long to Int
            timestamp = timestamp,
            confidenceScore = confidenceScore,
            heightCm = heightCm, // Satisfies the missing parameter error
            recommendedSize = sizeText,
            measurements = BodyMeasurements(
                heightCm = heightCm,
                shoulderWidth = shoulderWidth,
                chestCirc = chestCirc,
                waistCirc = waistCirc,
                hipWidth = hipWidth,
                hipCirc = hipCirc,
                armLength = armLength,
                torsoHeight = torsoHeight,
                inseam = inseam,
                scaleFactorCmPerPx = scaleFactorCmPerPx,
                calibrationMethod = calibrationMethod,
                calibrationSource = calibrationSource,
                cameraCalibrationUsed = cameraCalibrationUsed
            ),
            clothingSizes = listOf(
                ClothingSize(
                    category = sizeCategory,
                    sizeText = sizeText,
                    euSize = sizeEu, // Now correctly passes a String
                    usSize = sizeUs,
                    ukSize = sizeUk,
                    fitNotes = fitNotes
                )
            )
        )
    }

    companion object {
        fun fromScanResult(result: ScanResult): MeasurementEntity {
            val primarySize = result.clothingSizes.firstOrNull()

            return MeasurementEntity(
                id = result.id.toLong(), // Safely cast Int to Long
                timestamp = result.timestamp,
                confidenceScore = result.confidenceScore,
                shoulderWidth = result.measurements.shoulderWidth,
                chestCirc = result.measurements.chestCirc,
                waistCirc = result.measurements.waistCirc,
                hipWidth = result.measurements.hipWidth,
                hipCirc = result.measurements.hipCirc,
                armLength = result.measurements.armLength,
                torsoHeight = result.measurements.torsoHeight,
                inseam = result.measurements.inseam,
                scaleFactorCmPerPx = result.measurements.scaleFactorCmPerPx,
                calibrationMethod = result.measurements.calibrationMethod,
                calibrationSource = result.measurements.calibrationSource,
                cameraCalibrationUsed = result.measurements.cameraCalibrationUsed,
                heightCm = result.heightCm,
                sizeCategory = primarySize?.category ?: "Overall",
                sizeText = primarySize?.sizeText ?: result.recommendedSize,
                sizeEu = primarySize?.euSize ?: "", // Fallback to empty string instead of 0
                sizeUs = primarySize?.usSize ?: "",
                sizeUk = primarySize?.ukSize ?: "",
                fitNotes = primarySize?.fitNotes ?: ""
            )
        }
    }
}
