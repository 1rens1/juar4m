package com.rensdev.juar4m.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.rensdev.juar4m.BuildConfig
import com.rensdev.juar4m.MainViewModel
import com.rensdev.juar4m.ui.components.controller.AnimatedSideDialog
import com.rensdev.juar4m.ui.theme.ColorScheme
import com.rensdev.juar4m.ui.theme.Typography
import com.rensdev.juar4m.ui.theme.White
import kotlinx.coroutines.flow.flowOf
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun MenuDialog(
	viewModel: MainViewModel?,
	onDismissRequest: () -> Unit
) {
	val vibrationsEnabled by (viewModel?.vibrationsEnabled ?: flowOf(true))
		.collectAsState(initial = true)
	val timerModeEnabled by (viewModel?.timerModeEnabled ?: flowOf(false))
		.collectAsState(initial = false)

	AnimatedSideDialog(onDismissRequest = onDismissRequest) { dismiss ->
		Surface(
			modifier = Modifier
				.width(280.dp)
				.fillMaxHeight()
				.padding(start = 16.dp, top = 8.dp),
			color = ColorScheme.surfaceContainer
		) {
			Column(modifier = Modifier.padding(top = 4.dp)) {
//				Header
				Row(
					verticalAlignment = Alignment.CenterVertically,
					horizontalArrangement = Arrangement.spacedBy(4.dp)
				) {
					IconButton(onClick = dismiss) {
						Icon(
							imageVector = Icons.AutoMirrored.Filled.ArrowBack,
							contentDescription = "Back"
						)
					}
					Text("Menu", style = Typography.titleLarge)
				}

//				Body
				Column(
					modifier = Modifier
						.fillMaxHeight()
						.padding(bottom = 16.dp),
					verticalArrangement = Arrangement.SpaceBetween
				) {
					Column {
						HorizontalDivider(
							color = White.copy(alpha = 0.1f),
							modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
						)
						ToggleSetting(
							label = "Vibrations",
							value = vibrationsEnabled,
							onValueChange = {
								viewModel?.saveVibrationsEnabled(it)
							}
						)
						ToggleSetting(
							label = "Timer mode",
							value = timerModeEnabled,
							onValueChange = {
								viewModel?.saveTimerModeEnabled(it)
							}
						)
					}
					AppInfo(modifier = Modifier.padding(horizontal = 16.dp))
				}
			}
		}
	}
}

@Composable
private fun ToggleSetting(
	label: String,
	value: Boolean,
	onValueChange: (Boolean) -> Unit,
	modifier: Modifier = Modifier
) {
	Row(
		modifier = modifier
			.fillMaxWidth()
			.toggleable(
				value = value, onValueChange = onValueChange, role = Role.Switch
			)
			.padding(horizontal = 16.dp, vertical = 12.dp),
		verticalAlignment = Alignment.CenterVertically
	) {
		Text(
			text = label, modifier = Modifier.weight(1f)
		)
		Switch(
			checked = value, onCheckedChange = null
		)
	}
}

@Composable
private fun AppInfo(modifier: Modifier = Modifier) {
	val version = BuildConfig.VERSION_NAME
	val formattedBuildDate = try {
		Instant.ofEpochMilli(BuildConfig.BUILD_TIME)
			.atZone(ZoneId.systemDefault())
			.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
	} catch (e: Exception) {
		"Unknown"
	}

	Text(
		"""
		JUAR4M by @1rens1 (GitHub)
		Version: $version
		Build: $formattedBuildDate
		""".trimIndent(),
		style = Typography.bodyMedium.copy(color = White.copy(alpha = 0.5f)),
		modifier = modifier
	)
}
