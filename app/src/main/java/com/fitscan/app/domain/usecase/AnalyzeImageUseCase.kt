package com.fitscan.app.domain.usecase

import android.graphics.Bitmap
import com.fitscan.app.data.repository.MeasurementRepository
import com.fitscan.app.domain.model.ScanResult
import com.fitscan.app.ml.MeasurementCalculator
import com.fitscan.app.ml.PoseDetector
import com.fitscan.app.ml.SizeMapper

class AnalyzeImageUseCase(
    private val poseDetector: PoseDetector,
    private val repository: MeasurementRepository
) {
    suspend operator fun invoke(
        bitmap: Bitmap,
        heightCm: Float,
        imagePath: String? = null
    ): ScanResult {
        // 1. Run Pose Landmark Detection using on-device MediaPipe Model
        val landmarks = poseDetector.detectInImage(bitmap)
        
        // 2. Perform dimension calculation mapping
        val bodyMeasurements = MeasurementCalculator.calculateMeasurements(
            landmarks = landmarks,
            heightCm = heightCm,
            imageWidth = bitmap.width,
            imageHeight = bitmap.height
        )

        // 3. Size mapper calculations for specific types (Shirt, Trousers, etc.)
        val clothingSizes = SizeMapper.generateClothingSizes(bodyMeasurements)
        val (overallSizeText, _) = SizeMapper.mapChestToSize(bodyMeasurements.chestCirc)

        // Average landmark visibility maps to accuracy
        val avgVisibility = if (landmarks.isNotEmpty()) {
            landmarks.map { it.visibility }.average()
        } else {
            0.85
        }
        val confidenceScore = if (avgVisibility > 0.85) 87 else if (avgVisibility > 0.70) 74 else 58

        val scanResult = ScanResult(
            id = 0, // AutoGen
            timestamp = System.currentTimeMillis(),
            heightCm = heightCm,
            measurements = bodyMeasurements,
            recommendedSize = overallSizeText,
            clothingSizes = clothingSizes,
            confidenceScore = confidenceScore,
            imagePath = imagePath
        )

        // 4. Save scan to local SQLite Database via Room Repository
        val savedId = repository.saveScan(scanResult)
        return scanResult.copy(id = savedId)
    }
}
