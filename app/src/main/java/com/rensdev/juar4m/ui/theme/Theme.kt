package com.rensdev.juar4m.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private fun blackToWhite(whiteRatio: Float): Color =
	colorMix(Color.Black, Color.White, whiteRatio)

val ColorScheme = darkColorScheme(
	primary = White,
	onPrimary = Black,

	primaryContainer = White.copy(alpha = 0.2f),
	onPrimaryContainer = White,

	secondary = White,
	onSecondary = Black,
	secondaryContainer = White,
	onSecondaryContainer = Black,

	tertiary = White,
	onTertiary = Black,

	background = Black,
	onBackground = White,

	surface = Black,
	onSurface = White,

	surfaceVariant = Black,
	onSurfaceVariant = blackToWhite(0.75f),

	surfaceTint = Black,
	surfaceContainerLowest = blackToWhite(0.05f),
	surfaceContainerLow = blackToWhite(0.075f),
	surfaceContainer = blackToWhite(0.10f),
	surfaceContainerHigh = blackToWhite(0.125f),
	surfaceContainerHighest = blackToWhite(0.15f),

	outline = White,
	outlineVariant = White,

	inversePrimary = Black,
	inverseSurface = White,
	inverseOnSurface = Black,

	error = White,
	onError = Black,
	errorContainer = White,
	onErrorContainer = Black,

	scrim = Black.copy(alpha = 0.6f)
)

@Composable
fun Theme(
	content: @Composable () -> Unit
) {
	MaterialTheme(
		colorScheme = ColorScheme, typography = Typography, content = content
	)
}
