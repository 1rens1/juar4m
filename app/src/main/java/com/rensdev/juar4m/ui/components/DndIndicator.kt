package com.rensdev.juar4m.ui.components

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rensdev.juar4m.ui.theme.GeistMonoFontFamily
import com.rensdev.juar4m.ui.theme.Theme
import kotlinx.coroutines.delay

private val fontSize = 12.sp

@Composable
private fun DndDisabled(modifier: Modifier = Modifier) {
	var alpha by remember { mutableFloatStateOf(0.5f) }

	LaunchedEffect(Unit) {
		while (true) {
			alpha = if (alpha == 0.5f) 0.25f else 0.5f
			delay(600)
		}
	}

	Text(
		modifier = modifier,
		text = "Do not disturb OFF",
		color = Color.Red.copy(alpha = alpha),
		fontSize = fontSize,
		lineHeight = fontSize,
		letterSpacing = (-0.025).sp,
		fontFamily = GeistMonoFontFamily
	)
}

@Composable
private fun DndEnabled(modifier: Modifier = Modifier) {
	Text(
		modifier = modifier,
		text = "Dnd ON",
		color = Color.Green.copy(alpha = 0.3f),
		fontSize = fontSize,
		lineHeight = fontSize,
		letterSpacing = (-0.025).sp,
		fontFamily = GeistMonoFontFamily
	)
}

@Composable
fun DndIndicator(modifier: Modifier = Modifier) {
	val context = LocalContext.current
	val isInspection = LocalInspectionMode.current
	var isDndEnabled by remember { mutableStateOf(false) }

	fun checkDndStatus(): Boolean {
		if (isInspection) return false
		return try {
			val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
			nm.currentInterruptionFilter != NotificationManager.INTERRUPTION_FILTER_ALL
		} catch (_: Exception) {
			true
		}
	}

	DisposableEffect(context) {
		if (isInspection) return@DisposableEffect onDispose { }

		isDndEnabled = checkDndStatus()

		val receiver = object : BroadcastReceiver() {
			override fun onReceive(context: Context?, intent: Intent?) {
				isDndEnabled = checkDndStatus()
			}
		}

		val filter = IntentFilter(NotificationManager.ACTION_INTERRUPTION_FILTER_CHANGED)
		context.registerReceiver(receiver, filter)

		onDispose { context.unregisterReceiver(receiver) }
	}

	if (isDndEnabled) {
		DndEnabled(modifier)
	} else {
		DndDisabled(modifier)
	}
}

@Preview
@Composable
fun DndIndicatorPreview() {
	Theme {
		Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
			DndDisabled()
			DndEnabled()
		}
	}
}
