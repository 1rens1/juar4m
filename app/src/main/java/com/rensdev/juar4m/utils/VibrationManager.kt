package com.rensdev.juar4m.utils

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

class VibrationManager(context: Context) {
	private val systemVibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
		(context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
	} else {
		@Suppress("DEPRECATION") context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
	}

	var enabled: Boolean = true

	private val vibrator: Vibrator?
		get() = if (enabled) systemVibrator else null

	fun click() {
		vibrator?.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
	}

	fun weakContinuous() {
		val timing = longArrayOf(0, 1000)
		val amplitudes = intArrayOf(0, 40)
		vibrator?.vibrate(VibrationEffect.createWaveform(timing, amplitudes, 0))
	}

	fun doubleClick() {
		vibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_DOUBLE_CLICK))
	}

	fun cancel() {
		systemVibrator.cancel()
	}
}