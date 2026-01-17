package com.rensdev.juar4m.ui.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import java.time.Clock
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun ClockText(
	clock: Clock = Clock.systemDefaultZone(),
	text: @Composable (String) -> Unit = { Text(it) }
) {
	val formatter = remember { DateTimeFormatter.ofPattern("h:mm a  d/M/y") }
	var time by remember { mutableStateOf(LocalDateTime.now(clock).format(formatter)) }

	LaunchedEffect(clock) {
		while (isActive) {
			val now = LocalDateTime.now(clock)
			time = now.format(formatter)
			val msUntilNextMinute = 60_000L - (now.second * 1_000L + now.nano / 1_000_000L)
			delay(msUntilNextMinute.coerceAtLeast(1L))
		}
	}

	text(time)
}
