package com.rensdev.juar4m.preferences

object UserPreferencesDefaults {
	val LAST_DEVICE_ADDRESS = null
	const val APP_MARQUEE_ANIMATION_ENABLED = true
	const val APP_CORNER_DPAD_BUTTONS_ENABLED = true
	const val APP_VIBRATIONS_ENABLED = true
	const val APP_DISCONNECT_LOCK_ENABLED = true
	const val APP_STOPWATCH_MODE_ENABLED = false
	val SPEED_RANGE = 200f..255f
	val SPEED_STATE = RangeState.HIGH
	val SERVO_A_RANGE = 0f..90f
	val SERVO_A_STATE = RangeState.LOW
	val SERVO_B_RANGE = 0f..180f
	val SERVO_B_STATE = RangeState.LOW
	const val SERVO_C_INVERTED = false
}
