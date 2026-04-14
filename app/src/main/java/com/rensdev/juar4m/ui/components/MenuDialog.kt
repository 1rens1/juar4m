package com.rensdev.juar4m.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import com.rensdev.juar4m.BuildConfig
import com.rensdev.juar4m.MainViewModel
import com.rensdev.juar4m.R
import com.rensdev.juar4m.preferences.ServoMode
import com.rensdev.juar4m.preferences.UserPreferencesDefaults
import com.rensdev.juar4m.preferences.collectAsStateOrFallback
import com.rensdev.juar4m.ui.theme.Black
import com.rensdev.juar4m.ui.theme.ColorScheme
import com.rensdev.juar4m.ui.theme.GeistMonoFontFamily
import com.rensdev.juar4m.ui.theme.Red
import com.rensdev.juar4m.ui.theme.Theme
import com.rensdev.juar4m.ui.theme.Transparent
import com.rensdev.juar4m.ui.theme.Typography
import com.rensdev.juar4m.ui.theme.White
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun MenuDialog(
	viewModel: MainViewModel?, onDismissRequest: () -> Unit
) {
	val marqueAnimationEnabled by viewModel?.marqueeAnimationEnabled.collectAsStateOrFallback(
		UserPreferencesDefaults.APP_MARQUEE_ANIMATION_ENABLED
	)
	val cornerDPadButtonsEnabled by viewModel?.cornerDPadButtonsEnabled.collectAsStateOrFallback(
		UserPreferencesDefaults.APP_CORNER_DPAD_BUTTONS_ENABLED
	)
	val vibrationsEnabled by viewModel?.vibrationsEnabled.collectAsStateOrFallback(
		UserPreferencesDefaults.APP_VIBRATIONS_ENABLED
	)
	val disconnectLockEnabled by viewModel?.disconnectLockEnabled.collectAsStateOrFallback(
		UserPreferencesDefaults.APP_DISCONNECT_LOCK_ENABLED
	)
	val stopwatchModeEnabled by viewModel?.stopwatchModeEnabled.collectAsStateOrFallback(
		UserPreferencesDefaults.APP_STOPWATCH_MODE_ENABLED
	)
	val servoAMode by viewModel?.servoAMode.collectAsStateOrFallback(UserPreferencesDefaults.SERVO_A_MODE)
	val servoBMode by viewModel?.servoBMode.collectAsStateOrFallback(UserPreferencesDefaults.SERVO_B_MODE)
	val servoCMode by viewModel?.servoCMode.collectAsStateOrFallback(UserPreferencesDefaults.SERVO_C_MODE)

	var showServoModeConfigDialog by remember { mutableStateOf(false) }
	var editServoAMode by remember(servoAMode) { mutableStateOf(servoAMode) }
	var editServoBMode by remember(servoBMode) { mutableStateOf(servoBMode) }
	var editServoCMode by remember(servoCMode) { mutableStateOf(servoCMode) }

	AnimatedSideDialog(onDismissRequest = onDismissRequest) { dismiss ->
		Surface(
			modifier = Modifier
				.width(280.dp)
				.fillMaxHeight()
				.padding(start = 16.dp, top = 8.dp),
			color = ColorScheme.surfaceContainer
		) {
			Column(modifier = Modifier.padding(top = 4.dp)) {
				// Header
				Row(
					modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween
				) {
					Row(
						verticalAlignment = Alignment.CenterVertically,
						horizontalArrangement = Arrangement.spacedBy(4.dp)
					) {
						IconButton(onClick = dismiss) {
							Icon(
								imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back"
							)
						}
						Text("Menu", style = Typography.titleLarge)
					}
					Image(
						painter = painterResource(id = R.mipmap.ic_launcher_foreground),
						contentDescription = "JUAR4M Logo",
						modifier = Modifier
							.size(48.dp)
							.offset((-4).dp)
					)
				}
				HorizontalDivider(
					color = White.copy(alpha = 0.1f),
					modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
				)

				// Body
				val scrollState = rememberScrollState()
				Column(
					modifier = Modifier
						.fillMaxHeight()
						.verticalScroll(scrollState),
					verticalArrangement = Arrangement.SpaceBetween
				) {
					Column {
						SettingsLabel(
							text = "App Settings (saved on close)",
							style = Typography.bodySmall.copy(color = White.copy(alpha = 0.5f)),
						)
						ToggleSetting(
							label = "Bottom marquee animation (high battery)",
							value = marqueAnimationEnabled,
							onValueChange = { isEnabled ->
								viewModel?.setMarqueeAnimationEnabled(isEnabled)
							})
						ToggleSetting(
							label = "Corner DPad Buttons (↖↗↙↘)",
							value = cornerDPadButtonsEnabled,
							onValueChange = { isEnabled ->
								viewModel?.setCornerDPadButtonsEnabled(isEnabled)
							})
						SettingButton(
							modifier = Modifier.clickable(onClick = { showServoModeConfigDialog = true })
						) {
							Text(text = "Servo Mode Config", modifier = Modifier.weight(1f))
							Icon(
								modifier = Modifier.size(24.dp),
								imageVector = Icons.AutoMirrored.Default.ArrowForward,
								contentDescription = null
							)
						}
						SettingsLabel(
							text = "Session Settings (NOT saved on close)",
							style = Typography.bodySmall.copy(color = Red.copy(alpha = 0.5f))
						)
						ToggleSetting(
							label = "Vibrations", value = vibrationsEnabled, onValueChange = { isEnabled ->
								viewModel?.apply {
									setVibrationsEnabled(isEnabled)
									if (isEnabled) {
										vibrator.enabled = true
										vibrator.click()
									}
								}
							})
						ToggleSetting(
							label = "Lock controls when not connected",
							value = disconnectLockEnabled,
							onValueChange = {
								viewModel?.setDisconnectLockEnabled(it)
							})
						ToggleSetting(
							label = "Stopwatch mode", value = stopwatchModeEnabled, onValueChange = {
								viewModel?.setStopwatchModeEnabled(it)
							})
					}
					AppInfo(
						modifier = Modifier.padding(
							top = 0.dp, start = 16.dp, end = 16.dp, bottom = 16.dp
						)
					)
				}
			}
		}
	}

	val scrollState = rememberScrollState()

	if (showServoModeConfigDialog) {
		Dialog(
			onDismissRequest = { showServoModeConfigDialog = false },
			properties = DialogProperties(usePlatformDefaultWidth = false),
		) {
			Box(
				modifier = Modifier
					.fillMaxSize()
					.background(Black.copy(alpha = 0.5f)),
				contentAlignment = Alignment.Center
			) {
				Card(
					modifier = Modifier
						.fillMaxWidth(0.9f)
						.fillMaxHeight(0.9f)
						.padding(16.dp),
					colors = CardDefaults.cardColors(
						containerColor = ColorScheme.surfaceContainer,
						contentColor = AlertDialogDefaults.titleContentColor
					)
				) {
					Column(
						modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(0.dp)
					) {
						Row(
							modifier = Modifier
								.fillMaxWidth()
								.fillMaxHeight()
						) {
							Column(
								modifier = Modifier
									.width(IntrinsicSize.Max)
									.fillMaxHeight(),
								verticalArrangement = Arrangement.SpaceBetween
							) {
								Text("Configure Servo Modes", style = Typography.titleLarge)

								Column(
									modifier = Modifier
										.weight(1f)
										.verticalScroll(scrollState),
									verticalArrangement = Arrangement.SpaceEvenly
								) {
									ServoModeSelect(
										label = "Servo A",
										selected = editServoAMode,
										onSelect = { editServoAMode = it })
									ServoModeSelect(
										label = "Servo B",
										selected = editServoBMode,
										onSelect = { editServoBMode = it })
									ServoModeSelect(
										label = "Servo C",
										selected = editServoCMode,
										onSelect = { editServoCMode = it })
								}

								Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
									CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides Dp.Unspecified) {
										Surface(
											modifier = Modifier.weight(0.5f),
											onClick = {
												viewModel?.apply {
													setServoAMode(editServoAMode)
													setServoBMode(editServoBMode)
													setServoCMode(editServoCMode)
													commandController.sendServoConfig(
														editServoAMode, editServoBMode, editServoCMode
													)
												}
												showServoModeConfigDialog = false
											},
											color = ButtonDefaults.buttonColors().containerColor,
											contentColor = ButtonDefaults.buttonColors().contentColor,
											shape = ButtonDefaults.shape,
										) {
											Box(
												modifier = Modifier.padding(vertical = 6.dp),
												contentAlignment = Alignment.Center
											) {
												Text("Save", fontSize = Typography.bodyMedium.fontSize)
											}
										}

										Surface(
											modifier = Modifier.weight(1f),
											onClick = {
												editServoAMode = UserPreferencesDefaults.SERVO_A_MODE
												editServoBMode = UserPreferencesDefaults.SERVO_B_MODE
												editServoCMode = UserPreferencesDefaults.SERVO_C_MODE
											},
											color = White.copy(alpha = 0.1f),
											contentColor = White,
											shape = ButtonDefaults.shape,
										) {
											Box(
												modifier = Modifier.padding(vertical = 6.dp),
												contentAlignment = Alignment.Center
											) {
												Text("Reset to Defaults", fontSize = Typography.bodyMedium.fontSize)
											}
										}
									}
								}
							}

							Spacer(modifier = Modifier.width(16.dp))

							Image(
								painter = painterResource(id = R.drawable.servo_config_guide),
								contentDescription = null,
								modifier = Modifier
									.weight(1f)
									.fillMaxHeight(),
								contentScale = ContentScale.Fit
							)
						}
					}
				}
			}
		}
	}
}


