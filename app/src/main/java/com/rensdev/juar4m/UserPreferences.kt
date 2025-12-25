package com.rensdev.juar4m

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.rensdev.juar4m.ui.components.controller.ServoState
import com.rensdev.juar4m.ui.components.controller.ToggleServoDefaults
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.userPreferences: DataStore<Preferences> by preferencesDataStore("user_preferences")

class UserPreferences(private val context: Context) {
	companion object {
		val LAST_DEVICE_ADDRESS_KEY = stringPreferencesKey("last_device_address")
		val VIBRATIONS_ENABLED_KEY = booleanPreferencesKey("vibrations_enabled")
		val TIMER_MODE_ENABLED_KEY = booleanPreferencesKey("timer_mode_enabled")

		val SERVO_A_RANGE_LOW_KEY = floatPreferencesKey("servo_a_range_low")
		val SERVO_A_RANGE_HIGH_KEY = floatPreferencesKey("servo_a_range_high")
		val SERVO_B_RANGE_LOW_KEY = floatPreferencesKey("servo_b_range_low")
		val SERVO_B_RANGE_HIGH_KEY = floatPreferencesKey("servo_b_range_high")

		// LOW = false, HIGH = true
		val SERVO_A_STATE_KEY = booleanPreferencesKey("servo_a_state")
		val SERVO_B_STATE_KEY = booleanPreferencesKey("servo_b_state")
	}

	val lastDeviceAddress: Flow<String?> =
		context.userPreferences.data.map { it[LAST_DEVICE_ADDRESS_KEY] }

	suspend fun saveLastDeviceAddress(name: String?) {
		context.userPreferences.edit { prefs ->
			if (name == null) {
				prefs.remove(LAST_DEVICE_ADDRESS_KEY)
			} else {
				prefs[LAST_DEVICE_ADDRESS_KEY] = name
			}
		}
	}

	val vibrationsEnabled: Flow<Boolean> =
		context.userPreferences.data.map { it[VIBRATIONS_ENABLED_KEY] ?: true }
	val timerModeEnabled: Flow<Boolean> =
		context.userPreferences.data.map { it[TIMER_MODE_ENABLED_KEY] ?: false }

	suspend fun saveVibrationsEnabled(enabled: Boolean) {
		context.userPreferences.edit { prefs ->
			prefs[VIBRATIONS_ENABLED_KEY] = enabled
		}
	}

	suspend fun saveTimerModeEnabled(enabled: Boolean) {
		context.userPreferences.edit { prefs ->
			prefs[TIMER_MODE_ENABLED_KEY] = enabled
		}
	}

//	Servo A

	val servoARange: Flow<ClosedFloatingPointRange<Float>> =
		context.userPreferences.data.map { prefs ->
			val start = prefs[SERVO_A_RANGE_LOW_KEY] ?: ToggleServoDefaults.servoA.range.start
			val end = prefs[SERVO_A_RANGE_HIGH_KEY] ?: ToggleServoDefaults.servoA.range.endInclusive
			start..end
		}
	val servoAState: Flow<ServoState> = context.userPreferences.data.map { prefs ->
		val isHigh = prefs[SERVO_A_STATE_KEY] ?: (ToggleServoDefaults.servoA.state == ServoState.HIGH)
		if (isHigh) ServoState.HIGH else ServoState.LOW
	}

	suspend fun saveServoARange(range: ClosedFloatingPointRange<Float>) {
		context.userPreferences.edit { prefs ->
			prefs[SERVO_A_RANGE_LOW_KEY] = range.start
			prefs[SERVO_A_RANGE_HIGH_KEY] = range.endInclusive
		}
	}

	suspend fun saveServoAState(state: ServoState) {
		context.userPreferences.edit { prefs ->
			prefs[SERVO_A_STATE_KEY] = state == ServoState.HIGH
		}
	}

	//	Servo B
	val servoBRange: Flow<ClosedFloatingPointRange<Float>> =
		context.userPreferences.data.map { prefs ->
			val start = prefs[SERVO_B_RANGE_LOW_KEY] ?: ToggleServoDefaults.servoB.range.start
			val end = prefs[SERVO_B_RANGE_HIGH_KEY] ?: ToggleServoDefaults.servoB.range.endInclusive
			start..end
		}
	val servoBState: Flow<ServoState> = context.userPreferences.data.map { prefs ->
		val isHigh = prefs[SERVO_B_STATE_KEY] ?: (ToggleServoDefaults.servoB.state == ServoState.HIGH)
		if (isHigh) ServoState.HIGH else ServoState.LOW
	}

	suspend fun saveServoBRange(range: ClosedFloatingPointRange<Float>) {
		context.userPreferences.edit { prefs ->
			prefs[SERVO_B_RANGE_LOW_KEY] = range.start
			prefs[SERVO_B_RANGE_HIGH_KEY] = range.endInclusive
		}
	}

	suspend fun saveServoBState(state: ServoState) {
		context.userPreferences.edit { prefs ->
			prefs[SERVO_B_STATE_KEY] = state == ServoState.HIGH
		}
	}
}
