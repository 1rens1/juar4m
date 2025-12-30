package com.rensdev.juar4m.ui.components

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rensdev.juar4m.MainViewModel
import com.rensdev.juar4m.preferences.collectAsStateOrFallback
import com.rensdev.juar4m.stopwatch.formatStopwatch
import com.rensdev.juar4m.ui.theme.Blue
import com.rensdev.juar4m.ui.theme.GeistMonoFontFamily
import com.rensdev.juar4m.ui.theme.Red
import com.rensdev.juar4m.ui.theme.Typography
import com.rensdev.juar4m.ui.theme.White

@Composable
fun Stopwatch(modifier: Modifier = Modifier, viewModel: MainViewModel?) {
	val isStopwatchRunning by viewModel?.isStopwatchRunning.collectAsStateOrFallback(false)
	val stopwatchElapsed by viewModel?.stopwatchElapsed?.collectAsState()
		?: remember { mutableLongStateOf(0L) }
	val stopwatchElapsedFormatted = formatStopwatch(stopwatchElapsed)

	var showFinishedDialog by remember { mutableStateOf(false) }

	Box(modifier = modifier) {
		Text(
			stopwatchElapsedFormatted, style = Typography.headlineLarge.copy(
				color = White.copy(alpha = 0.3f), fontFamily = GeistMonoFontFamily, letterSpacing = (-2).sp
			), modifier = Modifier.align(Alignment.BottomStart)
		)
		if (isStopwatchRunning) {
			Surface(
				color = Red,
				contentColor = White,
				shape = ButtonDefaults.shape,
				modifier = Modifier
					.size(150.dp, 45.dp)
					.align(Alignment.BottomCenter)
					.pointerInput(Unit) {
						awaitEachGesture {
							awaitFirstDown()

							viewModel?.finishStopwatch()
							showFinishedDialog = true

							waitForUpOrCancellation()
						}
					}) {
				Box(
					modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
				) {
					Text("FINISH", style = Typography.headlineSmall)
				}
			}
		} else {
			Text(
				text = "READY",
				style = Typography.headlineSmall,
				textAlign = TextAlign.Center,
				modifier = Modifier
					.size(150.dp, 45.dp)
					.align(Alignment.BottomCenter)
					.wrapContentHeight(align = Alignment.CenterVertically)
			)
		}
	}

	if (showFinishedDialog) {
		AlertDialog(onDismissRequest = {}, title = {
			Text(
				"Stopwatch finished!"
			)
		}, text = {
			Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
				Text("Your time:", color = White.copy(alpha = 0.5f))
				Text(
					stopwatchElapsedFormatted, style = Typography.headlineLarge.copy(
						fontFamily = GeistMonoFontFamily, letterSpacing = (-2).sp
					)
				)
			}
		}, confirmButton = {
			TextButton(onClick = {
				showFinishedDialog = false
				viewModel?.resetStopwatch()
			}, colors = ButtonDefaults.textButtonColors(containerColor = Blue.copy(alpha = 0.2f))) {
				Text("Reset & Close")
			}
		})
	}
}
