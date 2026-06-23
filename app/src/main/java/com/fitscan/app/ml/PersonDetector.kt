package com.fitscan.app.ml

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Rect
import android.util.Log
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import java.io.FileNotFoundException
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel
import kotlin.math.max
import kotlin.math.min

data class PersonDetectionResult(
    val bitmap: Bitmap,
    val boundingBox: Rect,
    val confidence: Float,
    val modelUsed: Boolean,
    val message: String
)

class PersonDetector(private val context: Context) {

    private val yoloAssetName = "yolov8n.tflite"
    private val interpreter: Interpreter? by lazy {
        try {
            Interpreter(loadModelBuffer())
        } catch (_: FileNotFoundException) {
            null
        } catch (e: Exception) {
            Log.w("PersonDetector", "Unable to initialize YOLOv8 TFLite: ${e.message}")
            null
        }
    }

    fun detectAndCrop(bitmap: Bitmap): PersonDetectionResult {
        val activeInterpreter = interpreter
        if (activeInterpreter == null) {
            return fullFrameResult(bitmap, "YOLOv8 TFLite asset missing; using full-frame offline pose analysis.")
        }

        return try {
            detectWithYolo(bitmap, activeInterpreter)
                ?: fullFrameResult(bitmap, "YOLOv8 did not find a confident person; using full frame.")
        } catch (e: Exception) {
            Log.w("PersonDetector", "YOLOv8 inference failed: ${e.message}")
            fullFrameResult(bitmap, "YOLOv8 inference failed; using full frame.")
        }
    }

    private fun loadModelBuffer(): ByteBuffer {
        context.assets.openFd(yoloAssetName).use { assetFileDescriptor ->
            FileInputStream(assetFileDescriptor.fileDescriptor).use { stream ->
                return stream.channel.map(
                    FileChannel.MapMode.READ_ONLY,
                    assetFileDescriptor.startOffset,
                    assetFileDescriptor.declaredLength
                )
            }
        }
    }

    private fun detectWithYolo(bitmap: Bitmap, interpreter: Interpreter): PersonDetectionResult? {
        val inputTensor = interpreter.getInputTensor(0)
        val inputShape = inputTensor.shape()
        if (inputShape.size != 4) return null

        val inputHeight = inputShape[1]
        val inputWidth = inputShape[2]
        val channels = inputShape[3]
        if (channels != 3) return null

        val inputBuffer = createInputBuffer(bitmap, inputWidth, inputHeight, inputTensor.dataType())
        val outputTensor = interpreter.getOutputTensor(0)
        if (outputTensor.dataType() != DataType.FLOAT32) return null

        val outputShape = outputTensor.shape()
        if (outputShape.size != 3) return null

        val outputSize = outputShape.fold(1) { acc, value -> acc * value }
        val outputBuffer = ByteBuffer.allocateDirect(outputSize * 4).order(ByteOrder.nativeOrder())
        interpreter.run(inputBuffer, outputBuffer)
        outputBuffer.rewind()

        val output = FloatArray(outputSize)
        outputBuffer.asFloatBuffer().get(output)

        return decodeYoloOutput(
            bitmap = bitmap,
            inputWidth = inputWidth,
            inputHeight = inputHeight,
            outputShape = outputShape,
            output = output
        )
    }

    private fun createInputBuffer(
        bitmap: Bitmap,
        inputWidth: Int,
        inputHeight: Int,
        dataType: DataType
    ): ByteBuffer {
        val bytesPerChannel = if (dataType == DataType.FLOAT32) 4 else 1
        val buffer = ByteBuffer.allocateDirect(inputWidth * inputHeight * 3 * bytesPerChannel)
            .order(ByteOrder.nativeOrder())
        val resized = Bitmap.createScaledBitmap(bitmap, inputWidth, inputHeight, true)
        val pixels = IntArray(inputWidth * inputHeight)
        resized.getPixels(pixels, 0, inputWidth, 0, 0, inputWidth, inputHeight)

        pixels.forEach { pixel ->
            val r = (pixel shr 16) and 0xFF
            val g = (pixel shr 8) and 0xFF
            val b = pixel and 0xFF
            if (dataType == DataType.FLOAT32) {
                buffer.putFloat(r / 255f)
                buffer.putFloat(g / 255f)
                buffer.putFloat(b / 255f)
            } else {
                buffer.put(r.toByte())
                buffer.put(g.toByte())
                buffer.put(b.toByte())
            }
        }
        buffer.rewind()
        return buffer
    }

