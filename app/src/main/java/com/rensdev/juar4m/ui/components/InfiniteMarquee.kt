package com.rensdev.juar4m.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

private const val FPS = 15
private const val MILLIS_PER_FRAME = 1000L / FPS

@Composable
fun InfiniteMarquee(
	text: String,
	textStyle: TextStyle,
	modifier: Modifier = Modifier,
	gap: Float = 128f,
	speed: Float = 16f
) {
	val textMeasurer = rememberTextMeasurer()
	val textLayout = remember(text, textStyle) {
		textMeasurer.measure(text, style = textStyle)
	}

	val textWidth = textLayout.size.width.toFloat()
	val textHeight = textLayout.size.height
	val period = textWidth + gap
	val heightDp = with(LocalDensity.current) { textHeight.toDp() }

	var offset by remember { mutableFloatStateOf(0f) }

	LaunchedEffect(text, speed) {
		var lastTime = withFrameMillis { it }
		while (isActive) {
			val frameTime = withFrameMillis { it }
			val delta = (frameTime - lastTime) / 1000f
			offset = (offset + delta * speed) % period
			delay(MILLIS_PER_FRAME)
			lastTime = frameTime
		}
	}

	Canvas(
		modifier = modifier
			.fillMaxWidth()
			.height(heightDp)
			.clipToBounds()
	) {
		var currentX = -offset
		while (currentX < size.width) {
			drawText(
				textLayoutResult = textLayout, topLeft = Offset(currentX, 0f)
			)
			currentX += period
		}
	}
}