@Composable
private fun SettingsLabel(modifier: Modifier = Modifier, text: String, style: TextStyle) = Text(
	text, style = style, modifier = modifier.padding(
		top = 8.dp, bottom = 0.dp, start = 16.dp, end = 16.dp
	)
)

@Composable
private fun SettingButton(
	modifier: Modifier = Modifier, content: @Composable RowScope.() -> Unit = {}
) {
	Row(
		modifier = modifier
			.fillMaxWidth()
			.padding(horizontal = 16.dp, vertical = 12.dp),
		verticalAlignment = Alignment.CenterVertically,
		horizontalArrangement = Arrangement.spacedBy(8.dp)
	) {
		content()
	}
}

@Composable
private fun ToggleSetting(
	label: String, value: Boolean, onValueChange: (Boolean) -> Unit, modifier: Modifier = Modifier
) {
	SettingButton(
		modifier = modifier.toggleable(
			value = value, onValueChange = onValueChange, role = Role.Switch
		),
	) {
		Text(text = label, modifier = Modifier.weight(1f))
		Switch(
			checked = value,
			onCheckedChange = null,
			colors = SwitchDefaults.colors(uncheckedBorderColor = White.copy(alpha = 0.5f))
		)
	}
}

@Composable
private fun AppInfo(modifier: Modifier = Modifier) {
	val versionName = BuildConfig.VERSION_NAME
	val versionCode = BuildConfig.VERSION_CODE

	val formattedBuildDate = try {
		Instant.ofEpochMilli(BuildConfig.BUILD_TIME).atZone(ZoneId.systemDefault())
			.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
	} catch (_: Exception) {
		"Unknown"
	}

	Text(
		"""
		JUAR4M by @1rens1 (GitHub)
		Logo by Jo
		Version: $versionName ($versionCode)
		Build: $formattedBuildDate
		""".trimIndent(), style = Typography.bodySmall.copy(
			color = White.copy(alpha = 0.5f), fontFamily = GeistMonoFontFamily
		), modifier = modifier
	)
}

