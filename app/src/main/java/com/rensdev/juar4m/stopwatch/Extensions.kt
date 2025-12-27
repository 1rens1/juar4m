package com.rensdev.juar4m.stopwatch

fun formatStopwatch(elapsed: Long): String {
	val minutes = elapsed / 60000
	val seconds = (elapsed % 60000) / 1000
	val millis = (elapsed % 1000) / 10 // 2 numbers behind decimal

	val zeroPad = { value: Long -> value.toString().padStart(2, '0') }

	return "${minutes}:${zeroPad(seconds)}.${zeroPad(millis)}"
}
