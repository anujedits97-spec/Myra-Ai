package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
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
import com.example.ui.theme.CyberBlack
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberOrange
import com.example.ui.theme.CyberOrangeGlow
import com.example.ui.theme.CyberRedAccent
import com.example.ui.theme.CyberRedBorder
import com.example.ui.theme.CyberWhite
import kotlin.math.cos
import kotlin.math.sin

/**
 * High-particle animated circular Picture-in-Picture (PiP) view,
 * designed with rotating particle halos, concentric neon outline rings,
 * and audio-reactive core dynamics similar to Gemini Live.
 */
@Composable
fun MayaPipCircleView(
    isListening: Boolean,
    isSpeaking: Boolean,
    isProcessing: Boolean,
    audioRms: Float,
    onClickCore: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Primary infinite rotation transition
    val infiniteTransition = rememberInfiniteTransition(label = "pip_galaxy_orbit")
    
    // Fast clockwise rotation
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (isProcessing) 2000 else if (isListening) 3500 else 6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation_cw"
    )

    // Counter-clockwise orbital rotation
    val reverseRotationAngle by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (isProcessing) 2800 else 7500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation_ccw"
    )

    // Cosmic breathing wave
    val breathScale by infiniteTransition.animateFloat(
        initialValue = 0.90f,
        targetValue = 1.10f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (isListening) 800 else 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breath_scale"
    )

    // Reactive audio boost
    val reactiveBoost by animateFloatAsState(
        targetValue = if (isListening) {
            (audioRms / 8f).coerceIn(0.1f, 1.4f)
        } else if (isSpeaking) {
            0.65f * breathScale
        } else if (isProcessing) {
            0.4f * breathScale
        } else {
            0.05f
        },
        animationSpec = tween(durationMillis = 80),
        label = "pip_reactive_boost"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CyberBlack)
            .testTag("maya_pip_container"),
        contentAlignment = Alignment.Center
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(6.dp),
            contentAlignment = Alignment.Center
        ) {
            // Well-proportioned medium circle footprint
            val circleSize = minOf(maxWidth, maxHeight) * 0.94f

            Box(
                modifier = Modifier
                    .size(circleSize)
                    .aspectRatio(1f)
                    .clip(CircleShape)
                    .background(Color(0xFF030305))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onClickCore
                    )
                    .testTag("pip_circle_click_target"),
                contentAlignment = Alignment.Center
            ) {
                // High Particle Gemini-style HUD Canvas
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("pip_particle_canvas")
                ) {
                    drawGeminiStyleParticleHud(
                        rotationAngle = rotationAngle,
                        reverseRotationAngle = reverseRotationAngle,
                        reactiveBoost = reactiveBoost,
                        breathScale = breathScale,
                        isListening = isListening,
                        isSpeaking = isSpeaking,
                        isProcessing = isProcessing
                    )
                }

                // Center Compact Holographic Status Overlay
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(bottom = 6.dp)
                ) {
                    val iconVector = when {
                        isListening -> Icons.Default.Mic
                        isSpeaking -> Icons.AutoMirrored.Filled.VolumeUp
                        isProcessing -> Icons.Default.GraphicEq
                        else -> Icons.Default.Mic
                    }

                    val activeColor = when {
                        isListening -> CyberOrange
                        isSpeaking -> CyberCyan
                        isProcessing -> CyberRedAccent
                        else -> CyberRedAccent.copy(alpha = 0.85f)
                    }

                    Icon(
                        imageVector = iconVector,
                        contentDescription = null,
                        tint = activeColor,
                        modifier = Modifier.size(16.dp)
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    val statusLabel = when {
                        isListening -> "LISTENING"
                        isSpeaking -> "SPEAKING"
                        isProcessing -> "THINKING"
                        else -> "MAYA LIVE"
                    }

                    Text(
                        text = statusLabel,
                        color = activeColor,
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.2.sp
                    )
                }
            }
        }
    }
}

/**
 * Draws the high particle outline, multi-ring vortex, and orbital particle trails.
 */
