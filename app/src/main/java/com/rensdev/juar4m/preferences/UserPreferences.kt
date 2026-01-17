package com.rensdev.juar4m.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.rensdev.juar4m.ui.components.controller.ServoState
import kotlinx.coroutines.flow.map

val Context.userPreferences: DataStore<Preferences> by preferencesDataStore("user_preferences")

@Suppress("KotlinConstantConditions")
class UserPreferences(private val context: Context) {
	companion object {
		val LAST_DEVICE_ADDRESS = stringPreferencesKey("bluetooth.last_device_address")
		val FAVORITE_DEVICES = stringSetPreferencesKey("bluetooth.favorite_devices")

		val APP_MARQUEE_ANIMATION_ENABLED = booleanPreferencesKey("app.marquee_animation_enabled")
		val APP_CORNER_DPAD_BUTTONS_ENABLED = booleanPreferencesKey("app.corner_dpad_buttons_enabled")

		// Servo A
		val SERVO_A_RANGE_LOW = floatPreferencesKey("servo.a.range_low")
		val SERVO_A_RANGE_HIGH = floatPreferencesKey("servo.a.range_high")
		val SERVO_A_STATE = booleanPreferencesKey("servo.a.state") // true = HIGH, false = LOW

		// Servo B
		val SERVO_B_RANGE_LOW = floatPreferencesKey("servo.b.range_low")
		val SERVO_B_RANGE_HIGH = floatPreferencesKey("servo.b.range_high")
		val SERVO_B_STATE = booleanPreferencesKey("servo.b.state")

		// Servo C
		val SERVO_C_INVERTED = booleanPreferencesKey("servo.c.inverted")

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

	// Servo C
	val servoCInverted = get(SERVO_C_INVERTED, false)
	suspend fun setServoCInverted(inverted: Boolean) = set(SERVO_C_INVERTED, inverted)

	val stopwatchPB = get(STOPWATCH_PB, null)
	suspend fun setStopwatchPB(pb: Long?) = set(STOPWATCH_PB, pb)
}
