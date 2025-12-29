package com.focusstreak.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

// Reusable Confetti Animation
@Composable
fun ConfettiAnimation(
    modifier: Modifier = Modifier,
    durationMillis: Int = 3000
) {
    val particles = remember {
        List(100) { ConfettiParticle() }
    }

    val transition = rememberInfiniteTransition()
    // We actually want a one-shot animation usually, but infinite is easier to setup for continuous flow.
    // For a "Burst", we can just use a LaunchedEffect to drive a single Animatable.

    val progress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis, easing = LinearEasing)
        )
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        particles.forEach { particle ->
            particle.update(progress.value, width, height)
            drawParticle(particle)
        }
    }
}

private class ConfettiParticle {
    val startX = Random.nextFloat()
    val startY = Random.nextFloat() * 0.5f - 0.5f // Start above screen or top half
    val endX = startX + (Random.nextFloat() - 0.5f) * 0.5f
    val endY = 1.2f // Fall below screen

    val color = listOf(
        Color(0xFFFF5722), // Orange
        Color(0xFF7000FF), // Purple
        Color(0xFF00C853), // Green
        Color(0xFFFFD600), // Yellow
        Color(0xFF2962FF)  // Blue
    ).random()

    val rotationSpeed = (Random.nextFloat() - 0.5f) * 20f
    val size = Random.nextFloat() * 10f + 5f
    val phase = Random.nextFloat() * 2 * PI

    var currentX = 0f
    var currentY = 0f
    var currentRotation = 0f

    fun update(progress: Float, width: Float, height: Float) {
        // Simple linear interpolation with some noise
        // Recalculate based on progress to avoid statefulness issues in DrawScope if possible,
        // but here we are just mapping 0..1 to positions

        // Add some "gravity" curve
        val time = progress

        // X follows linear + sine wave for flutter
        currentX = (startX + (endX - startX) * time) * width + (sin(time * 10 + phase).toFloat() * 20f)

        // Y follows gravity (accelerating)
        currentY = (startY + (endY - startY) * time * time) * height

        currentRotation = rotationSpeed * time * 360
    }
}

private fun DrawScope.drawParticle(particle: ConfettiParticle) {
    // Simple circle or rect
    drawCircle(
        color = particle.color,
        center = Offset(particle.currentX, particle.currentY),
        radius = particle.size
    )
}
