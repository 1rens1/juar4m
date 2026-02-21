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
	val speedRange by viewModel?.speedRange.collectAsStateOrFallback(UserPreferencesDefaults.SPEED_RANGE)
	val speedState by viewModel?.speedState.collectAsStateOrFallback(UserPreferencesDefaults.SPEED_STATE)
	val commandController = viewModel?.commandController
	val vibrator = viewModel?.vibrator

	Row(
		modifier = modifier,
		verticalAlignment = Alignment.CenterVertically,
		horizontalArrangement = Arrangement.spacedBy(8.dp)
	) {

		Text(
			text = "Speed",
			modifier = Modifier
				.vertical()
				.rotate(-90f),
			color = White.copy(alpha = 0.5f),
			style = Typography.labelLarge
		)

		Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
			SpeedButton(
				value = speedRange.endInclusive.toInt(),
				isActive = speedState == RangeState.HIGH,
				activeColor = ControllerUI.SPEED_HIGH_COLOR,
				onClick = {
					if (speedState != RangeState.HIGH) {
						viewModel?.setSpeedState(RangeState.HIGH)
						commandController?.setSpeed(speedRange.endInclusive.toInt())
						vibrator?.strongClick()
					}
				}
			)

			SpeedButton(
				value = speedRange.start.toInt(),
				isActive = speedState == RangeState.LOW,
				activeColor = ControllerUI.SPEED_LOW_COLOR,
				onClick = {
					if (speedState != RangeState.LOW) {
						viewModel?.setSpeedState(RangeState.LOW)
						commandController?.setSpeed(speedRange.start.toInt())
						vibrator?.strongClick()
					}
				}
			)
		}
	}
}

@Composable
private fun SpeedButton(
	value: Int,
	isActive: Boolean,
	activeColor: androidx.compose.ui.graphics.Color,
	onClick: () -> Unit
) {
	Surface(
		modifier = Modifier
			.size(ControllerUI.SPEED_BUTTON_SIZE)
			.border(
				2.dp,
				activeColor.copy(alpha = 0.25f),
				RoundedCornerShape(ControllerUI.BUTTON_ROUNDNESS)
			)
			.pointerInput(Unit) {
				awaitEachGesture {
					awaitFirstDown()
					onClick()
					waitForUpOrCancellation()
				}
			},
		shape = RoundedCornerShape(ControllerUI.BUTTON_ROUNDNESS),
		color = if (isActive) activeColor else ControllerUI.SPEED_INACTIVE_COLOR
	) {
		Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
			Text(
				text = "$value",
				fontFamily = GeistMonoFontFamily,
				color = Black,
				fontWeight = FontWeight.Bold,
				letterSpacing = 1.sp
			)
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
