package com.fitscan.app.ui.screens.camera

import android.graphics.Bitmap
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.fitscan.app.domain.model.PoseLandmark
import com.fitscan.app.ml.PoseDetector

class PoseAnalyzer(
    private val poseDetector: PoseDetector,
    private val onPoseDetected: (List<PoseLandmark>, Bitmap) -> Unit
) : ImageAnalysis.Analyzer {

    private var lastAnalyzedTimestamp = 0L

    @OptIn(ExperimentalGetImage::class)
    override fun analyze(image: ImageProxy) {
        val currentTimestamp = System.currentTimeMillis()
        
        // Throttle to 1 frame per 1500ms
        if (currentTimestamp - lastAnalyzedTimestamp >= 1500) {
            try {
                // Convert ImageProxy to Bitmap using CameraX utilities (supported natively in 1.3+)
                val bitmap = image.toBitmap()
                
                // Analyze using PoseDetector
                poseDetector.detectLiveStream(bitmap, currentTimestamp) { landmarks ->
                    onPoseDetected(landmarks, bitmap)
                }
                
                lastAnalyzedTimestamp = currentTimestamp
            } catch (e: Exception) {
                // Handle conversions failures gracefully
                e.printStackTrace()
            }
        }
        
        // Critical step: Must close the image proxy to avoid blocking the queue
        image.close()
    }
}
