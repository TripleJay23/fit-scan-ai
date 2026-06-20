package com.fitscan.app.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fitscan.app.domain.model.PoseLandmark
import com.fitscan.app.ui.theme.OnSurfaceDark
import com.fitscan.app.ui.theme.WarmGold

@Composable
fun PoseOverlayCanvas(
    landmarks: List<PoseLandmark>,
    shoulderWidthCm: Float = 44f,
    waistCircCm: Float = 80f,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "scan_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_scale"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_alpha"
    )

    Box(modifier = modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // 1. Draw centered body silhouette guide (dashed outline)
            val path = Path().apply {
                // Approximate standard human silhouette centered nicely
                val cx = width / 2f
                val cy = height / 2f
                val headRadius = width * 0.08f
                
                // Head
                addOval(androidx.compose.ui.geometry.Rect(cx - headRadius, cy - height * 0.35f, cx + headRadius, cy - height * 0.23f))
                
                // Left shoulder to neck
                moveTo(cx - width * 0.15f, cy - height * 0.18f)
                quadraticTo(cx - width * 0.05f, cy - height * 0.20f, cx, cy - height * 0.23f)
                // Right shoulder to neck
                quadraticTo(cx + width * 0.05f, cy - height * 0.20f, cx + width * 0.15f, cy - height * 0.18f)
                
                // Left torso & arm outline
                lineTo(cx - width * 0.18f, cy + height * 0.10f)
                lineTo(cx - width * 0.12f, cy + height * 0.10f)
                lineTo(cx - width * 0.12f, cy - height * 0.10f)
                lineTo(cx - width * 0.08f, cy + height * 0.20f)
                
                // Left leg
                lineTo(cx - width * 0.08f, cy + height * 0.40f)
                lineTo(cx - width * 0.03f, cy + height * 0.40f)
                lineTo(cx - width * 0.02f, cy + height * 0.15f)
                
                // Right leg (symmetrical)
                lineTo(cx + width * 0.02f, cy + height * 0.15f)
                lineTo(cx + width * 0.03f, cy + height * 0.40f)
                lineTo(cx + width * 0.08f, cy + height * 0.40f)
                
                // Right torso & arm outline
                lineTo(cx + width * 0.08f, cy + height * 0.20f)
                lineTo(cx + width * 0.12f, cy - height * 0.10f)
                lineTo(cx + width * 0.12f, cy + height * 0.10f)
                lineTo(cx + width * 0.18f, cy + height * 0.10f)
                close()
            }

            drawPath(
                path = path,
                color = Color.White.copy(alpha = 0.25f),
                style = Stroke(
                    width = 2.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 15f), 0f)
                )
            )

            // 2. Draw animated gold pulsing ring at waist (center-torso level)
            val waistX = width / 2f
            val waistY = height * 0.52f // Waist level
            val baseRadius = 45.dp.toPx()
            
            drawCircle(
                color = WarmGold.copy(alpha = pulseAlpha),
                radius = baseRadius * pulseScale,
                center = Offset(waistX, waistY),
                style = Stroke(width = 3.dp.toPx())
            )
            drawCircle(
                color = WarmGold.copy(alpha = 0.5f),
                radius = baseRadius,
                center = Offset(waistX, waistY),
                style = Stroke(width = 1.5.dp.toPx())
            )
            // Center focal point
            drawCircle(
                color = WarmGold,
                radius = 3.dp.toPx(),
                center = Offset(waistX, waistY)
            )

            // 3. Draw detected skeleton landmarks and connection lines (if populated)
            if (landmarks.isNotEmpty()) {
                val pointMap = landmarks.associateBy { it.index }
                
                fun drawSkeletalLine(i1: Int, i2: Int) {
                    val p1 = pointMap[i1]
                    val p2 = pointMap[i2]
                    if (p1 != null && p2 != null && p1.visibility > 0.6f && p2.visibility > 0.6f) {
                        drawLine(
                            color = Color.White.copy(alpha = 0.50f),
                            start = Offset(p1.x * width, p1.y * height),
                            end = Offset(p2.x * width, p2.y * height),
                            strokeWidth = 2.dp.toPx()
                        )
                    }
                }

                // Connections
                drawSkeletalLine(11, 12) // Shoulder to shoulder
                drawSkeletalLine(11, 13) // L shoulder to arm
                drawSkeletalLine(13, 15) // L elbow to wrist
                drawSkeletalLine(12, 14) // R shoulder to arm
                drawSkeletalLine(14, 16) // R elbow to wrist
                drawSkeletalLine(11, 23) // L shoulder to hip
                drawSkeletalLine(12, 24) // R shoulder to hip
                drawSkeletalLine(23, 24) // Hip to hip
                drawSkeletalLine(23, 25) // L hip to knee
                drawSkeletalLine(25, 27) // L knee to ankle
                drawSkeletalLine(24, 26) // R hip to knee
                drawSkeletalLine(26, 28) // R knee to ankle

                // Draw Dots at detected joints
                landmarks.forEach { lm ->
                    if (lm.visibility > 0.6f) {
                        drawCircle(
                            color = WarmGold,
                            radius = 4.dp.toPx(),
                            center = Offset(lm.x * width, lm.y * height)
                        )
                        drawCircle(
                            color = Color.White,
                            radius = 2.dp.toPx(),
                            center = Offset(lm.x * width, lm.y * height)
                        )
                    }
                }
            }
        }

        // 4. Overlaid labels corresponding precisely to specs:
        // "SHOULDER: 44CM ✓" and "WAIST: 80CM ✓" at accurate levels
        if (landmarks.isNotEmpty()) {
            val pointMap = landmarks.associateBy { lm -> lm.index }
            val lShoulder = pointMap[11]
            val rShoulder = pointMap[12]
            val lHip = pointMap[23]

            if (lShoulder != null && rShoulder != null) {
                // Average shoulder level
                val sY = (lShoulder.y + rShoulder.y) / 2f
                Box(
                    modifier = Modifier
                        .offset(x = 24.dp, y = (sY * 650).dp) // approximate pixel alignment based on screen density
                ) {
                    MeasurementLabelOverlay(label = "SHOULDER: ${shoulderWidthCm.toInt()}CM")
                }
            }

            if (lHip != null) {
                Box(
                    modifier = Modifier
                        .offset(x = 160.dp, y = (lHip.y * 700).dp)
                ) {
                    MeasurementLabelOverlay(label = "WAIST: ${waistCircCm.toInt()}CM")
                }
            }
        }
    }
}

@Composable
fun MeasurementLabelOverlay(label: String) {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier
            .background(
                color = OnSurfaceDark.copy(alpha = 0.08f),
                shape = RoundedCornerShape(8.dp)
            )
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.1f),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
    ) {
        androidx.compose.material3.Text(
            text = label,
            color = OnSurfaceDark,
            fontSize = 12.sp,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.width(4.dp))
        androidx.compose.material3.Icon(
            imageVector = androidx.compose.material.icons.Icons.Filled.CheckCircle,
            contentDescription = null,
            tint = WarmGold,
            modifier = Modifier.padding(start = 2.dp)
        )
    }
}