    private fun decodeYoloOutput(
        bitmap: Bitmap,
        inputWidth: Int,
        inputHeight: Int,
        outputShape: IntArray,
        output: FloatArray
    ): PersonDetectionResult? {
        val dimA = outputShape[1]
        val dimB = outputShape[2]
        val channelsFirst = dimA < dimB
        val attributes = if (channelsFirst) dimA else dimB
        val boxes = if (channelsFirst) dimB else dimA
        if (attributes < 5) return null

        fun value(attribute: Int, box: Int): Float {
            return if (channelsFirst) {
                output[attribute * boxes + box]
            } else {
                output[box * attributes + attribute]
            }
        }

        var bestBox: Rect? = null
        var bestArea = 0
        var bestConfidence = 0f
        val confidenceThreshold = 0.6f

        for (boxIndex in 0 until boxes) {
            val confidence = value(4, boxIndex)
            if (confidence < confidenceThreshold) continue

            val centerX = value(0, boxIndex)
            val centerY = value(1, boxIndex)
            val width = value(2, boxIndex)
            val height = value(3, boxIndex)
            val mappedBox = toBitmapRect(
                bitmap = bitmap,
                inputWidth = inputWidth,
                inputHeight = inputHeight,
                centerX = centerX,
                centerY = centerY,
                width = width,
                height = height
            )
            val area = mappedBox.width() * mappedBox.height()
            if (area > bestArea) {
                bestArea = area
                bestBox = mappedBox
                bestConfidence = confidence
            }
        }

        val box = bestBox ?: return null
        val paddedBox = padBox(box, bitmap.width, bitmap.height)
        val cropped = Bitmap.createBitmap(
            bitmap,
            paddedBox.left,
            paddedBox.top,
            paddedBox.width().coerceAtLeast(1),
            paddedBox.height().coerceAtLeast(1)
        )
        return PersonDetectionResult(
            bitmap = cropped,
            boundingBox = paddedBox,
            confidence = bestConfidence,
            modelUsed = true,
            message = "YOLOv8 TFLite person crop"
        )
    }

    private fun toBitmapRect(
        bitmap: Bitmap,
        inputWidth: Int,
        inputHeight: Int,
        centerX: Float,
        centerY: Float,
        width: Float,
        height: Float
    ): Rect {
        val normalizedOutput = centerX <= 1.5f && centerY <= 1.5f && width <= 1.5f && height <= 1.5f
        val sourceWidth = if (normalizedOutput) 1f else inputWidth.toFloat()
        val sourceHeight = if (normalizedOutput) 1f else inputHeight.toFloat()
        val scaleX = bitmap.width / sourceWidth
        val scaleY = bitmap.height / sourceHeight
        val left = ((centerX - width / 2f) * scaleX).toInt().coerceIn(0, bitmap.width - 1)
        val top = ((centerY - height / 2f) * scaleY).toInt().coerceIn(0, bitmap.height - 1)
        val right = ((centerX + width / 2f) * scaleX).toInt().coerceIn(left + 1, bitmap.width)
        val bottom = ((centerY + height / 2f) * scaleY).toInt().coerceIn(top + 1, bitmap.height)
        return Rect(left, top, right, bottom)
    }

    private fun padBox(box: Rect, imageWidth: Int, imageHeight: Int): Rect {
        val padX = (box.width() * 0.05f).toInt()
        val padY = (box.height() * 0.05f).toInt()
        return Rect(
            max(0, box.left - padX),
            max(0, box.top - padY),
            min(imageWidth, box.right + padX),
            min(imageHeight, box.bottom + padY)
        )
    }

    private fun fullFrameResult(bitmap: Bitmap, message: String): PersonDetectionResult {
        return PersonDetectionResult(
            bitmap = bitmap,
            boundingBox = Rect(0, 0, bitmap.width, bitmap.height),
            confidence = 0f,
            modelUsed = false,
            message = message
        )
    }
}
