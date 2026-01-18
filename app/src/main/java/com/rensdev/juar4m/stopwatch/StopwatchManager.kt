package com.rensdev.juar4m.stopwatch

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

private const val UPDATE_PER_SECOND = 25
private const val UPDATE_INTERVAL_MILLIS = 1000L / UPDATE_PER_SECOND

class StopwatchManager(private val scope: CoroutineScope) {

	private var job: Job? = null
	private var startTimeNs: Long = 0L
	private var accumulatedTimeNs: Long = 0L

	private val _elapsedMillis = MutableStateFlow(0L)
	val elapsedMillis: StateFlow<Long> = _elapsedMillis.asStateFlow()

	private val _isRunning = MutableStateFlow(false)
	val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

	private var isReady = true

	fun start() {
		if (_isRunning.value) return
		if (!isReady) return

		_isRunning.value = true
		isReady = true
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
		isReady = false
		job?.cancel()

		val currentNs = System.nanoTime()
		accumulatedTimeNs += (currentNs - startTimeNs)

		_elapsedMillis.value = TimeUnit.NANOSECONDS.toMillis(accumulatedTimeNs)
	}

	fun reset() {
		_isRunning.value = false
		job?.cancel()

		isReady = true
		accumulatedTimeNs = 0L
		_elapsedMillis.value = 0L
	}
}
