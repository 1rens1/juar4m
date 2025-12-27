package com.rensdev.juar4m.utils

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class VibrationManager(context: Context) {
	private val systemVibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
		(context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
	} else {
		@Suppress("DEPRECATION") context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
	}

	private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
	private var loopJob: Job? = null
	var enabled: Boolean = true

	private val vibrator: Vibrator?
		get() = if (enabled) systemVibrator else null

	private fun oneShot(milliseconds: Long) {
		vibrator?.vibrate(
			VibrationEffect.createOneShot(
				milliseconds, VibrationEffect.DEFAULT_AMPLITUDE
			)
		)
	}

	private var isHighPriorityActive = false

	private fun oneShotHighPriority(duration: Long) {
		isHighPriorityActive = true
		oneShot(duration)
		scope.launch {
			delay(duration)
			isHighPriorityActive = false
		}
	}

	fun click() {
		oneShotHighPriority(70)
	}

	fun strongClick() {
		oneShotHighPriority(200)
	}

	fun weakContinuous() {
		if (loopJob?.isActive == true) return
		loopJob = scope.launch {
			while (isActive) {
				if (!isHighPriorityActive) {
					oneShot(20)
				}
				delay(50)
			}
		}
	}

	fun cancel() {
		loopJob?.cancel()
		loopJob = null
		isHighPriorityActive = false
		systemVibrator.cancel()
	}
}
