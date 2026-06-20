package com.fitscan.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.fitscan.app.domain.model.BodyMeasurements
import com.fitscan.app.domain.model.ScanResult
import com.fitscan.app.ml.SizeMapper

@Entity(tableName = "measurements")
data class MeasurementEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long,
    val heightCm: Float,
    val confidenceScore: Int,
    val shoulderWidth: Float,
    val chestCirc: Float,
    val waistCirc: Float,
    val hipCirc: Float,
    val armLength: Float,
    val hipWidth: Float,
    val recommendedSize: String,
    val imagePath: String? = null
) {
    fun toScanResult(): ScanResult {
        val measurements = BodyMeasurements(
            shoulderWidth = shoulderWidth,
            chestCirc = chestCirc,
            waistCirc = waistCirc,
            hipCirc = hipCirc,
            armLength = armLength,
            hipWidth = hipWidth,
            heightCm = heightCm
        )
        return ScanResult(
            id = id,
            timestamp = timestamp,
            heightCm = heightCm,
            measurements = measurements,
            recommendedSize = recommendedSize,
            clothingSizes = SizeMapper.generateClothingSizes(measurements),
            confidenceScore = confidenceScore,
            imagePath = imagePath
        )
    }

    companion object {
        fun fromScanResult(scanResult: ScanResult): MeasurementEntity {
            return MeasurementEntity(
                id = scanResult.id,
                timestamp = scanResult.timestamp,
                heightCm = scanResult.heightCm,
                confidenceScore = scanResult.confidenceScore,
                shoulderWidth = scanResult.measurements.shoulderWidth,
                chestCirc = scanResult.measurements.chestCirc,
                waistCirc = scanResult.measurements.waistCirc,
                hipCirc = scanResult.measurements.hipCirc,
                armLength = scanResult.measurements.armLength,
                hipWidth = scanResult.measurements.hipWidth,
                recommendedSize = scanResult.recommendedSize,
                imagePath = scanResult.imagePath
            )
        }
    }
}
