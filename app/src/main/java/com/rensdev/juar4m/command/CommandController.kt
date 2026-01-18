package com.rensdev.juar4m.command

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicInteger

class CommandController(
	private val scope: CoroutineScope,
	private val onSendCommand: (bytes: ByteArray) -> Unit,
	private val onInput: (command: Int) -> Unit
) {
	private val currentDriveState = AtomicInteger(Commands.STOP_MOTORS)

	private var tickerJob: Job? = null
	private var nudgeJob: Job? = null

	@Volatile
	private var pauseTicker = false

	fun setServo(id: Int, angle: Int) {
		val packed = 0x8000 or ((id and 0x7F) shl 8) or (angle and 0xFF)
		sendCommand(packed)
	}

	fun drive(command: Int) {
		currentDriveState.set(command)
		onInput(command)
	}

	fun stop() {
		currentDriveState.set(Commands.STOP_MOTORS)
		onInput(Commands.STOP_MOTORS)
	}

	fun nudge(command: Int) {
		pauseTicker = true
		nudgeJob?.cancel()

		sendCommand(command)

		nudgeJob = scope.launch {
			delay(60)
			pauseTicker = false
		}
	}

	fun start() {
		stopTicker()
		tickerJob = scope.launch(Dispatchers.IO) {
			while (isActive) {
				if (!pauseTicker) {
					sendCommand(currentDriveState.get())
				}
				delay(30)
			}
		}
	}

	fun stopTicker() {
		tickerJob?.cancel()
		nudgeJob?.cancel()
		pauseTicker = false
	}

	private fun sendCommand(command: Int) {
		// Int to 2 byte little endian
		val bytes = byteArrayOf(
			(command and 0xFF).toByte(),
			((command shr 8) and 0xFF).toByte()
		)
		onSendCommand(bytes)
		onInput(command)
	}
}
