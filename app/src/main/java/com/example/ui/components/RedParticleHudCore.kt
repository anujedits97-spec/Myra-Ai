package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberOrange
import com.example.ui.theme.CyberRedAccent
import com.example.ui.theme.CyberWhite
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun RedParticleHudCore(
    modifier: Modifier = Modifier,
    isListening: Boolean = false,
    isSpeaking: Boolean = false,
    isProcessing: Boolean = false,
    audioRms: Float = 0f,
    onClickCore: () -> Unit = {}
) {
    val infiniteTransition = rememberInfiniteTransition(label = "core_anim")
    val animationValue by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "particle_loop"
    )

    val pulseTransition = rememberInfiniteTransition(label = "pulse_anim")
    val fastPulse by pulseTransition.animateFloat(
        initialValue = 0.82f,
        targetValue = 1.18f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "fast_pulse"
    )

    val reactiveBoost = if (isListening) {
        0.2f + (audioRms * 0.3f)
    } else if (isSpeaking) {
        0.15f * fastPulse
    } else if (isProcessing) {
        0.1f * fastPulse
    } else {
        0f
    }

    Column(
        modifier = modifier.testTag("hud_core_container"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(220.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClickCore
                ),
            contentAlignment = Alignment.Center
        ) {
            Canvas(
                modifier = Modifier
                    .size(200.dp)
                    .testTag("hud_particle_canvas")
            ) {
                drawSophisticatedRedCore(
                    animationValue = animationValue,
                    reactiveBoost = reactiveBoost,
                    isListening = isListening,
                    isSpeaking = isSpeaking,
                    isProcessing = isProcessing
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Sophisticated Dark Status Indicator
        val statusText = when {
            isListening -> "VOICE INPUT CAPTURING"
            isProcessing -> "NEURAL PROCESSING"
            isSpeaking -> "VOCALIZING RESPONSE"
            else -> "SYSTEM ACTIVE"
        }

        val statusColor = when {
            isListening -> CyberRedAccent
            isProcessing -> CyberOrange
            isSpeaking -> CyberCyan
            else -> CyberRedAccent.copy(alpha = 0.65f)
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .background(
                    color = statusColor.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(12.dp)
                )
                .border(
                    width = 1.dp,
                    color = statusColor.copy(alpha = 0.25f),
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(horizontal = 12.dp, vertical = 4.dp)
                .testTag("hud_status_badge")
        ) {
            Icon(
                imageVector = when {
                    isListening -> Icons.Default.Mic
                    isSpeaking -> Icons.AutoMirrored.Filled.VolumeUp
                    else -> Icons.Default.GraphicEq
                },
                contentDescription = null,
                tint = statusColor,
                modifier = Modifier.size(12.dp)
            )
            Text(
                text = " $statusText",
                color = statusColor,
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 2.sp
            )
        }
    }
}

private fun DrawScope.drawSophisticatedRedCore(
    animationValue: Float,
    reactiveBoost: Float,
    isListening: Boolean,
    isSpeaking: Boolean,
    isProcessing: Boolean
) {
    val center = Offset(size.width / 2f, size.height / 2f)
    val maxRadius = (size.width / 2f) * (1f + reactiveBoost * 0.2f)

    // 1. Concentric Subtle Dark HUD Rings (Sophisticated Dark: border-red-500/10, /20, /30)
    drawCircle(
        color = CyberRedAccent.copy(alpha = 0.10f),
        center = center,
        radius = maxRadius,
        style = Stroke(width = 1.dp.toPx())
    )
    drawCircle(
        color = CyberRedAccent.copy(alpha = 0.20f),
        center = center,
        radius = maxRadius * 0.85f,
        style = Stroke(width = 1.dp.toPx())
    )
    drawCircle(
        color = CyberRedAccent.copy(alpha = 0.30f),
        center = center,
        radius = maxRadius * 0.70f,
        style = Stroke(width = 1.dp.toPx())
    )

    // 2. Dashed Orbital Ring (Rotated with animation)
    val dashedRadius = maxRadius * 0.85f
    val rotationOffset = animationValue * 40f
    drawCircle(
        color = CyberRedAccent.copy(alpha = 0.40f),
        center = center,
        radius = dashedRadius,
        style = Stroke(
            width = 1.5.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 10f), rotationOffset)
        )
    )

    // 3. Dynamic Pulsing Dual Cyber Rings
    val r1 = (maxRadius * animationValue).coerceAtLeast(2f)
    val r2 = (maxRadius * (1.0f - animationValue)).coerceAtLeast(2f)
    val pulseRingAlpha = (0.25f + reactiveBoost * 0.2f).coerceAtMost(0.7f)
    drawCircle(
        color = CyberRedAccent.copy(alpha = pulseRingAlpha * 0.6f),
        center = center,
        radius = r1,
        style = Stroke(width = 1.2.dp.toPx())
    )
    drawCircle(
        color = CyberRedAccent.copy(alpha = pulseRingAlpha * 0.6f),
        center = center,
        radius = r2,
        style = Stroke(width = 1.2.dp.toPx())
    )

    // 4. Cardinal Glowing Coordinate Nodes (North, South, East, West with shadow glow)
    val cardinalPositions = listOf(
        Offset(center.x, center.y - maxRadius), // Top (North)
        Offset(center.x, center.y + maxRadius), // Bottom (South)
        Offset(center.x - maxRadius, center.y), // Left (West)
        Offset(center.x + maxRadius, center.y)  // Right (East)
    )
    for (pos in cardinalPositions) {
        // Glowing halo
        drawCircle(
            color = CyberRedAccent.copy(alpha = 0.35f),
            center = pos,
            radius = 6.dp.toPx() * (1f + reactiveBoost)
        )
        // Solid node
        drawCircle(
            color = Color(0xFFF87171), // red-400
            center = pos,
            radius = 3.dp.toPx()
        )
    }

    // 5. 12 Orbital Particle Nodes along the inner orbit
    val particleCount = 12
    for (i in 0 until particleCount) {
        val angle = (i * 2.0 * Math.PI / particleCount) + (animationValue * 2.0 * Math.PI)
        val x = center.x + (maxRadius * 0.70f) * cos(angle).toFloat()
        val y = center.y + (maxRadius * 0.70f) * sin(angle).toFloat()

        val particleColor = if (i % 3 == 0 && (isListening || isSpeaking)) CyberOrange else CyberRedAccent
        drawCircle(
            color = particleColor.copy(alpha = 0.85f),
            center = Offset(x, y),
            radius = 2.8.dp.toPx()
        )
    }

    // 6. Central Deep Red Glow Blur + White Hot Luminous Core
    val coreGlowRadius = maxRadius * (0.45f + reactiveBoost * 0.15f)
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                CyberRedAccent.copy(alpha = 0.85f),
                CyberRedAccent.copy(alpha = 0.40f),
                Color(0x33450A0A),
                Color.Transparent
            ),
            center = center,
            radius = coreGlowRadius * 1.6f
        ),
        center = center,
        radius = coreGlowRadius * 1.5f
    )

    // Solid inner core
    val innerCoreRadius = maxRadius * (0.22f + reactiveBoost * 0.08f)
    drawCircle(
        color = CyberRedAccent.copy(alpha = 0.90f),
        center = center,
        radius = innerCoreRadius
    )

    // White core focal highlight
    drawCircle(
        color = CyberWhite,
        center = center,
        radius = innerCoreRadius * 0.35f
    )
}
