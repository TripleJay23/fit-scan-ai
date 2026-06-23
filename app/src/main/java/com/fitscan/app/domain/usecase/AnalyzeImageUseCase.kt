package com.fitscan.app.domain.usecase

import android.graphics.Bitmap
import com.fitscan.app.data.repository.MeasurementRepository
import com.fitscan.app.domain.model.AnalysisRequest
import com.fitscan.app.domain.model.ScanResult
import com.fitscan.app.ml.MeasurementCalculator
import com.fitscan.app.ml.PersonDetector
import com.fitscan.app.ml.PoseDetector
import com.fitscan.app.ml.ReferenceObjectDetector
import com.fitscan.app.ml.SizeMapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AnalyzeImageUseCase(
    private val repository: MeasurementRepository,
    private val poseDetector: PoseDetector,
    private val personDetector: PersonDetector
) {
    suspend operator fun invoke(bitmap: Bitmap, heightCm: Float): Result<ScanResult> {
        return invoke(AnalysisRequest(bitmap = bitmap, userHeightCm = heightCm))
    }

    suspend operator fun invoke(request: AnalysisRequest): Result<ScanResult> = withContext(Dispatchers.Default) {
        try {
            val personDetection = personDetector.detectAndCrop(request.bitmap)
            val analysisBitmap = personDetection.bitmap
            val referenceDetection = ReferenceObjectDetector.detect(
                bitmap = request.bitmap,
                type = request.referenceObjectType
            )

            // 1. Detect Landmarks locally on-device
            val landmarks = poseDetector.detectInImage(analysisBitmap)
            
            if (landmarks.isEmpty()) {
                return@withContext Result.failure(Exception("AI could not detect your body clearly. Ensure full body is visible."))
            }

            // 2. Calculate Measurements locally
            val measurements = MeasurementCalculator.calculateMeasurements(
                landmarks = landmarks,
                imageWidth = analysisBitmap.width,
                imageHeight = analysisBitmap.height,
                calibrationInput = MeasurementCalculator.CalibrationInput(
                    userHeightCm = request.userHeightCm,
                    referenceObjectType = request.referenceObjectType,
                    referenceObjectPixels = referenceDetection?.pixelWidth,
                    cameraCalibration = request.cameraCalibration
                )
            )

            // 3. Generate Clothing Size Recommendations locally
            val recommendedSizes = SizeMapper.generateClothingSizes(measurements)

            // 4. Map to Domain Model
            val avgVisibility = landmarks.map { it.visibility }.average().toFloat()
            val calibrationScore = when (measurements.calibrationSource) {
                "reference_object" -> ((referenceDetection?.confidence ?: 0f) * 100).toInt().coerceIn(60, 100)
                "camera_intrinsics_height" -> 85
                "user_height" -> 70
                else -> 50
            }
            val detectionScore = if (personDetection.modelUsed) {
                (personDetection.confidence * 100).toInt().coerceIn(0, 100)
            } else {
                70
            }
            val confidenceScore = (
                avgVisibility * 60f +
                    calibrationScore * 0.25f +
                    detectionScore * 0.15f
                ).toInt().coerceIn(0, 100)
            
            val resultId = (System.currentTimeMillis() % 100000).toInt()
            val result = ScanResult(
                id = resultId,
                timestamp = System.currentTimeMillis(),
                confidenceScore = confidenceScore,
                heightCm = measurements.heightCm,
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
