package com.rensdev.juar4m.utils

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

fun Context.getVibrator(): Vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
	(getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
} else {
	@Suppress("DEPRECATION") getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
}

fun Vibrator.vibrateClick() {
	vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
}

fun Vibrator.vibrateWeakContinuous() {
	val timing = longArrayOf(0, 1000)
	val amplitudes = intArrayOf(0, 40)
	vibrate(VibrationEffect.createWaveform(timing, amplitudes, 0))
}
