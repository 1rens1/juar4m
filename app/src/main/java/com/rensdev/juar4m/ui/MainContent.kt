package com.rensdev.juar4m.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rensdev.juar4m.MainViewModel
import com.rensdev.juar4m.bluetooth.BluetoothStatus
import com.rensdev.juar4m.preferences.UserPreferencesDefaults
import com.rensdev.juar4m.preferences.collectAsStateOrFallback
import com.rensdev.juar4m.ui.components.BatteryIndicator
import com.rensdev.juar4m.ui.components.BluetoothButton
import com.rensdev.juar4m.ui.components.ClockText
import com.rensdev.juar4m.ui.components.DndIndicator
import com.rensdev.juar4m.ui.components.InfiniteMarquee
import com.rensdev.juar4m.ui.components.MenuButton
import com.rensdev.juar4m.ui.components.Stopwatch
import com.rensdev.juar4m.ui.components.controller.DPadSection
import com.rensdev.juar4m.ui.components.controller.ServoSection
import com.rensdev.juar4m.ui.theme.GeistFontFamily
import com.rensdev.juar4m.ui.theme.GeistMonoFontFamily
import com.rensdev.juar4m.ui.theme.Gray
import com.rensdev.juar4m.ui.theme.Theme
import com.rensdev.juar4m.ui.theme.White
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.withTimeoutOrNull

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun MainContent(
	viewModel: MainViewModel? = null,
	bluetoothStatus: BluetoothStatus? = null,
	content: (@Composable () -> Unit)? = null
) {
	val snackbarHostState = remember { SnackbarHostState() }
	@Suppress("LocalVariableName") val _bluetoothStatus by viewModel?.bluetoothStatus.collectAsStateOrFallback(
		bluetoothStatus ?: BluetoothStatus.Disconnected
	)
	val marqueeAnimationEnabled by viewModel?.marqueeAnimationEnabled.collectAsStateOrFallback(
		UserPreferencesDefaults.APP_MARQUEE_ANIMATION_ENABLED
	)
	val disconnectLock by viewModel?.disconnectLockEnabled.collectAsStateOrFallback(
		UserPreferencesDefaults.APP_DISCONNECT_LOCK_ENABLED
	)
	val stopwatchModeEnabled by viewModel?.stopwatchModeEnabled.collectAsStateOrFallback(
		UserPreferencesDefaults.APP_STOPWATCH_MODE_ENABLED
	)

	LaunchedEffect(viewModel) {
		viewModel?.userMessages?.collectLatest {
			withTimeoutOrNull(1250) {
				snackbarHostState.showSnackbar(
					message = it.message,
					withDismissAction = it.withDismissAction,
					duration = SnackbarDuration.Indefinite
				)
			}
		}
	}

	Theme {
		Box(modifier = Modifier.fillMaxSize()) {
			Scaffold(
				modifier = Modifier
					.fillMaxSize()
					.windowInsetsPadding(WindowInsets.displayCutout),
			) { _ ->
				InfiniteMarquee(
					modifier = Modifier.align(Alignment.BottomCenter), text = "JUARA!", textStyle = TextStyle(
						color = White.copy(alpha = 0.1f),
						fontFamily = GeistFontFamily,
						fontWeight = FontWeight.Black,
						fontStyle = FontStyle.Italic
					), playing = marqueeAnimationEnabled
				)
				// Controller Screen
				Row(
					modifier = Modifier
						.fillMaxSize()
						.padding(horizontal = 16.dp)
				) {
					ServoSection(
						viewModel = viewModel, modifier = Modifier
							.weight(1f)
							.fillMaxSize()
					)
					DPadSection(
						viewModel = viewModel, modifier = Modifier
							.weight(1f)
							.fillMaxSize()
					)
				}

				// Disabled overlay
				if (disconnectLock && (_bluetoothStatus != BluetoothStatus.Connected)) {
					Box(
						modifier = Modifier
							.fillMaxSize()
							.clickable(
								indication = null, interactionSource = remember { MutableInteractionSource() }) { }
							.background(Color(0xFF1A1A1A).copy(0.7f)))
				}

				Row(
					modifier = Modifier
						.fillMaxWidth()
						.padding(24.dp, 4.dp),
					horizontalArrangement = Arrangement.SpaceBetween,
					verticalAlignment = Alignment.Top
				) {
					Row(
						horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End),
						verticalAlignment = Alignment.Top
					) {
						MenuButton(
							modifier = Modifier, viewModel = viewModel
						)
						Box(modifier = Modifier.height(16.dp), contentAlignment = Alignment.Center) {
							ClockText {
								Text(
									text = it,
									fontSize = 12.sp,
									lineHeight = 12.sp,
									color = Gray,
									fontFamily = GeistMonoFontFamily
								)
							}
						}
					}

					Row(
						horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End),
						verticalAlignment = Alignment.CenterVertically
					) {
						DndIndicator()
						BatteryIndicator()
					}
				}
				BluetoothButton(
					viewModel = viewModel,
					bluetoothStatus = _bluetoothStatus,
					modifier = Modifier
						.align(Alignment.TopCenter)
						.padding(top = 8.dp)
				)

				if (stopwatchModeEnabled) {
					Stopwatch(
						viewModel = viewModel,
						modifier = Modifier
							.align(Alignment.BottomCenter)
							.fillMaxWidth()
							.padding(16.dp, 24.dp)
					)
				}

				content?.invoke()
			}
			SnackbarHost(
				hostState = snackbarHostState,
				modifier = Modifier
					.align(Alignment.BottomStart)
					.padding(top = 16.dp, end = 16.dp)
					.safeDrawingPadding()
			) { data ->
				Snackbar(
					snackbarData = data,
					modifier = Modifier.widthIn(max = 400.dp),
					containerColor = Color(0xFF17172B),
					contentColor = White,
					dismissActionContentColor = White,
					shape = RoundedCornerShape(12.dp)
				)
			}
		}
	}
}

@Preview(widthDp = 915, heightDp = 412)
@Composable
fun MainContentPreview() {
	MainContent(bluetoothStatus = BluetoothStatus.Connected)
}

//@Preview(widthDp = 960, heightDp = 580)
@Suppress("unused")
@Composable
fun MainContentDisconnectedPreview() {
	MainContent(bluetoothStatus = BluetoothStatus.Disconnected)
}
