package com.rensdev.juar4m.ui.components

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rensdev.juar4m.ui.theme.Black
import com.rensdev.juar4m.ui.theme.Red
import com.rensdev.juar4m.ui.theme.White
import kotlinx.coroutines.delay

@Composable
fun BatteryIndicator(initialValue: Int = 100) {
	val context = LocalContext.current

	val batteryState by produceState(
		initialValue = Pair(initialValue, false)
	) {
		val receiver = object : BroadcastReceiver() {
			override fun onReceive(context: Context?, intent: Intent) {
				val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
				val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)

				val status = intent.getIntExtra(
					BatteryManager.EXTRA_STATUS,
					BatteryManager.BATTERY_STATUS_UNKNOWN
				)

				val charging =
					status == BatteryManager.BATTERY_STATUS_CHARGING ||
							status == BatteryManager.BATTERY_STATUS_FULL

				if (level >= 0 && scale > 0) {
					value = Pair((level * 100) / scale, charging)
				}
			}
		}

		context.registerReceiver(
			receiver,
			IntentFilter(Intent.ACTION_BATTERY_CHANGED)
		)

		awaitDispose {
			context.unregisterReceiver(receiver)
		}
	}

	val batteryLevel = batteryState.first
	val isCharging = batteryState.second

	val lowBattery = batteryLevel <= 20 && !isCharging

	var alpha by remember { mutableFloatStateOf(1f) }

	LaunchedEffect(lowBattery) {
		if (!lowBattery) {
			alpha = 1f
			return@LaunchedEffect
		}

		while (true) {
			alpha = if (alpha == 1f) 0.75f else 1f
			delay(600)
		}
	}

	Box(
		modifier = Modifier
			.width(28.dp)
			.height(16.dp)
			.clip(RoundedCornerShape(6.dp))
			.alpha(alpha)
			.background(White.copy(alpha = 0.6f)),
		contentAlignment = Alignment.Center
	) {
		Box(
			modifier = Modifier
				.fillMaxHeight()
				.fillMaxWidth(batteryLevel / 100f)
				.background(White)
				.align(Alignment.CenterStart)
		)

		Text(
			text = buildAnnotatedString {
				append("$batteryLevel")
				withStyle(SpanStyle(fontSize = 8.sp)) {
					append("%")
				}
			},
			color = if (lowBattery) Red else Black,
			fontSize = 10.sp,
			fontWeight = FontWeight.Bold,
			letterSpacing = (-0.25).sp
		)
	}
}

@Preview
@Composable
fun BatteryIndicatorPreview() {
	Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
		BatteryIndicator()
		BatteryIndicator(69)
		BatteryIndicator(20)
		BatteryIndicator(0)
	}
}
