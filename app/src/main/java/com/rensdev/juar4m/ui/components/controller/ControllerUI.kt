package com.rensdev.juar4m.ui.components.controller

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.rensdev.juar4m.ui.theme.Black
import com.rensdev.juar4m.ui.theme.Blue
import com.rensdev.juar4m.ui.theme.Cyan
import com.rensdev.juar4m.ui.theme.Magenta
import com.rensdev.juar4m.ui.theme.Red
import com.rensdev.juar4m.ui.theme.Typography
import com.rensdev.juar4m.ui.theme.White
import com.rensdev.juar4m.ui.theme.Yellow

data object ControllerUI {
	val BUTTON_SIZE = 85.dp
	val BUTTON_ROUNDNESS = BUTTON_SIZE * 0.1f
	val ICON_SIZE = BUTTON_SIZE * 0.5f
	val DPAD_GAP = 0.dp
	val DPAD_COLOR = White
	val DPAD_CONTENT_COLOR = Black
	val DPAD_ACTIVE_COLOR = Red
	val DPAD_ACTIVE_CONTENT_COLOR = White

	val NUDGE_THINNESS = BUTTON_SIZE * 0.35f
	val NUDGE_PADDING = NUDGE_THINNESS * 0.4f
	val NUDGE_COLOR = Blue.copy(alpha = 0.5f)
	val NUDGE_CONTENT_COLOR = White
	val NUDGE_ACTIVE_COLOR = Color(0xFFFF9800)
	val NUDGE_ACTIVE_CONTENT_COLOR = White
	const val NUDGE_FLASH_DURATION_MILLIS = 50L

	val SERVO_CONTAINER_HEIGHT = BUTTON_SIZE * 2.6f
	val SERVO_TOGGLE_LOW_COLOR = Cyan.copy(alpha = 0.5f)
	val SERVO_TOGGLE_HIGH_COLOR = Magenta.copy(alpha = 0.5f)
	val SERVO_TOGGLE_CONTENT_COLOR = White
	val SERVO_TEXT_STYLE = Typography.headlineSmall

	val SERVO_C_UP_COLOR = Red
	val SERVO_C_DOWN_COLOR = Yellow

}
