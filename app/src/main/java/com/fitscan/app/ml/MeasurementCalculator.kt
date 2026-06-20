package com.fitscan.app.ml

import com.fitscan.app.domain.model.BodyMeasurements
import com.fitscan.app.domain.model.PoseLandmark
import kotlin.math.sqrt

object MeasurementCalculator {

    fun euclideanDistance(p1: PoseLandmark, p2: PoseLandmark, width: Int, height: Int): Float {
        val dx = (p1.x - p2.x) * width
        val dy = (p1.y - p2.y) * height
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
        val landmarkMap = landmarks.associateBy { it.index }

        val nose = landmarkMap[0] ?: PoseLandmark(0, 0.50f, 0.15f, 0f, 1f)
        val lShoulder = landmarkMap[11] ?: PoseLandmark(11, 0.40f, 0.28f, 0f, 1f)
        val rShoulder = landmarkMap[12] ?: PoseLandmark(12, 0.60f, 0.28f, 0f, 1f)
        val lElbow = landmarkMap[13] ?: PoseLandmark(13, 0.35f, 0.45f, 0f, 1f)
        val rElbow = landmarkMap[14] ?: PoseLandmark(14, 0.65f, 0.45f, 0f, 1f)
        val lWrist = landmarkMap[15] ?: PoseLandmark(15, 0.32f, 0.60f, 0f, 1f)
        val rWrist = landmarkMap[16] ?: PoseLandmark(16, 0.68f, 0.60f, 0f, 1f)
        val lHip = landmarkMap[23] ?: PoseLandmark(23, 0.43f, 0.52f, 0f, 1f)
        val rHip = landmarkMap[24] ?: PoseLandmark(24, 0.57f, 0.52f, 0f, 1f)
        val lAnkle = landmarkMap[27] ?: PoseLandmark(27, 0.45f, 0.90f, 0f, 1f)
        val rAnkle = landmarkMap[28] ?: PoseLandmark(28, 0.55f, 0.90f, 0f, 1f)

        val anklesMid = midpoint(lAnkle, rAnkle)

        // pixelBodyHeight = distance(NOSE, midpoint(L_ANKLE, R_ANKLE))
        val pixelBodyHeight = euclideanDistance(nose, anklesMid, imageWidth, imageHeight)

        // scaleFactor = heightCm / pixelBodyHeight
        val scaleFactor = if (pixelBodyHeight > 0) heightCm / pixelBodyHeight else 1.0f

        // shoulderWidth = distance(L_SHOULDER, R_SHOULDER) * scaleFactor
        val shoulderWidth = euclideanDistance(lShoulder, rShoulder, imageWidth, imageHeight) * scaleFactor

        // hipWidth = distance(L_HIP, R_HIP) * scaleFactor
        val hipWidth = euclideanDistance(lHip, rHip, imageWidth, imageHeight) * scaleFactor

        // chestCirc = shoulderWidth * 2.15f
        val chestCirc = shoulderWidth * 2.15f

        // waistCirc = hipWidth * 1.85f
        val waistCirc = hipWidth * 1.85f

        // hipCirc = hipWidth * 2.40f
        val hipCirc = hipWidth * 2.40f

        // armLength = (distance(L_SHOULDER,L_ELBOW) + distance(L_ELBOW,L_WRIST)) * scaleFactor
        val armLength = (euclideanDistance(lShoulder, lElbow, imageWidth, imageHeight) +
                euclideanDistance(lElbow, lWrist, imageWidth, imageHeight)) * scaleFactor

        return BodyMeasurements(
            shoulderWidth = shoulderWidth,
            chestCirc = chestCirc,
            waistCirc = waistCirc,
            hipCirc = hipCirc,
            armLength = armLength,
            hipWidth = hipWidth,
            heightCm = heightCm
        )
    }
}
