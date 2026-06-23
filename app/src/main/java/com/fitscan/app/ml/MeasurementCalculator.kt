package com.fitscan.app.ml

import com.fitscan.app.domain.model.BodyMeasurements
import com.fitscan.app.domain.model.CameraCalibration
import com.fitscan.app.domain.model.PoseLandmark
import com.fitscan.app.domain.model.ReferenceObjectType
import kotlin.math.pow
import kotlin.math.sqrt

object MeasurementCalculator {

    // CONSTANTS MATCHING THE BACKEND ANATOMICAL DESIGN
    private const val CHEST_CIRCUMFERENCE_MULTIPLIER = 2.15f
    private const val WAIST_CIRCUMFERENCE_MULTIPLIER = 1.85f
    private const val HIP_CIRCUMFERENCE_MULTIPLIER = 2.40f
    private const val DEFAULT_HEIGHT_CM = 170f

    data class CalibrationInput(
        val userHeightCm: Float? = null,
        val referenceObjectType: ReferenceObjectType? = null,
        val referenceObjectPixels: Float? = null,
        val cameraCalibration: CameraCalibration? = null,
        val assumedDistanceCm: Float = 150f // Default scan distance
    )

    private data class ImagePoint(val x: Float, val y: Float)

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

    private fun euclideanDistance(p1: ImagePoint, p2: ImagePoint): Float {
        val dx = p1.x - p2.x
        val dy = p1.y - p2.y
        return sqrt(dx * dx + dy * dy)
    }

    private fun midpoint(p1: ImagePoint, p2: ImagePoint): ImagePoint {
        return ImagePoint(
            x = (p1.x + p2.x) / 2f,
            y = (p1.y + p2.y) / 2f
        )
    }

    private fun PoseLandmark.toImagePoint(
        imageWidth: Int,
        imageHeight: Int,
        cameraCalibration: CameraCalibration?
    ): ImagePoint {
        val rawPoint = ImagePoint(
            x = x * imageWidth,
            y = y * imageHeight
        )
        return applyDistortionCorrection(rawPoint, cameraCalibration)
    }

    private fun applyDistortionCorrection(point: ImagePoint, calibration: CameraCalibration?): ImagePoint {
        if (calibration?.hasDistortionCorrection != true) return point

        val fx = calibration.intrinsicCalibration.getOrNull(0) ?: return point
        val fy = calibration.intrinsicCalibration.getOrNull(1) ?: return point
        val cx = calibration.intrinsicCalibration.getOrNull(2) ?: return point
        val cy = calibration.intrinsicCalibration.getOrNull(3) ?: return point
        if (fx == 0f || fy == 0f) return point

        // [k1, k2, k3, p1, p2]
        val k1 = calibration.lensDistortion.getOrNull(0) ?: 0f
        val k2 = calibration.lensDistortion.getOrNull(1) ?: 0f
        val k3 = calibration.lensDistortion.getOrNull(2) ?: 0f
        val p1 = calibration.lensDistortion.getOrNull(3) ?: 0f
        val p2 = calibration.lensDistortion.getOrNull(4) ?: 0f

        val x = (point.x - cx) / fx
        val y = (point.y - cy) / fy
        val r2 = x * x + y * y
        
        // Radial part
        val radial = 1f + k1 * r2 + k2 * r2.pow(2) + k3 * r2.pow(3)
        
        // Tangential part
        val dx = 2f * p1 * x * y + p2 * (r2 + 2f * x * x)
        val dy = p1 * (r2 + 2f * y * y) + 2f * p2 * x * y

        return ImagePoint(
            x = cx + (x * radial + dx) * fx,
            y = cy + (y * radial + dy) * fy
        )
    }

    fun calculateMeasurements(
        landmarks: List<PoseLandmark>,
        heightCm: Float,
        imageWidth: Int,
        imageHeight: Int
    ): BodyMeasurements {
        return calculateMeasurements(
            landmarks = landmarks,
            imageWidth = imageWidth,
            imageHeight = imageHeight,
            calibrationInput = CalibrationInput(userHeightCm = heightCm)
        )
    }

