package com.rensdev.juar4m.stopwatch

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.TimeUnit

private const val UPDATE_PER_SECOND = 30
private const val UPDATE_INTERVAL_MILLIS = 1000L / UPDATE_PER_SECOND

class StopwatchManager(private val scope: CoroutineScope) {

	private var job: Job? = null
	private var startTimeNs: Long = 0L
	private var accumulatedTimeNs: Long = 0L

	private val _elapsedMillis = MutableStateFlow(0L)
	val elapsedMillis: StateFlow<Long> = _elapsedMillis.asStateFlow()

	private val _isRunning = MutableStateFlow(false)
	val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()


	/**
	 * Called whenever a control input is detected.
	 * Starts the timer if it's not already running.
	 */
	fun onInputDetected() {
		if (!_isRunning.value) {
			start()
		}
	}

	fun start() {
		if (_isRunning.value) return

		_isRunning.value = true
		startTimeNs = System.nanoTime()

		job = scope.launch(Dispatchers.Default) {
			while (isActive) {
				val currentNs = System.nanoTime()
				val totalNs = accumulatedTimeNs + (currentNs - startTimeNs)

				_elapsedMillis.value = TimeUnit.NANOSECONDS.toMillis(totalNs)

				delay(UPDATE_INTERVAL_MILLIS)
			}
		}
	}

	fun stop() {
		if (!_isRunning.value) return

		_isRunning.value = false
		job?.cancel()

		val currentNs = System.nanoTime()
		accumulatedTimeNs += (currentNs - startTimeNs)

		_elapsedMillis.value = TimeUnit.NANOSECONDS.toMillis(accumulatedTimeNs)
	}

	fun reset() {
		stop()
		accumulatedTimeNs = 0L
		_elapsedMillis.value = 0L
	}
}
