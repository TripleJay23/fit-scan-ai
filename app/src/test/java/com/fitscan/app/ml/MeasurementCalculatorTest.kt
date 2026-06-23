package com.fitscan.app.ml

import com.fitscan.app.domain.model.PoseLandmark
import com.fitscan.app.domain.model.ReferenceObjectType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MeasurementCalculatorTest {

    @Test
    fun userHeightCalibrationScalesNormalizedLandmarksInPixelSpace() {
        val measurements = MeasurementCalculator.calculateMeasurements(
            landmarks = landmarks(),
            imageWidth = 1000,
            imageHeight = 2000,
            calibrationInput = MeasurementCalculator.CalibrationInput(userHeightCm = 180f)
        )

        assertEquals("user_height", measurements.calibrationSource)
        assertEquals(180f, measurements.heightCm, 0.01f)
        assertEquals(20.25f, measurements.shoulderWidth, 0.01f)
        assertTrue(measurements.scaleFactorCmPerPx > 0f)
    }

    @Test
    fun referenceObjectCalibrationTakesPriorityOverUserHeight() {
        val measurements = MeasurementCalculator.calculateMeasurements(
            landmarks = landmarks(),
            imageWidth = 1000,
            imageHeight = 2000,
            calibrationInput = MeasurementCalculator.CalibrationInput(
                userHeightCm = 180f,
                referenceObjectType = ReferenceObjectType.CREDIT_CARD,
                referenceObjectPixels = 100f
            )
        )

        assertEquals("reference_object", measurements.calibrationSource)
        assertEquals(0.0856f, measurements.scaleFactorCmPerPx, 0.0001f)
        assertEquals(17.12f, measurements.shoulderWidth, 0.01f)
        assertTrue(measurements.calibrationMethod.contains("credit_card"))
    }

    private fun landmarks(): List<PoseLandmark> {
        return listOf(
            PoseLandmark(index = 0, x = 0.50f, y = 0.10f, z = 0f, visibility = 1f),
            PoseLandmark(index = 11, x = 0.40f, y = 0.25f, z = 0f, visibility = 1f),
            PoseLandmark(index = 12, x = 0.60f, y = 0.25f, z = 0f, visibility = 1f),
            PoseLandmark(index = 13, x = 0.35f, y = 0.45f, z = 0f, visibility = 1f),
            PoseLandmark(index = 14, x = 0.65f, y = 0.45f, z = 0f, visibility = 1f),
            PoseLandmark(index = 15, x = 0.32f, y = 0.60f, z = 0f, visibility = 1f),
            PoseLandmark(index = 16, x = 0.68f, y = 0.60f, z = 0f, visibility = 1f),
            PoseLandmark(index = 23, x = 0.43f, y = 0.52f, z = 0f, visibility = 1f),
            PoseLandmark(index = 24, x = 0.57f, y = 0.52f, z = 0f, visibility = 1f),
            PoseLandmark(index = 25, x = 0.44f, y = 0.72f, z = 0f, visibility = 1f),
            PoseLandmark(index = 26, x = 0.56f, y = 0.72f, z = 0f, visibility = 1f),
            PoseLandmark(index = 27, x = 0.45f, y = 0.90f, z = 0f, visibility = 1f),
            PoseLandmark(index = 28, x = 0.55f, y = 0.90f, z = 0f, visibility = 1f)
        )
    }
}
