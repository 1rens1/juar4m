package com.rensdev.juar4m.ui.components.controller

import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rensdev.juar4m.MainViewModel
import com.rensdev.juar4m.preferences.RangeState
import com.rensdev.juar4m.preferences.UserPreferencesDefaults
import com.rensdev.juar4m.preferences.collectAsStateOrFallback
import com.rensdev.juar4m.ui.theme.Black
import com.rensdev.juar4m.ui.theme.GeistMonoFontFamily
import com.rensdev.juar4m.ui.theme.Theme
import com.rensdev.juar4m.ui.theme.Typography
import com.rensdev.juar4m.ui.theme.White

@Composable
fun SpeedSection(viewModel: MainViewModel?, modifier: Modifier = Modifier) {
	val vibrator = viewModel?.vibrator
	val commandController = viewModel?.commandController
	val speedRange by viewModel?.speedRange.collectAsStateOrFallback(UserPreferencesDefaults.SPEED_RANGE)
	val speedState by viewModel?.speedState.collectAsStateOrFallback(UserPreferencesDefaults.SPEED_STATE)

	Row(
		modifier = modifier,
		verticalAlignment = Alignment.CenterVertically,
		horizontalArrangement = Arrangement.spacedBy(8.dp)
	) {
		Text(
			"Speed",
			modifier = Modifier
				.vertical()
				.rotate(-90f),
			color = White.copy(alpha = 0.5f),
			style = Typography.labelLarge
		)
		Column(
			verticalArrangement = Arrangement.spacedBy(8.dp)
		) {
			Surface(
				modifier = Modifier
					.size(ControllerUI.SPEED_BUTTON_SIZE)
					.border(
						2.dp,
						ControllerUI.SPEED_HIGH_COLOR.copy(alpha = 0.25f),
						RoundedCornerShape(ControllerUI.BUTTON_ROUNDNESS)
					)
					.pointerInput(Unit) {
						awaitEachGesture {
							awaitFirstDown()
							if (speedState != RangeState.HIGH) {
								viewModel?.setSpeedState(RangeState.HIGH)
								commandController?.setSpeed(speedRange.endInclusive.toInt())
								vibrator?.strongClick()
							}
							waitForUpOrCancellation()
						}
					},
				shape = RoundedCornerShape(ControllerUI.BUTTON_ROUNDNESS),
				color = if (speedState == RangeState.HIGH) ControllerUI.SPEED_HIGH_COLOR else ControllerUI.SPEED_INACTIVE_COLOR,
			) {
				Box(
					modifier = Modifier.fillMaxSize(),
					contentAlignment = Alignment.Center
				) {
					Text(
						"${speedRange.endInclusive.toInt()}",
						fontFamily = GeistMonoFontFamily,
						color = Black,
						fontWeight = FontWeight.Bold,
						letterSpacing = 1.sp
					)
				}
			}
			Surface(
				modifier = Modifier
					.size(ControllerUI.SPEED_BUTTON_SIZE)
					.border(
						2.dp,
						ControllerUI.SPEED_LOW_COLOR.copy(alpha = 0.25f),
						RoundedCornerShape(ControllerUI.BUTTON_ROUNDNESS)
					)
					.pointerInput(Unit) {
						awaitEachGesture {
							awaitFirstDown()
							if (speedState != RangeState.LOW) {
								viewModel?.setSpeedState(RangeState.LOW)
								commandController?.setSpeed(speedRange.start.toInt())
								vibrator?.strongClick()
							}
							waitForUpOrCancellation()
						}
					},
				shape = RoundedCornerShape(ControllerUI.BUTTON_ROUNDNESS),
				color = if (speedState == RangeState.LOW) ControllerUI.SPEED_LOW_COLOR else ControllerUI.SPEED_INACTIVE_COLOR
			) {
				Box(
					modifier = Modifier.fillMaxSize(),
					contentAlignment = Alignment.Center
				) {
					Text(
						"${speedRange.start.toInt()}",
						fontFamily = GeistMonoFontFamily,
						color = Black,
						fontWeight = FontWeight.Bold,
						letterSpacing = 1.sp
					)
				}
			}
		}
	}
}

@Preview
@Composable
fun SpeedSectionPreview() {
	Theme {
		SpeedSection(null)
	}
}
