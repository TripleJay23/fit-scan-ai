package com.fitscan.app.ml

import android.graphics.Bitmap
import android.graphics.Color
import com.fitscan.app.domain.model.ReferenceObjectType
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

data class ReferenceObjectDetection(
    val type: ReferenceObjectType,
    val pixelWidth: Float,
    val confidence: Float,
    val boundingBox: IntArray
)

object ReferenceObjectDetector {

    fun detect(bitmap: Bitmap, type: ReferenceObjectType?): ReferenceObjectDetection? {
        if (type == null || bitmap.width <= 0 || bitmap.height <= 0) return null

        val step = max(2, min(bitmap.width, bitmap.height) / 320)
        val meanBrightness = estimateMeanBrightness(bitmap, step)

        var minX = bitmap.width
        var minY = bitmap.height
        var maxX = -1
        var maxY = -1
        var matched = 0

        var y = 0
        while (y < bitmap.height) {
            var x = 0
            while (x < bitmap.width) {
                val color = bitmap.getPixel(x, y)
                if (matchesReferencePixel(color, meanBrightness, type)) {
                    minX = min(minX, x)
                    minY = min(minY, y)
                    maxX = max(maxX, x)
                    maxY = max(maxY, y)
                    matched++
                }
                x += step
            }
            y += step
        }

        if (matched == 0 || maxX <= minX || maxY <= minY) return null

        val boxWidth = (maxX - minX).toFloat()
        val boxHeight = (maxY - minY).toFloat()
        val shorter = min(boxWidth, boxHeight)
        val longer = max(boxWidth, boxHeight)
        val minUsefulSide = min(bitmap.width, bitmap.height) * 0.05f
        if (shorter < minUsefulSide) return null

        val expectedAspect = when (type) {
            ReferenceObjectType.A4_PAPER -> 1.414f
            ReferenceObjectType.CREDIT_CARD -> 1.586f
        }
        val aspect = longer / shorter
        val aspectScore = (1f - (abs(aspect - expectedAspect) / expectedAspect)).coerceIn(0f, 1f)
        if (aspectScore < 0.45f) return null

        val sampledArea = ((boxWidth / step) * (boxHeight / step)).coerceAtLeast(1f)
        val fillScore = (matched / sampledArea).coerceIn(0f, 1f)
        val confidence = (aspectScore * 0.7f + fillScore * 0.3f).coerceIn(0f, 1f)
        val pixelWidth = if (type.useLongSideForPixels) longer else shorter

        return ReferenceObjectDetection(
            type = type,
            pixelWidth = pixelWidth,
            confidence = confidence,
            boundingBox = intArrayOf(minX, minY, maxX, maxY)
        )
    }

    private fun estimateMeanBrightness(bitmap: Bitmap, step: Int): Float {
        var total = 0f
        var count = 0
        var y = 0
        while (y < bitmap.height) {
            var x = 0
            while (x < bitmap.width) {
                total += brightness(bitmap.getPixel(x, y))
                count++
                x += step
            }
            y += step
        }
        return if (count == 0) 0f else total / count
    }

    private fun matchesReferencePixel(color: Int, meanBrightness: Float, type: ReferenceObjectType): Boolean {
        val brightness = brightness(color)
        val saturation = saturation(color)
        return when (type) {
            ReferenceObjectType.A4_PAPER -> brightness > max(165f, meanBrightness + 35f) && saturation < 70f
            ReferenceObjectType.CREDIT_CARD -> brightness > max(110f, meanBrightness + 15f) && saturation < 120f
        }
    }

    private fun brightness(color: Int): Float {
        return (Color.red(color) * 0.299f) + (Color.green(color) * 0.587f) + (Color.blue(color) * 0.114f)
    }

    private fun saturation(color: Int): Float {
        val maxChannel = max(Color.red(color), max(Color.green(color), Color.blue(color))).toFloat()
        val minChannel = min(Color.red(color), min(Color.green(color), Color.blue(color))).toFloat()
        return maxChannel - minChannel
    }
}