@Composable
fun ServoModeSelect(label: String, selected: ServoMode, onSelect: (ServoMode) -> Unit) {
	Column {
		Text(text = label, color = White.copy(alpha = 0.8f), fontSize = Typography.bodyMedium.fontSize)
		ServoModeButtonGroup(selected = selected, onSelect = onSelect)
	}
}

/**
 * Segmented button group that replaces the dropdown for servo mode selection.
 * Selected segment uses the primary button colors; unselected uses surfaceContainerHigh.
 * The default checkmark icon is suppressed so only the label is shown.
 */
@Composable
fun ServoModeButtonGroup(
	modifier: Modifier = Modifier,
	selected: ServoMode,
	onSelect: (ServoMode) -> Unit,
) {
	val modes = ServoMode.entries
	Row(modifier = modifier) {
		modes.forEachIndexed { index, mode ->
			val isSelected = mode == selected
			val shape = SegmentedButtonDefaults.itemShape(index, modes.size)

			Surface(
				onClick = { onSelect(mode) },
				shape = shape,
				color = if (isSelected) ButtonDefaults.buttonColors().containerColor else ColorScheme.surfaceContainerHigh,
				contentColor = if (isSelected) ButtonDefaults.buttonColors().contentColor else ColorScheme.onSurface,
				border = BorderStroke(
					width = 1.dp, color = if (isSelected) Transparent else White.copy(alpha = 0.15f)
				),
				modifier = Modifier
					.weight(1f)
					.fillMaxHeight()
					.offset(x = if (index > 0) (-1).dp * index else 0.dp)
					.zIndex(if (isSelected) 1f else 0f)
			) {
				Box(
					contentAlignment = Alignment.Center, modifier = Modifier.padding(
						if (index == 0 || index == modes.size - 1) 12.dp else 8.dp, 4.dp
					)
				) {
					Text(text = mode.label(), fontSize = Typography.bodySmall.fontSize, maxLines = 1)
				}
			}
		}
	}
}

@Preview(widthDp = 960, heightDp = 580)
@Composable
fun MenuDialogPreview() {
	Theme {
		MenuDialog(null, onDismissRequest = { })
	}
}
