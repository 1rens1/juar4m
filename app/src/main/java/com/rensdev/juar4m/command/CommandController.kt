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
	private val onSendPacket: (ByteArray) -> Unit
) {
	private val currentDriveState = AtomicInteger(Commands.STOP_MOTORS)

	private var tickerJob: Job? = null

	fun setServo(id: Int, angle: Int) {
		val packed = 0x8000 or ((id and 0x7F) shl 8) or (angle and 0xFF)
		sendRaw(packed)
	}

	fun drive(command: Int) {
		currentDriveState.set(command)
	}

	fun stop() {
		currentDriveState.set(Commands.STOP_MOTORS)
	}

	fun nudge(command: Int) {
		scope.launch {
			sendRaw(command)

			pauseTicker = true
			delay(60)
			pauseTicker = false
		}
	}

	@Volatile
	private var pauseTicker = false

	fun start() {
		stopTicker()
		tickerJob = scope.launch(Dispatchers.IO) {
			while (isActive) {
				if (!pauseTicker) {
					sendRaw(currentDriveState.get())
				}
				delay(50)
			}
		}
	}

	fun stopTicker() {
		tickerJob?.cancel()
	}

	private fun sendRaw(command: Int) {
		val bytes = byteArrayOf(
			(command and 0xFF).toByte(),
			((command shr 8) and 0xFF).toByte()
		)
		onSendPacket(bytes)
	}
}
