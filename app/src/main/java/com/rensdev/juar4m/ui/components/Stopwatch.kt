package com.rensdev.juar4m.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.rensdev.juar4m.MainViewModel
import com.rensdev.juar4m.preferences.collectAsStateOrFallback
import com.rensdev.juar4m.stopwatch.formatStopwatch
import com.rensdev.juar4m.ui.theme.Black
import com.rensdev.juar4m.ui.theme.Blue
import com.rensdev.juar4m.ui.theme.ColorScheme
import com.rensdev.juar4m.ui.theme.GeistMonoFontFamily
import com.rensdev.juar4m.ui.theme.Green
import com.rensdev.juar4m.ui.theme.Red
import com.rensdev.juar4m.ui.theme.Theme
import com.rensdev.juar4m.ui.theme.Typography
import com.rensdev.juar4m.ui.theme.White
import com.rensdev.juar4m.ui.theme.Yellow
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.math.absoluteValue

@Composable
fun Stopwatch(modifier: Modifier = Modifier, viewModel: MainViewModel?) {
	val isStopwatchRunning by viewModel?.isStopwatchRunning.collectAsStateOrFallback(false)
	val stopwatchElapsed by viewModel?.stopwatchElapsed?.collectAsState()
		?: remember { mutableLongStateOf(0L) }
	val stopwatchPB by viewModel?.stopwatchPB.collectAsStateOrFallback(null)

	var showFinishedDialog by remember { mutableStateOf(false) }

	Box(modifier = modifier) {
		Column(
			modifier = Modifier
				.align(Alignment.BottomStart)
				.alpha(0.3f)
		) {
			if (stopwatchPB != null) {
				if (stopwatchElapsed > 0) {
					val diff = stopwatchElapsed - stopwatchPB!!
					DiffTextDisplay(
						diff = diff, style = Typography.titleLarge
					)
				} else {
					Row(
						modifier = Modifier.alpha(0.8f),
						horizontalArrangement = Arrangement.spacedBy(8.dp),
						verticalAlignment = Alignment.CenterVertically
					) {
						Text(
							"PB: ${formatStopwatch(stopwatchPB!!)}", style = Typography.titleMedium.copy(
								fontFamily = GeistMonoFontFamily,
								letterSpacing = (-1).sp
							)
						)
						Surface(
							modifier = Modifier
								.width(48.dp)
								.height(24.dp)
								.combinedClickable(onClick = {
									viewModel?.showSnackbar("Long press to reset PB")
								}, onLongClick = {
									viewModel?.setStopwatchPB(null)
								}),
							color = White,
							contentColor = Black,
							shape = ButtonDefaults.shape
						) {
							Box(
								modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
							) {
								Text("Reset", fontSize = 12.sp)
							}
						}
					}
				}
			}

			Text(
				formatStopwatch(stopwatchElapsed), style = Typography.headlineLarge.copy(
					fontFamily = GeistMonoFontFamily, letterSpacing = (-2).sp
				)
			)
		}

		if (isStopwatchRunning) {
			FinishButton(
				modifier = Modifier.align(Alignment.BottomCenter), onFinish = {
					viewModel?.finishStopwatch()
					showFinishedDialog = true
				})
		} else {
			ReadyText(modifier = Modifier.align(Alignment.BottomCenter))
		}
	}

	if (showFinishedDialog) {
		StopwatchFinishDialog(
			stopwatchElapsed = stopwatchElapsed,
			stopwatchPB = stopwatchPB,
			onSavePB = { viewModel?.setStopwatchPB(stopwatchElapsed) },
			onResetPB = { viewModel?.setStopwatchPB(null) },
			onResetAndClose = {
				showFinishedDialog = false
				viewModel?.resetStopwatch()
			})
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StopwatchFinishDialog(
	stopwatchElapsed: Long,
	stopwatchPB: Long?,
	onSavePB: () -> Unit,
	onResetPB: () -> Unit,
	onResetAndClose: () -> Unit
) {
	val isPB = stopwatchPB == null || stopwatchElapsed < stopwatchPB
	val diff = if (stopwatchPB != null) stopwatchElapsed - stopwatchPB else 0L

	BasicAlertDialog(
		onDismissRequest = {},
		properties = DialogProperties(usePlatformDefaultWidth = false),
		modifier = Modifier
			.widthIn(min = 500.dp)
			.fillMaxWidth(0.5f)
	) {
		Surface(
			shape = RoundedCornerShape(28.dp),
			color = ColorScheme.surfaceContainerHigh,
		) {
			Column(modifier = Modifier.padding(24.dp)) {
				// Header
				Row(
					verticalAlignment = Alignment.CenterVertically,
					modifier = Modifier.padding(bottom = 16.dp)
				) {
					Text("Stopwatch finished", style = Typography.titleLarge)
					if (isPB && stopwatchPB != null) {
						Spacer(modifier = Modifier.width(8.dp))
						Surface(
							color = Yellow.copy(alpha = 0.2f), shape = RoundedCornerShape(4.dp)
						) {
							Text(
								"NEW PERSONAL BEST!",
								color = Yellow,
								style = Typography.labelSmall,
								fontWeight = FontWeight.Bold,
								modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
							)
						}
					}
				}

				// Content
				Row(
					modifier = Modifier.fillMaxWidth(),
					verticalAlignment = Alignment.Bottom,
					horizontalArrangement = Arrangement.spacedBy(8.dp)
				) {
					Text(
						formatStopwatch(stopwatchElapsed), style = Typography.headlineLarge.copy(
							fontFamily = GeistMonoFontFamily, letterSpacing = (-2).sp, color = White.copy(0.75f)
						)
					)
					if (stopwatchPB != null && diff.absoluteValue > 0L) {
						DiffTextDisplay(
							diff = diff,
							style = Typography.headlineSmall,
							modifier = Modifier.padding(bottom = 2.dp)
						)
					}
				}
				if (stopwatchPB != null) {
					Text(
						"Current PB: ${formatStopwatch(stopwatchPB)}",
						style = Typography.bodyMedium,
						color = White.copy(alpha = 0.3f),
						fontFamily = GeistMonoFontFamily
					)
				}

				Spacer(modifier = Modifier.height(24.dp))

				// Footer
				Row(
					modifier = Modifier.fillMaxWidth(),
					horizontalArrangement = Arrangement.SpaceBetween,
					verticalAlignment = Alignment.CenterVertically
				) {
					Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
						if (stopwatchPB != null) {
							TextButton(
								onClick = onResetPB,
								colors = ButtonDefaults.textButtonColors(containerColor = Red.copy(alpha = 0.1f))
							) {
								Text("Reset PB")
							}
						}

						if (isPB) {
							val blinkAlpha = remember { Animatable(0.1f) }

							LaunchedEffect(Unit) {
								delay(500)
								repeat(2) {
									blinkAlpha.animateTo(0.2f, animationSpec = tween(durationMillis = 200))
									blinkAlpha.animateTo(0.1f, animationSpec = tween(durationMillis = 200))
								}
							}

							Button(
								onClick = onSavePB, colors = ButtonDefaults.textButtonColors(
									containerColor = Green.copy(alpha = blinkAlpha.value)
								)
							) {
								Text("Save as new PB")
							}
						}
					}

					TextButton(
						onClick = onResetAndClose,
						colors = ButtonDefaults.textButtonColors(containerColor = Blue.copy(alpha = 0.2f))
					) {
						Text("Reset & Close")
					}
				}
			}
		}
	}
}

@Composable
private fun DiffTextDisplay(
	modifier: Modifier = Modifier, diff: Long, style: androidx.compose.ui.text.TextStyle
) {
	val isFaster = diff <= 0
	val color = if (isFaster) Green else Red
	val sign = if (isFaster) "-" else "+"
	val absDiff = abs(diff)

	val minutes = absDiff / 60000
	val seconds = (absDiff % 60000) / 1000
	val millis = (absDiff % 1000) / 10

	val formattedDiff = buildAnnotatedString {
		append(sign)
		if (minutes > 0) append("$minutes:")
		append(seconds.toString().padStart(2, '0'))
		append(".")
		withStyle(SpanStyle(fontSize = style.fontSize * 0.75)) {
			append(millis.toString().padStart(2, '0'))
		}
	}

	Text(
		modifier = modifier, text = formattedDiff, style = style.copy(
			color = color, fontFamily = GeistMonoFontFamily, fontWeight = FontWeight.Bold
		)
	)
}

@Composable
private fun FinishButton(modifier: Modifier = Modifier, onFinish: () -> Unit) {
	Surface(
		color = Red,
		contentColor = White,
		shape = ButtonDefaults.shape,
		modifier = modifier
			.size(150.dp, 45.dp)
			.pointerInput(Unit) {
				awaitEachGesture {
					awaitFirstDown()
					onFinish()
					waitForUpOrCancellation()
				}
			}) {
		Box(
			modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
		) {
			Text("FINISH", style = Typography.headlineSmall)
		}
	}
}

@Composable
private fun ReadyText(modifier: Modifier = Modifier) {
	Text(
		text = "READY",
		style = Typography.headlineSmall.copy(color = White),
		textAlign = TextAlign.Center,
		modifier = modifier
			.size(150.dp, 45.dp)
			.wrapContentHeight(align = Alignment.CenterVertically)
	)
}

@Preview(
	widthDp = 500, heightDp = 200, showBackground = true
)
@Composable
private fun StopwatchPreview() {
	Theme {
		Stopwatch(viewModel = null)
	}
}

@Preview
@Composable
private fun StopwatchFinishDialogPreview() {
	Theme {
		StopwatchFinishDialog(
			stopwatchElapsed = 54321,
			stopwatchPB = 123456,
			onSavePB = {},
			onResetPB = {},
			onResetAndClose = {})
	}
}
