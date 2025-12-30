package com.rensdev.juar4m.preferences

import com.rensdev.juar4m.ui.components.controller.ServoState

object UserPreferencesDefaults {
	val LAST_DEVICE_ADDRESS = null
	const val APP_VIBRATIONS_ENABLED = true
	const val APP_DISCONNECT_LOCK_ENABLED = true
	const val APP_STOPWATCH_MODE_ENABLED = false
	val SERVO_A_RANGE = 0f..90f
	val SERVO_A_STATE = ServoState.LOW
	val SERVO_B_RANGE = 0f..180f
	val SERVO_B_STATE = ServoState.LOW
	val SERVO_C_INVERTED = false
}
