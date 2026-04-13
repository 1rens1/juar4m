package com.rensdev.juar4m.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map

val Context.userPreferences: DataStore<Preferences> by preferencesDataStore("user_preferences")

enum class RangeState(val value: Boolean) {
	LOW(false), HIGH(true)
}

enum class ServoMode(val value: Int) {
	DISABLED(0), POSITIONAL(1), CONTINUOUS(2);

	fun label(): String = name.lowercase().replaceFirstChar { it.uppercase() }

	companion object {
		fun fromInt(value: Int) = entries.find { it.value == value } ?: POSITIONAL
	}
}

class UserPreferences(private val context: Context) {
	companion object {
		val LAST_DEVICE_ADDRESS = stringPreferencesKey("bluetooth.last_device_address")
		val FAVORITE_DEVICES = stringSetPreferencesKey("bluetooth.favorite_devices")

		val APP_MARQUEE_ANIMATION_ENABLED = booleanPreferencesKey("app.marquee_animation_enabled")
		val APP_CORNER_DPAD_BUTTONS_ENABLED = booleanPreferencesKey("app.corner_dpad_buttons_enabled")
		val APP_DUAL_CONTINUOUS_ENABLED = booleanPreferencesKey("app.dual_continuous_enabled")

		// Speed
		val SPEED_RANGE_LOW = floatPreferencesKey("speed.range_low")
		val SPEED_RANGE_HIGH = floatPreferencesKey("speed.range_high")
		val SPEED_STATE = booleanPreferencesKey("speed.state") // true = HIGH, false = LOW

		// Servo A
		val SERVO_A_MODE = intPreferencesKey("servo.a.mode")
		val SERVO_A_RANGE_LOW = floatPreferencesKey("servo.a.range_low")
		val SERVO_A_RANGE_HIGH = floatPreferencesKey("servo.a.range_high")
		val SERVO_A_STATE = booleanPreferencesKey("servo.a.state") // true = HIGH, false = LOW

		// Servo B
		val SERVO_B_MODE = intPreferencesKey("servo.b.mode")
		val SERVO_B_RANGE_LOW = floatPreferencesKey("servo.b.range_low")
		val SERVO_B_RANGE_HIGH = floatPreferencesKey("servo.b.range_high")
		val SERVO_B_STATE = booleanPreferencesKey("servo.b.state") // true = HIGH, false = LOW


		// Servo C
		val SERVO_C_MODE = intPreferencesKey("servo.c.mode")
		val SERVO_C_INVERTED = booleanPreferencesKey("servo.c.inverted")
		val SERVO_C_RANGE_LOW = floatPreferencesKey("servo.c.range_low")
		val SERVO_C_RANGE_HIGH = floatPreferencesKey("servo.c.range_high")
		val SERVO_C_STATE = booleanPreferencesKey("servo.c.state") // true = HIGH, false = LOW

		// Stopwatch
		val STOPWATCH_PB = longPreferencesKey("stopwatch.pb")
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

	val favoriteDevices = get(FAVORITE_DEVICES, emptySet())
	suspend fun toggleFavoriteDevice(address: String) {
		context.userPreferences.edit { preferences ->
			val currentFavorites = preferences[FAVORITE_DEVICES] ?: emptySet()
			val newFavorites = currentFavorites.toMutableSet()

			if (newFavorites.contains(address)) {
				newFavorites.remove(address)
			} else {
				newFavorites.add(address)
			}
			preferences[FAVORITE_DEVICES] = newFavorites
		}
	}

	val marqueeAnimationEnabled =
		get(APP_MARQUEE_ANIMATION_ENABLED, UserPreferencesDefaults.APP_MARQUEE_ANIMATION_ENABLED)

	suspend fun setMarqueeAnimationEnabled(enabled: Boolean) =
		set(APP_MARQUEE_ANIMATION_ENABLED, enabled)

	val cornerDPadButtonsEnabled = get(
		APP_CORNER_DPAD_BUTTONS_ENABLED, UserPreferencesDefaults.APP_CORNER_DPAD_BUTTONS_ENABLED
	)

	suspend fun setCornerDPadButtonsEnabled(enabled: Boolean) =
		set(APP_CORNER_DPAD_BUTTONS_ENABLED, enabled)

	val dualContinuousEnabled = get(
		APP_DUAL_CONTINUOUS_ENABLED, UserPreferencesDefaults.APP_DUAL_CONTINUOUS_ENABLED
	)

	suspend fun setDualContinuousEnabled(enabled: Boolean) = set(APP_DUAL_CONTINUOUS_ENABLED, enabled)


	// Speed
	val speedState = get(
		SPEED_STATE, UserPreferencesDefaults.SPEED_STATE == RangeState.HIGH
	).map { isHigh -> if (isHigh) RangeState.HIGH else RangeState.LOW }

	suspend fun setSpeedState(state: RangeState) = set(SPEED_STATE, state == RangeState.HIGH)

	val speedRange = context.userPreferences.data.map { prefs ->
		val start = prefs[SPEED_RANGE_LOW] ?: UserPreferencesDefaults.SPEED_RANGE.start
		val end = prefs[SPEED_RANGE_HIGH] ?: UserPreferencesDefaults.SPEED_RANGE.endInclusive
		start..end
	}

	suspend fun setSpeedRange(range: ClosedFloatingPointRange<Float>) {
		context.userPreferences.edit { prefs ->
			prefs[SPEED_RANGE_LOW] = range.start
			prefs[SPEED_RANGE_HIGH] = range.endInclusive
		}
	}

	// Servo A
	val servoAMode = get(SERVO_A_MODE, ServoMode.POSITIONAL.value).map { ServoMode.fromInt(it) }
	suspend fun setServoAMode(mode: ServoMode) = set(SERVO_A_MODE, mode.value)

	val servoAState = get(
		SERVO_A_STATE, UserPreferencesDefaults.SERVO_A_STATE == RangeState.HIGH
	).map { isHigh -> if (isHigh) RangeState.HIGH else RangeState.LOW }

	suspend fun setServoAState(state: RangeState) = set(SERVO_A_STATE, state == RangeState.HIGH)

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
	val servoBMode = get(SERVO_B_MODE, ServoMode.POSITIONAL.value).map { ServoMode.fromInt(it) }
	suspend fun setServoBMode(mode: ServoMode) = set(SERVO_B_MODE, mode.value)

	val servoBState = get(
		SERVO_B_STATE, UserPreferencesDefaults.SERVO_B_STATE == RangeState.HIGH
	).map { isHigh -> if (isHigh) RangeState.HIGH else RangeState.LOW }

	suspend fun setServoBState(state: RangeState) = set(SERVO_B_STATE, state == RangeState.HIGH)

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

	// Servo C
	val servoCMode = get(SERVO_C_MODE, UserPreferencesDefaults.SERVO_C_MODE.value).map { ServoMode.fromInt(it) }
	suspend fun setServoCMode(mode: ServoMode) = set(SERVO_C_MODE, mode.value)

	val servoCInverted = get(SERVO_C_INVERTED, false)
	suspend fun setServoCInverted(inverted: Boolean) = set(SERVO_C_INVERTED, inverted)

	val servoCState = get(
		SERVO_C_STATE, UserPreferencesDefaults.SERVO_C_STATE == RangeState.HIGH
	).map { isHigh -> if (isHigh) RangeState.HIGH else RangeState.LOW }

	suspend fun setServoCState(state: RangeState) = set(SERVO_C_STATE, state == RangeState.HIGH)

	val servoCRange = context.userPreferences.data.map { prefs ->
		val start = prefs[SERVO_C_RANGE_LOW] ?: UserPreferencesDefaults.SERVO_C_RANGE.start
		val end = prefs[SERVO_C_RANGE_HIGH] ?: UserPreferencesDefaults.SERVO_C_RANGE.endInclusive
		start..end
	}

	suspend fun setServoCRange(range: ClosedFloatingPointRange<Float>) {
		context.userPreferences.edit { prefs ->
			prefs[SERVO_C_RANGE_LOW] = range.start
			prefs[SERVO_C_RANGE_HIGH] = range.endInclusive
		}
	}

	val stopwatchPB = get(STOPWATCH_PB, null)
	suspend fun setStopwatchPB(pb: Long?) = set(STOPWATCH_PB, pb)
}