    fun calculateMeasurements(
        landmarks: List<PoseLandmark>,
        imageWidth: Int,
        imageHeight: Int,
        calibrationInput: CalibrationInput
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
        val lKnee = landmarkMap[25] ?: return BodyMeasurements(0f,0f,0f,0f,0f,0f,0f)
        val rKnee = landmarkMap[26] ?: return BodyMeasurements(0f,0f,0f,0f,0f,0f,0f)
        val lAnkle = landmarkMap[27] ?: return BodyMeasurements(0f,0f,0f,0f,0f,0f,0f)
        val rAnkle = landmarkMap[28] ?: return BodyMeasurements(0f,0f,0f,0f,0f,0f,0f)

        val cameraCalibration = calibrationInput.cameraCalibration

        val nosePoint = nose.toImagePoint(imageWidth, imageHeight, cameraCalibration)
        val lShoulderPoint = lShoulder.toImagePoint(imageWidth, imageHeight, cameraCalibration)
        val rShoulderPoint = rShoulder.toImagePoint(imageWidth, imageHeight, cameraCalibration)
        val lElbowPoint = lElbow.toImagePoint(imageWidth, imageHeight, cameraCalibration)
        val rElbowPoint = rElbow.toImagePoint(imageWidth, imageHeight, cameraCalibration)
        val lWristPoint = lWrist.toImagePoint(imageWidth, imageHeight, cameraCalibration)
        val rWristPoint = rWrist.toImagePoint(imageWidth, imageHeight, cameraCalibration)
        val lHipPoint = lHip.toImagePoint(imageWidth, imageHeight, cameraCalibration)
        val rHipPoint = rHip.toImagePoint(imageWidth, imageHeight, cameraCalibration)
        val lKneePoint = lKnee.toImagePoint(imageWidth, imageHeight, cameraCalibration)
        val rKneePoint = rKnee.toImagePoint(imageWidth, imageHeight, cameraCalibration)
        val lAnklePoint = lAnkle.toImagePoint(imageWidth, imageHeight, cameraCalibration)
        val rAnklePoint = rAnkle.toImagePoint(imageWidth, imageHeight, cameraCalibration)

        val anklesMid = midpoint(lAnklePoint, rAnklePoint)
        val noseToAnklePixels = euclideanDistance(nosePoint, anklesMid)
        if (noseToAnklePixels <= 0f) return BodyMeasurements(0f,0f,0f,0f,0f,0f,0f)

        val actualPixelHeight = noseToAnklePixels / 0.90f
        val referencePixels = calibrationInput.referenceObjectPixels
        val referenceObjectType = calibrationInput.referenceObjectType
        val cameraAssisted = cameraCalibration?.hasIntrinsicGeometry == true
        val userHeight = calibrationInput.userHeightCm?.takeIf { it > 0f } ?: DEFAULT_HEIGHT_CM

        val scaleFactor: Float
        val calibrationMethod: String
        val calibrationSource: String

        // Method A: Reference Object (Highest Precision)
        if (referenceObjectType != null && referencePixels != null && referencePixels > 0f) {
            scaleFactor = referenceObjectType.physicalWidthCm / referencePixels
            calibrationMethod = "Reference Object Calibration (${referenceObjectType.key}: ${referenceObjectType.physicalWidthCm}cm = ${"%.1f".format(referencePixels)}px)"
            calibrationSource = "reference_object"
        } 
        // Method B: Camera2 Intrinsics (Mitigate Distortion)
        else if (cameraCalibration?.sensorPhysicalSize != null && 
                 cameraCalibration.focalLengthsMm.isNotEmpty() && 
                 cameraCalibration.pixelArraySize != null) {
            
            val focalLength = cameraCalibration.focalLengthsMm[0]
            val sensorWidth = cameraCalibration.sensorPhysicalSize.widthMm
            val pixelWidth = cameraCalibration.pixelArraySize.widthPx
            
            // Basic scale factor at assumed distance D (Thick lens/pinhole approximation)
            // S = (D * sensor_w) / (f * pixel_w)
            val baseScale = (calibrationInput.assumedDistanceCm * (sensorWidth / 10f)) / 
                           ((focalLength / 10f) * pixelWidth)
            
            // Correct using user height to refine distance estimate
            val calculatedHeight = actualPixelHeight * baseScale
            val distanceCorrection = userHeight / calculatedHeight
            scaleFactor = baseScale * distanceCorrection
            
            calibrationMethod = "Camera Intrinsic Calibration (Assisted by User Height)"
            calibrationSource = "camera_intrinsics"
        }
        // Method C: User Height (Fallback)
        else {
            scaleFactor = userHeight / actualPixelHeight
            calibrationSource = "user_height"
            calibrationMethod = "User Height Calibration (${userHeight}cm = ${"%.1f".format(actualPixelHeight)}px)"
        }

        // 2. Transpile width spatial metrics
        val shoulderWidth = euclideanDistance(lShoulderPoint, rShoulderPoint) * scaleFactor
        val hipWidth = euclideanDistance(lHipPoint, rHipPoint) * scaleFactor
        val torsoHeight = euclideanDistance(
            midpoint(lShoulderPoint, rShoulderPoint),
            midpoint(lHipPoint, rHipPoint)
        ) * scaleFactor

        // 3. Average arm tracking length paths
        val lArmLength = euclideanDistance(lShoulderPoint, lElbowPoint) + euclideanDistance(lElbowPoint, lWristPoint)
        val rArmLength = euclideanDistance(rShoulderPoint, rElbowPoint) + euclideanDistance(rElbowPoint, rWristPoint)
        val armLength = ((lArmLength + rArmLength) / 2f) * scaleFactor
        val lLegLength = euclideanDistance(lKneePoint, lAnklePoint)
        val rLegLength = euclideanDistance(rKneePoint, rAnklePoint)
        val inseam = ((lLegLength + rLegLength) / 2f) * scaleFactor * 2f

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
            heightCm = userHeight,
            shoulderWidth = shoulderWidth,
            chestCirc = chestCirc,
            waistCirc = waistCirc,
            hipWidth = hipWidth,     // Correctly mapped
            hipCirc = hipCirc,
            armLength = armLength,
            torsoHeight = torsoHeight,
            inseam = inseam,
            scaleFactorCmPerPx = scaleFactor,
            calibrationMethod = calibrationMethod,
            calibrationSource = calibrationSource,
            cameraCalibrationUsed = cameraAssisted
        )
    }
}