private fun DrawScope.drawGeminiStyleParticleHud(
    rotationAngle: Float,
    reverseRotationAngle: Float,
    reactiveBoost: Float,
    breathScale: Float,
    isListening: Boolean,
    isSpeaking: Boolean,
    isProcessing: Boolean
) {
    val center = Offset(size.width / 2f, size.height / 2f)
    val maxRadius = (size.width / 2f) * 0.94f

    // 1. Outermost Glowing Ambient Aura (Red & Amber Neon Blend)
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                CyberRedAccent.copy(alpha = 0.28f + reactiveBoost * 0.15f),
                CyberOrange.copy(alpha = 0.12f),
                Color.Transparent
            ),
            center = center,
            radius = maxRadius * 1.05f
        ),
        center = center,
        radius = maxRadius * 1.05f
    )

    // 2. Multi-tier Concentric Neon Circular Outlines
    // Outermost Sharp Boundary Ring
    drawCircle(
        color = CyberRedAccent.copy(alpha = 0.45f + reactiveBoost * 0.2f),
        center = center,
        radius = maxRadius,
        style = Stroke(width = 1.8.dp.toPx())
    )

    // Secondary Inner Precision Ring
    drawCircle(
        color = CyberOrange.copy(alpha = 0.30f),
        center = center,
        radius = maxRadius * 0.88f,
        style = Stroke(width = 1.dp.toPx())
    )

    // Orbit Track 1 (Dashed clockwise rotation)
    val dashPhase1 = (rotationAngle * 2.5f)
    drawCircle(
        color = CyberRedAccent.copy(alpha = 0.50f),
        center = center,
        radius = maxRadius * 0.80f,
        style = Stroke(
            width = 1.4.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 10f), dashPhase1)
        )
    )

    // Orbit Track 2 (Dashed counter-clockwise rotation)
    val dashPhase2 = (reverseRotationAngle * 2.0f)
    drawCircle(
        color = CyberOrange.copy(alpha = 0.45f),
        center = center,
        radius = maxRadius * 0.65f,
        style = Stroke(
            width = 1.2.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), dashPhase2)
        )
    )

    // 3. Four Cardinal Diamond Satellite Nodes on the outer perimeter
    val radRotation = Math.toRadians(rotationAngle.toDouble())
    for (i in 0 until 4) {
        val angle = radRotation + (i * Math.PI / 2.0)
        val x = center.x + maxRadius * cos(angle).toFloat()
        val y = center.y + maxRadius * sin(angle).toFloat()

        // Glow halo
        drawCircle(
            color = CyberRedAccent.copy(alpha = 0.6f),
            center = Offset(x, y),
            radius = 5.dp.toPx() * (1f + reactiveBoost * 0.4f)
        )
        // Diamond solid core
        drawCircle(
            color = Color(0xFFFCA5A5),
            center = Offset(x, y),
            radius = 2.5.dp.toPx()
        )
    }

    // 4. Gemini High-Density Particle Stream (Outer Orbit: 24 Particles)
    val outerParticleCount = 24
    for (i in 0 until outerParticleCount) {
        val baseAngle = (i * 2.0 * Math.PI / outerParticleCount) + radRotation
        // Add subtle wave oscillation to radius
        val waveOffset = sin(baseAngle * 3 + rotationAngle * 0.05).toFloat() * 4.dp.toPx() * breathScale
        val orbitR = (maxRadius * 0.80f) + waveOffset

        val px = center.x + orbitR * cos(baseAngle).toFloat()
        val py = center.y + orbitR * sin(baseAngle).toFloat()

        val isAccent = i % 4 == 0
        val particleColor = if (isAccent) {
            if (isListening) CyberOrange else CyberRedAccent
        } else {
            CyberRedAccent.copy(alpha = 0.75f)
        }

        // Particle Head Glow
        drawCircle(
            color = particleColor.copy(alpha = 0.35f),
            center = Offset(px, py),
            radius = (if (isAccent) 4.5.dp else 3.dp).toPx() * (1f + reactiveBoost * 0.3f)
        )
        // Particle Core Dot
        drawCircle(
            color = if (isAccent) CyberWhite else particleColor,
            center = Offset(px, py),
            radius = (if (isAccent) 2.2.dp else 1.5.dp).toPx()
        )

        // Trailing particle comet effect
        val trailAngle = baseAngle - 0.08
        val tx = center.x + orbitR * cos(trailAngle).toFloat()
        val ty = center.y + orbitR * sin(trailAngle).toFloat()
        drawCircle(
            color = particleColor.copy(alpha = 0.30f),
            center = Offset(tx, ty),
            radius = 1.2.dp.toPx()
        )
    }

    // 5. Middle Swirling Particle Stream (18 Counter-Rotating Particles)
    val revRadRotation = Math.toRadians(reverseRotationAngle.toDouble())
    val midParticleCount = 18
    for (i in 0 until midParticleCount) {
        val baseAngle = (i * 2.0 * Math.PI / midParticleCount) + revRadRotation
        val orbitR = maxRadius * 0.65f

        val px = center.x + orbitR * cos(baseAngle).toFloat()
        val py = center.y + orbitR * sin(baseAngle).toFloat()

        val isAmber = i % 3 == 0
        val pColor = if (isAmber) CyberOrange else CyberRedAccent

        drawCircle(
            color = pColor.copy(alpha = 0.8f),
            center = Offset(px, py),
            radius = (if (isAmber) 2.0.dp else 1.4.dp).toPx() * (1f + reactiveBoost * 0.2f)
        )
    }

    // 6. Inner Vortex Particles (12 Orbiting Core Sparks)
    val innerCount = 12
    for (i in 0 until innerCount) {
        val baseAngle = (i * 2.0 * Math.PI / innerCount) + (radRotation * 1.5)
        val orbitR = maxRadius * 0.45f * (1f + reactiveBoost * 0.2f)

        val px = center.x + orbitR * cos(baseAngle).toFloat()
        val py = center.y + orbitR * sin(baseAngle).toFloat()

        drawCircle(
            color = if (i % 2 == 0) CyberWhite.copy(alpha = 0.9f) else CyberRedAccent,
            center = Offset(px, py),
            radius = 1.6.dp.toPx()
        )
    }

    // 7. Central Deep Glowing Radial Nebula Nucleus (Sound Reactive Core)
    val coreGlowRadius = (maxRadius * (0.35f + reactiveBoost * 0.25f)).coerceAtLeast(10f)
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                (if (isListening) CyberOrange else CyberRedAccent).copy(alpha = 0.75f),
                CyberRedAccent.copy(alpha = 0.35f),
                Color.Transparent
            ),
            center = center,
            radius = coreGlowRadius * 1.4f
        ),
        center = center,
        radius = coreGlowRadius * 1.3f
    )

    // Inner bright solid nucleus
    val innerCoreRadius = (maxRadius * (0.16f + reactiveBoost * 0.12f)).coerceAtLeast(6f)
    drawCircle(
        color = (if (isListening) CyberOrange else CyberRedAccent).copy(alpha = 0.90f),
        center = center,
        radius = innerCoreRadius
    )

    // White hot center focal dot
    drawCircle(
        color = CyberWhite,
        center = center,
        radius = innerCoreRadius * 0.35f
    )
}
