package com.fitscan.app.ml

import com.fitscan.app.domain.model.BodyMeasurements
import com.fitscan.app.domain.model.PoseLandmark
import kotlin.math.sqrt

object MeasurementCalculator {

    // CONSTANTS MATCHING THE BACKEND ANATOMICAL DESIGN
    private const val CHEST_CIRCUMFERENCE_MULTIPLIER = 2.15f
    private const val WAIST_CIRCUMFERENCE_MULTIPLIER = 1.85f
    private const val HIP_CIRCUMFERENCE_MULTIPLIER = 2.40f

    fun euclideanDistance(p1: PoseLandmark, p2: PoseLandmark): Float {
        val dx = p1.x - p2.x
        val dy = p1.y - p2.y
        return sqrt(dx * dx + dy * dy)
    }

    fun midpoint(p1: PoseLandmark, p2: PoseLandmark): PoseLandmark {
        return PoseLandmark(
            index = -1,
            x = (p1.x + p2.x) / 2f,
            y = (p1.y + p2.y) / 2f,
            z = (p1.z + p2.z) / 2f,
            visibility = (p1.visibility + p2.visibility) / 2f
        )
    }

    fun calculateMeasurements(
        landmarks: List<PoseLandmark>,
        heightCm: Float,
        imageWidth: Int,
        imageHeight: Int
    ): BodyMeasurements {
        // Map list into an easily queryable dictionary index mapping
        val landmarkMap = landmarks.associateBy { it.index }

        // Core extraction indices
        val nose = landmarkMap[0] ?: return BodyMeasurements(0f,0f,0f,0f,0f,0f,0f)
        val lShoulder = landmarkMap[11] ?: return BodyMeasurements(0f,0f,0f,0f,0f,0f,0f)
        val rShoulder = landmarkMap[12] ?: return BodyMeasurements(0f,0f,0f,0f,0f,0f,0f)
        val lElbow = landmarkMap[13] ?: return BodyMeasurements(0f,0f,0f,0f,0f,0f,0f)
        val rElbow = landmarkMap[14] ?: return BodyMeasurements(0f,0f,0f,0f,0f,0f,0f)
        val lWrist = landmarkMap[15] ?: return BodyMeasurements(0f,0f,0f,0f,0f,0f,0f)
        val rWrist = landmarkMap[16] ?: return BodyMeasurements(0f,0f,0f,0f,0f,0f,0f)
        val lHip = landmarkMap[23] ?: return BodyMeasurements(0f,0f,0f,0f,0f,0f,0f)
        val rHip = landmarkMap[24] ?: return BodyMeasurements(0f,0f,0f,0f,0f,0f,0f)
        val lAnkle = landmarkMap[27] ?: return BodyMeasurements(0f,0f,0f,0f,0f,0f,0f)
        val rAnkle = landmarkMap[28] ?: return BodyMeasurements(0f,0f,0f,0f,0f,0f,0f)

        // 1. Calculate Calibration Scale Factor matching the backend ratio
        val anklesMid = midpoint(lAnkle, rAnkle)
        val noseToAnklePixels = euclideanDistance(nose, anklesMid)

        if (noseToAnklePixels <= 0f) return BodyMeasurements(0f,0f,0f,0f,0f,0f,0f)

        // Correct for 90% nose-to-ankle anatomical height distribution
        val actualPixelHeight = noseToAnklePixels / 0.90f
        val scaleFactor = heightCm / actualPixelHeight

        // 2. Transpile width spatial metrics
        val shoulderWidth = euclideanDistance(lShoulder, rShoulder) * scaleFactor
        val hipWidth = euclideanDistance(lHip, rHip) * scaleFactor

        // 3. Average arm tracking length paths
        val lArmLength = euclideanDistance(lShoulder, lElbow) + euclideanDistance(lElbow, lWrist)
        val rArmLength = euclideanDistance(rShoulder, rElbow) + euclideanDistance(rElbow, rWrist)
        val armLength = ((lArmLength + rArmLength) / 2f) * scaleFactor

        // 4. Calculate final structural estimations
        val chestCirc = shoulderWidth * CHEST_CIRCUMFERENCE_MULTIPLIER

        var waistCircMod = WAIST_CIRCUMFERENCE_MULTIPLIER
        val hipToShoulderRatio = hipWidth / (shoulderWidth + 1e-5f)
        if (hipToShoulderRatio > 0.95f) {
            waistCircMod += 0.05f
        } else if (hipToShoulderRatio < 0.82f) {
            waistCircMod -= 0.05f
        }
        val waistCirc = hipWidth * waistCircMod
        val hipCirc = hipWidth * HIP_CIRCUMFERENCE_MULTIPLIER

        // Returns the mapped model containing the explicitly requested fields
        return BodyMeasurements(
            heightCm = heightCm,
            shoulderWidth = shoulderWidth,
            chestCirc = chestCirc,
            waistCirc = waistCirc,
            hipWidth = hipWidth,     // Correctly mapped
            hipCirc = hipCirc,
            armLength = armLength
        )
    }
}