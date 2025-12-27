package com.rensdev.juar4m.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.rensdev.juar4m.ui.components.controller.ServoState
import kotlinx.coroutines.flow.map

val Context.userPreferences: DataStore<Preferences> by preferencesDataStore("user_preferences")

class UserPreferences(private val context: Context) {
	companion object {
		val LAST_DEVICE_ADDRESS = stringPreferencesKey("bluetooth.last_device_address")
		// val APP_VIBRATIONS_ENABLED = booleanPreferencesKey("app.vibrations_enabled")
		// val APP_DISCONNECT_LOCK_ENABLED = booleanPreferencesKey("app.disconnect_lock_enabled")
		// val APP_STOPWATCH_MODE_ENABLED = booleanPreferencesKey("app.stopwatch_mode_enabled")

		// Servo A
		val SERVO_A_RANGE_LOW = floatPreferencesKey("servo.a.range_low")
		val SERVO_A_RANGE_HIGH = floatPreferencesKey("servo.a.range_high")
		val SERVO_A_STATE = booleanPreferencesKey("servo.a.state") // true = HIGH, false = LOW

		// Servo B
		val SERVO_B_RANGE_LOW = floatPreferencesKey("servo.b.range_low")
		val SERVO_B_RANGE_HIGH = floatPreferencesKey("servo.b.range_high")
		val SERVO_B_STATE = booleanPreferencesKey("servo.b.state")
	}

	private fun <T> get(key: Preferences.Key<T>, defaultValue: Nothing?) =
		context.userPreferences.data.map { it[key] ?: defaultValue }

	private fun <T> get(key: Preferences.Key<T>, defaultValue: T) =
		context.userPreferences.data.map { it[key] ?: defaultValue }

	private suspend fun <T> set(key: Preferences.Key<T>, value: T?) {
		context.userPreferences.edit { prefs ->
			if (value == null) {
				prefs.remove(key)
			} else {
				prefs[key] = value
			}
		}
	}

	val lastDeviceAddress = get(LAST_DEVICE_ADDRESS, UserPreferencesDefaults.LAST_DEVICE_ADDRESS)
	suspend fun setLastDeviceAddress(name: String?) = set(LAST_DEVICE_ADDRESS, name)
	// MOVED TO MainViewModel.kt
	// val vibrationsEnabled =
	// 	get(APP_VIBRATIONS_ENABLED, UserPreferencesDefaults.APP_VIBRATIONS_ENABLED)
	//
	// suspend fun setVibrationsEnabled(enabled: Boolean) = set(APP_VIBRATIONS_ENABLED, enabled)
	// val disconnectLockEnabled =
	// 	get(APP_DISCONNECT_LOCK_ENABLED, UserPreferencesDefaults.APP_DISCONNECT_LOCK_ENABLED)
	//
	// suspend fun setDisconnectLockEnabled(enabled: Boolean) = set(APP_DISCONNECT_LOCK_ENABLED, enabled)
	// val stopwatchModeEnabled =
	// 	get(APP_STOPWATCH_MODE_ENABLED, UserPreferencesDefaults.APP_STOPWATCH_MODE_ENABLED)
	//
	// suspend fun setStopwatchModeEnabled(enabled: Boolean) = set(APP_STOPWATCH_MODE_ENABLED, enabled)

	// Servo A
	val servoAState = get(
		SERVO_A_STATE, UserPreferencesDefaults.SERVO_A_STATE == ServoState.HIGH
	).map { isHigh -> if (isHigh) ServoState.HIGH else ServoState.LOW }

	suspend fun setServoAState(state: ServoState) = set(SERVO_A_STATE, state == ServoState.HIGH)

	val servoARange = context.userPreferences.data.map { prefs ->
		val start = prefs[SERVO_A_RANGE_LOW] ?: UserPreferencesDefaults.SERVO_A_RANGE.start
		val end = prefs[SERVO_A_RANGE_HIGH] ?: UserPreferencesDefaults.SERVO_A_RANGE.endInclusive
		start..end
	}

	suspend fun setServoARange(range: ClosedFloatingPointRange<Float>) {
		context.userPreferences.edit { prefs ->
			prefs[SERVO_A_RANGE_LOW] = range.start
			prefs[SERVO_A_RANGE_HIGH] = range.endInclusive
		}
	}

	// Servo B
	val servoBState = get(
		SERVO_B_STATE, UserPreferencesDefaults.SERVO_B_STATE == ServoState.HIGH
	).map { isHigh -> if (isHigh) ServoState.HIGH else ServoState.LOW }

	suspend fun setServoBState(state: ServoState) = set(SERVO_B_STATE, state == ServoState.HIGH)

	val servoBRange = context.userPreferences.data.map { prefs ->
		val start = prefs[SERVO_B_RANGE_LOW] ?: UserPreferencesDefaults.SERVO_B_RANGE.start
		val end = prefs[SERVO_B_RANGE_HIGH] ?: UserPreferencesDefaults.SERVO_B_RANGE.endInclusive
		start..end
	}

	suspend fun setServoBRange(range: ClosedFloatingPointRange<Float>) {
		context.userPreferences.edit { prefs ->
			prefs[SERVO_B_RANGE_LOW] = range.start
			prefs[SERVO_B_RANGE_HIGH] = range.endInclusive
		}
	}
}
