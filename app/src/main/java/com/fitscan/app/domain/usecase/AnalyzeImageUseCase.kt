package com.fitscan.app.domain.usecase

import android.content.Context
import android.graphics.Bitmap
import com.fitscan.app.data.repository.MeasurementRepository
import com.fitscan.app.domain.model.BodyMeasurements
import com.fitscan.app.domain.model.ClothingSize
import com.fitscan.app.domain.model.ScanResult
import com.fitscan.app.ml.MeasurementCalculator
import com.fitscan.app.ml.PoseDetector
import com.fitscan.app.ml.SizeMapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class AnalyzeImageUseCase(
    private val context: Context,
    private val repository: MeasurementRepository,
    private val poseDetector: PoseDetector
) {
    suspend operator fun invoke(bitmap: Bitmap, heightCm: Float): Result<ScanResult> = withContext(Dispatchers.Default) {
        try {
            // 1. Detect Landmarks locally on-device
            val landmarks = poseDetector.detectInImage(bitmap)
            
            if (landmarks.isEmpty()) {
                return@withContext Result.failure(Exception("AI could not detect your body clearly. Ensure full body is visible."))
            }

            // 2. Calculate Measurements locally
            val measurements = MeasurementCalculator.calculateMeasurements(
                landmarks = landmarks,
                heightCm = heightCm,
                imageWidth = bitmap.width,
                imageHeight = bitmap.height
            )

            // 3. Generate Clothing Size Recommendations locally
            val recommendedSizes = SizeMapper.generateClothingSizes(measurements)

            // 4. Map to Domain Model
            val resultId = (System.currentTimeMillis() % 100000).toInt()
            val result = ScanResult(
                id = resultId,
                timestamp = System.currentTimeMillis(),
                confidenceScore = 92, // Locally calculated baseline
                heightCm = heightCm,
                recommendedSize = recommendedSizes.firstOrNull()?.sizeText ?: "N/A",
                measurements = measurements,
                clothingSizes = recommendedSizes
            )

            // 5. Save the local calculation to the Room Database
            repository.saveScan(result)

            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
