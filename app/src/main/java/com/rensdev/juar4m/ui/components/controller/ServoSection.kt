package com.rensdev.juar4m.ui.components.controller

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.rensdev.juar4m.MainViewModel
import com.rensdev.juar4m.command.CommandController
import com.rensdev.juar4m.preferences.RangeState
import com.rensdev.juar4m.preferences.ServoMode
import com.rensdev.juar4m.preferences.UserPreferencesDefaults
import com.rensdev.juar4m.preferences.collectAsStateOrFallback
import com.rensdev.juar4m.ui.theme.Black
import com.rensdev.juar4m.ui.theme.Typography
import com.rensdev.juar4m.ui.theme.White
import com.rensdev.juar4m.ui.theme.colorMix
import com.rensdev.juar4m.utils.VibrationManager
import kotlin.math.roundToInt

val SERVO_ANGLE_BOUNDS = 0f..180f
const val DEGREE_SYMBOL = '\u00B0'

/**
 * Servo slot in the 2-unit-tall grid.
 * POSITIONAL = 1 unit, CONTINUOUS = 2 units.
 */
data class ServoGridItem(
	val servoId: Int, // 1=A, 2=B, 3=C
	val label: AnnotatedString,
	val mode: ServoMode,
	val units: Int, // 1 (positional) or 2 (continuous)
)

/**
 * Packs items into columns of height 2 units, top-to-bottom then left-to-right.
 * An item that doesn't fit the remaining space in the current column starts a new one.
 */
fun packServoColumns(items: List<ServoGridItem>): List<List<ServoGridItem>> {
	val result = mutableListOf<MutableList<ServoGridItem>>()
	var col = mutableListOf<ServoGridItem>()
	var remaining = 2
	for (item in items) {
		if (item.units > remaining) {
			if (col.isNotEmpty()) result += col
			col = mutableListOf()
			remaining = 2
		}
		col += item
		remaining -= item.units
	}
	if (col.isNotEmpty()) result += col
	return result
}

data class ToggleServo(
	val state: RangeState,
	val range: ClosedFloatingPointRange<Float> = 0f..180f,
)

fun ToggleServo.getValue(): Int {
	return when (state) {
		RangeState.LOW -> range.start
		RangeState.HIGH -> range.endInclusive
	}.roundToInt()
}

fun ToggleServo.getColor(): Color {
	return when (state) {
		RangeState.LOW -> ControllerUI.SERVO_TOGGLE_LOW_COLOR
		RangeState.HIGH -> ControllerUI.SERVO_TOGGLE_HIGH_COLOR
	}
}

fun ToggleServo.getHighLow(state: RangeState): Int {
	return when (state) {
		RangeState.LOW -> range.start
		RangeState.HIGH -> range.endInclusive
	}.roundToInt()
}

fun servoLabel(name: String): AnnotatedString {
	return buildAnnotatedString {
		append("Servo ")
		withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
			append(name)
		}
	}
}

@Composable
fun ServoSection(viewModel: MainViewModel?, modifier: Modifier = Modifier) {
	val vibrator = viewModel?.vibrator
	val commandController = viewModel?.commandController

	var isEditingServos by rememberSaveable { mutableStateOf(false) }

	// Stored prefs
	val storedServoAMode by viewModel?.servoAMode.collectAsStateOrFallback(UserPreferencesDefaults.SERVO_A_MODE)
	val storedRangeA by viewModel?.servoARange.collectAsStateOrFallback(UserPreferencesDefaults.SERVO_A_RANGE)
	val storedStateA by viewModel?.servoAState.collectAsStateOrFallback(UserPreferencesDefaults.SERVO_A_STATE)
	val servoAInverted by viewModel?.servoAInverted.collectAsStateOrFallback(UserPreferencesDefaults.SERVO_A_INVERTED)

	val storedServoBMode by viewModel?.servoBMode.collectAsStateOrFallback(UserPreferencesDefaults.SERVO_B_MODE)
	val storedRangeB by viewModel?.servoBRange.collectAsStateOrFallback(UserPreferencesDefaults.SERVO_B_RANGE)
	val storedStateB by viewModel?.servoBState.collectAsStateOrFallback(UserPreferencesDefaults.SERVO_B_STATE)
	val servoBInverted by viewModel?.servoBInverted.collectAsStateOrFallback(UserPreferencesDefaults.SERVO_B_INVERTED)

	val storedServoCMode by viewModel?.servoCMode.collectAsStateOrFallback(UserPreferencesDefaults.SERVO_C_MODE)
	val storedRangeC by viewModel?.servoCRange.collectAsStateOrFallback(UserPreferencesDefaults.SERVO_C_RANGE)
	val storedStateC by viewModel?.servoCState.collectAsStateOrFallback(UserPreferencesDefaults.SERVO_C_STATE)
	val servoCInverted by viewModel?.servoCInverted.collectAsStateOrFallback(UserPreferencesDefaults.SERVO_C_INVERTED)

	val storedSpeedRange by viewModel?.speedRange.collectAsStateOrFallback(UserPreferencesDefaults.SPEED_RANGE)

	val swapDPadAndServoEnabled by viewModel?.swapDPadAndServoEnabled.collectAsStateOrFallback(
		UserPreferencesDefaults.APP_SWAP_DPAD_AND_SERVO_ENABLED
	)

	// Edit state (mirrors stored, mutated locally until save)
	var editRangeA by remember { mutableStateOf(storedRangeA) }
	var editRangeB by remember { mutableStateOf(storedRangeB) }
	var editRangeC by remember { mutableStateOf(storedRangeC) }
	var editSpeedRange by remember { mutableStateOf(storedSpeedRange) }

	LaunchedEffect(storedRangeA, storedRangeB, storedRangeC, storedSpeedRange) {
		editRangeA = storedRangeA
		editRangeB = storedRangeB
		editRangeC = storedRangeC
		editSpeedRange = storedSpeedRange
	}

	val servoA = ToggleServo(storedStateA, editRangeA)
	val servoB = ToggleServo(storedStateB, editRangeB)
	val servoC = ToggleServo(storedStateC, editRangeC)

	val servoALabel = remember { servoLabel("A") }
	val servoBLabel = remember { servoLabel("B") }
	val servoCLabel = remember { servoLabel("C") }

	// Pack servos into 2-unit columns
	val servoColumns = remember(storedServoAMode, storedServoBMode, storedServoCMode) {
		packServoColumns(
			listOf(
				ServoGridItem(
					1, servoALabel, storedServoAMode, if (storedServoAMode == ServoMode.POSITIONAL) 1 else 2
				),
				ServoGridItem(
					2, servoBLabel, storedServoBMode, if (storedServoBMode == ServoMode.POSITIONAL) 1 else 2
				),
				ServoGridItem(
					3, servoCLabel, storedServoCMode, if (storedServoCMode == ServoMode.POSITIONAL) 1 else 2
				),
			)
		)
	}

	// Renders the correct control widget for a given ServoGridItem.
	// Used in both the normal view (all columns) and the editing view (first column only).
	@Composable
	fun ServoItemContent(item: ServoGridItem) {
		when (item.mode) {
			ServoMode.POSITIONAL -> {
				val servo = when (item.servoId) {
					1 -> servoA; 2 -> servoB; else -> servoC
				}
				ToggleServoButton(
					label = item.label, servo = servo, onToggle = {
						val newState = if (servo.state == RangeState.LOW) RangeState.HIGH else RangeState.LOW
						when (item.servoId) {
							1 -> viewModel?.setServoAState(newState)
							2 -> viewModel?.setServoBState(newState)
							else -> viewModel?.setServoCState(newState)
						}
						vibrator?.click()
						commandController?.setServo(item.servoId, servo.copy(state = newState).getValue())
					})
			}

			ServoMode.CONTINUOUS -> {
				val inverted = when (item.servoId) {
					1 -> servoAInverted
					2 -> servoBInverted
					else -> servoCInverted
				}
				ContinuousServoControls(
					modifier = Modifier.fillMaxHeight(),
					vibrator = vibrator,
					commandController = commandController,
					servoId = item.servoId,
					label = item.label,
					inverted = inverted
				)
			}

			else -> {}
		}
	}

	Column(
		modifier = modifier,
		verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
		horizontalAlignment = if (swapDPadAndServoEnabled) Alignment.End else Alignment.Start
	) {
		if (!isEditingServos) {
			Row(
				horizontalArrangement = Arrangement.spacedBy(16.dp),
				verticalAlignment = Alignment.CenterVertically,
				modifier = Modifier.height(ControllerUI.SERVO_CONTAINER_HEIGHT)
			) {
				for (col in servoColumns) {
					Column(modifier = Modifier.fillMaxHeight()) {
						for (item in col) {
							Box(modifier = Modifier.weight(item.units.toFloat())) {
								ServoItemContent(item)
							}
						}
					}
				}
			}
		}

		if (isEditingServos) {
			Row(
				horizontalArrangement = Arrangement.spacedBy(16.dp),
				verticalAlignment = Alignment.Top,
			) {
				val buttonsPreview = @Composable {
					val firstCol = servoColumns.firstOrNull() ?: emptyList()
					if (firstCol.isNotEmpty()) {
						Column(modifier = Modifier.height(ControllerUI.SERVO_CONTAINER_HEIGHT)) {
							for (item in firstCol) {
								Box(modifier = Modifier.weight(item.units.toFloat())) {
									ServoItemContent(item)
								}
							}
						}
					}
				}

				val editSliders = @Composable {
					Column(
						modifier = Modifier
							.weight(1f)
							.verticalScroll(rememberScrollState()),
						verticalArrangement = Arrangement.spacedBy(8.dp)
					) {
						// Servo A — range slider or invert toggle
						if (storedServoAMode == ServoMode.POSITIONAL) {
							ToggleServoRangeSlider(
								servo = servoA.copy(range = editRangeA),
								label = servoALabel,
								onValueChange = { editRangeA = it })
						} else if (storedServoAMode == ServoMode.CONTINUOUS) {
							InvertServoRow(
								letter = "A", inverted = servoAInverted, onCheckedChange = {
									viewModel?.setServoAInverted(it)
									vibrator?.click()
								})
						}

						// Servo B — range slider or invert toggle
						if (storedServoBMode == ServoMode.POSITIONAL) {
							ToggleServoRangeSlider(
								servo = servoB.copy(range = editRangeB),
								label = servoBLabel,
								onValueChange = { editRangeB = it })
						} else if (storedServoBMode == ServoMode.CONTINUOUS) {
							InvertServoRow(
								letter = "B", inverted = servoBInverted, onCheckedChange = {
									viewModel?.setServoBInverted(it)
									vibrator?.click()
								})
						}

						// Servo C — range slider or invert toggle
						if (storedServoCMode == ServoMode.POSITIONAL) {
							ToggleServoRangeSlider(
								servo = servoC.copy(range = editRangeC),
								label = servoCLabel,
								onValueChange = { editRangeC = it })
						} else if (storedServoCMode == ServoMode.CONTINUOUS) {
							InvertServoRow(
								letter = "C", inverted = servoCInverted, onCheckedChange = {
									viewModel?.setServoCInverted(it)
									vibrator?.click()
								})
						}

						SpeedRangeSlider(
							speedRange = editSpeedRange, onValueChange = { editSpeedRange = it })
					}
				}

				if (swapDPadAndServoEnabled) {
					editSliders()
					buttonsPreview()
				} else {
					buttonsPreview()
					editSliders()
				}
			}
		}

		Row(
			horizontalArrangement = Arrangement.spacedBy(
				16.dp, if (swapDPadAndServoEnabled) Alignment.Start else Alignment.End
			), verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()
		) {
			if (isEditingServos) {
				Surface(
					modifier = Modifier
						.height(ButtonDefaults.MinHeight)
						.clip(ButtonDefaults.shape)
						.clickable {
							vibrator?.click()
							editRangeA = UserPreferencesDefaults.SERVO_A_RANGE
							editRangeB = UserPreferencesDefaults.SERVO_B_RANGE
							editRangeC = UserPreferencesDefaults.SERVO_C_RANGE
							editSpeedRange = UserPreferencesDefaults.SPEED_RANGE
							viewModel?.setServoAInverted(UserPreferencesDefaults.SERVO_A_INVERTED)
							viewModel?.setServoBInverted(UserPreferencesDefaults.SERVO_B_INVERTED)
							viewModel?.setServoCInverted(UserPreferencesDefaults.SERVO_C_INVERTED)
						}, shape = ButtonDefaults.shape, color = White.copy(alpha = 0.1f), contentColor = White
				) {
					Box(
						modifier = Modifier.padding(ButtonDefaults.ContentPadding),
						contentAlignment = Alignment.Center
					) {
						Text(text = "Reset to defaults", style = Typography.labelLarge)
					}
				}
			}
			EditServoRangeButton(isEditingServos = isEditingServos, viewModel = viewModel, onEdit = {
				isEditingServos = !isEditingServos
				vibrator?.click()
			}, onSave = {
				viewModel?.setServoARange(editRangeA)
				viewModel?.setServoBRange(editRangeB)
				viewModel?.setServoCRange(editRangeC)
				viewModel?.setSpeedRange(editSpeedRange)
				isEditingServos = !isEditingServos
				vibrator?.click()
				val speedValue =
					if (viewModel?.speedState?.value == RangeState.HIGH) editSpeedRange.endInclusive
					else editSpeedRange.start
				commandController?.syncState(
					speedValue.toInt(),
					storedServoAMode,
					ToggleServo(storedStateA, editRangeA).getValue(),
					storedServoBMode,
					ToggleServo(storedStateB, editRangeB).getValue(),
					storedServoCMode,
					ToggleServo(storedStateC, editRangeC).getValue()
				)
			})
		}
	}
}

/**
 * Invert toggle row shown in the edit panel for any servo in CONTINUOUS mode.
 * [letter] is "A", "B", or "C".
 */
@Composable
fun InvertServoRow(
	letter: String,
	inverted: Boolean,
	onCheckedChange: (Boolean) -> Unit,
) {
	Row(
		verticalAlignment = Alignment.CenterVertically,
		horizontalArrangement = Arrangement.spacedBy((-8).dp)
	) {
		Switch(
			modifier = Modifier
				.rotate(90f)
				.scale(0.7f)
				.height(ButtonDefaults.MinHeight),
			checked = inverted,
			onCheckedChange = onCheckedChange,
			thumbContent = {
				Icon(
					imageVector = Icons.Default.ArrowDropUp,
					contentDescription = null,
					modifier = Modifier.rotate(if (!inverted) -90f else 90f),
					tint = White
				)
			},
			colors = SwitchDefaults.colors(
				uncheckedTrackColor = ControllerUI.SERVO_C_DOWN_COLOR.copy(alpha = 0.15f),
				checkedTrackColor = ControllerUI.SERVO_C_DOWN_COLOR.copy(alpha = 0.15f),
				uncheckedThumbColor = colorMix(Black, ControllerUI.SERVO_C_UP_COLOR, 0.15f),
				checkedThumbColor = colorMix(Black, ControllerUI.SERVO_C_UP_COLOR, 0.15f),
				uncheckedBorderColor = White.copy(alpha = 0.15f),
				checkedBorderColor = White.copy(alpha = 0.15f),
			)
		)
		Text("Invert $letter", style = Typography.labelMedium)
	}
}

@Composable
fun ToggleServoButton(
	modifier: Modifier = Modifier, label: AnnotatedString, servo: ToggleServo, onToggle: () -> Unit
) {
	val shape = when (servo.state) {
		RangeState.LOW -> RoundedCornerShape(
			topStart = ControllerUI.BUTTON_ROUNDNESS,
			topEnd = ControllerUI.BUTTON_ROUNDNESS,
			bottomStart = 0.dp,
			bottomEnd = ControllerUI.BUTTON_ROUNDNESS
		)

		RangeState.HIGH -> RoundedCornerShape(
			topStart = ControllerUI.BUTTON_ROUNDNESS,
			topEnd = ControllerUI.BUTTON_ROUNDNESS,
			bottomStart = ControllerUI.BUTTON_ROUNDNESS,
			bottomEnd = 0.dp
		)
	}

	Row(
		verticalAlignment = Alignment.CenterVertically,
		horizontalArrangement = Arrangement.spacedBy(8.dp)
	) {
		Text(
			label,
			modifier = Modifier
				.vertical()
				.rotate(-90f)
				.alpha(0.5f),
			textAlign = TextAlign.Center,
			style = Typography.labelLarge
		)
		Column {
			Surface(
				modifier = modifier
					.size(ControllerUI.SERVO_BUTTON_SIZE)
					.pointerInput(servo, onToggle) {
						awaitEachGesture {
							awaitFirstDown()
							onToggle()
							waitForUpOrCancellation()
						}
					},
				shape = shape,
				color = servo.getColor(),
				contentColor = ControllerUI.SERVO_TOGGLE_CONTENT_COLOR,
			) {
				Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
					Text("${servo.getValue()}$DEGREE_SYMBOL", style = ControllerUI.SERVO_TEXT_STYLE)
				}
			}
			Row(modifier = Modifier.width(ControllerUI.SERVO_BUTTON_SIZE)) {
				ToggleServoHighLowIndicator(
					modifier = Modifier.weight(1f), targetState = RangeState.LOW, servo = servo
				)
				ToggleServoHighLowIndicator(
					modifier = Modifier.weight(1f), targetState = RangeState.HIGH, servo = servo
				)
			}
		}
	}
}

@Composable
fun ToggleServoHighLowIndicator(
	modifier: Modifier = Modifier, targetState: RangeState, servo: ToggleServo
) {
	val isActive = servo.state == targetState

	Box(
		modifier = modifier.background(
			shape = RoundedCornerShape(
				topStart = 0.dp,
				topEnd = 0.dp,
				bottomStart = ControllerUI.BUTTON_ROUNDNESS,
				bottomEnd = ControllerUI.BUTTON_ROUNDNESS
			), color = if (isActive) servo.getColor() else Color.Transparent
		)
	) {
		Text(
			"${servo.getHighLow(targetState)}$DEGREE_SYMBOL",
			textAlign = TextAlign.Center,
			modifier = Modifier
				.fillMaxWidth()
				.padding(vertical = 2.dp),
			style = Typography.labelMedium
		)
	}
}

fun Modifier.vertical() = layout { measurable, constraints ->
	val placeable = measurable.measure(constraints)
	layout(placeable.height, placeable.width) {
		placeable.place(
			x = -(placeable.width / 2 - placeable.height / 2),
			y = -(placeable.height / 2 - placeable.width / 2)
		)
	}
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun EditServoRangeButton(
	viewModel: MainViewModel?, isEditingServos: Boolean, onEdit: () -> Unit, onSave: () -> Unit
) {
	val backgroundColor =
		if (isEditingServos) IconButtonDefaults.filledIconButtonColors().containerColor
		else White.copy(alpha = 0.1f)
	val contentColor = if (isEditingServos) IconButtonDefaults.filledIconButtonColors().contentColor
	else White.copy(alpha = 0.8f)

	Surface(
		modifier = Modifier
			.size(ButtonDefaults.MinHeight)
			.clip(IconButtonDefaults.filledShape)
			.combinedClickable(
				onClick = {
					if (isEditingServos) onSave()
					else viewModel?.showSnackbar("Long press to edit")
				},
				onLongClick = { onEdit() },
			),
		shape = IconButtonDefaults.filledShape,
		color = backgroundColor,
		contentColor = contentColor
	) {
		Box(contentAlignment = Alignment.Center) {
			Icon(
				imageVector = if (isEditingServos) Icons.Default.Save else Icons.Default.Edit,
				contentDescription = null,
				modifier = Modifier.size(24.dp)
			)
		}
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToggleServoRangeSlider(
	servo: ToggleServo,
	label: AnnotatedString,
	modifier: Modifier = Modifier,
	onValueChange: (ClosedFloatingPointRange<Float>) -> Unit = {}
) {
	Column(verticalArrangement = Arrangement.spacedBy((-10).dp)) {
		Text(text = label, style = Typography.labelLarge, modifier = Modifier.alpha(0.5f))
		RangeSlider(
			modifier = modifier.fillMaxWidth(),
			value = servo.range,
			valueRange = SERVO_ANGLE_BOUNDS,
			onValueChange = onValueChange,
			steps = 180 / 5 - 1,
			colors = SliderDefaults.colors(
				activeTrackColor = White.copy(alpha = 0.25f),
				inactiveTrackColor = Color.Transparent,
				activeTickColor = White.copy(alpha = 0.5f),
				inactiveTickColor = White.copy(alpha = 0.25f),
			),
			startThumb = {
				ToggleServoRangeSliderThumb(
					color = ControllerUI.SERVO_TOGGLE_LOW_COLOR, value = servo.range.start
				)
			},
			endThumb = {
				ToggleServoRangeSliderThumb(
					color = ControllerUI.SERVO_TOGGLE_HIGH_COLOR, value = servo.range.endInclusive
				)
			})
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpeedRangeSlider(
	speedRange: ClosedFloatingPointRange<Float>,
	modifier: Modifier = Modifier,
	onValueChange: (ClosedFloatingPointRange<Float>) -> Unit = {}
) {
	Column(verticalArrangement = Arrangement.spacedBy((-10).dp)) {
		Text(text = "Speed", style = Typography.labelLarge, modifier = Modifier.alpha(0.5f))
		RangeSlider(
			modifier = modifier.fillMaxWidth(),
			value = speedRange,
			valueRange = 0f..255f,
			onValueChange = onValueChange,
			steps = 255 / 5 - 1,
			colors = SliderDefaults.colors(
				activeTrackColor = White.copy(alpha = 0.25f),
				inactiveTrackColor = Color.Transparent,
				activeTickColor = White.copy(alpha = 0.5f),
				inactiveTickColor = White.copy(alpha = 0.25f),
			),
			startThumb = {
				ToggleServoRangeSliderThumb(color = ControllerUI.SPEED_LOW_COLOR, value = speedRange.start)
			},
			endThumb = {
				ToggleServoRangeSliderThumb(
					color = ControllerUI.SPEED_HIGH_COLOR, value = speedRange.endInclusive
				)
			})
	}
}

@Composable
fun ToggleServoRangeSliderThumb(color: Color, value: Float) {
	Box(
		modifier = Modifier
			.size(4.dp, 20.dp)
			.background(color = color, shape = RoundedCornerShape(16.dp)),
		contentAlignment = Alignment.BottomCenter
	) {
		Text(
			text = value.toInt().toString(),
			style = Typography.labelSmall,
			modifier = Modifier
				.offset(y = 16.dp)
				.wrapContentSize(unbounded = true)
		)
	}
}

@Composable
fun ContinuousServoControls(
	modifier: Modifier = Modifier,
	vibrator: VibrationManager?,
	commandController: CommandController?,
	servoId: Int,
	label: AnnotatedString,
	inverted: Boolean = false
) {
	var command by remember { mutableStateOf("RELEASED") }
	val density = LocalDensity.current
	val buttonSize = ControllerUI.SERVO_BUTTON_SIZE

	Row(
		modifier = modifier,
		verticalAlignment = Alignment.CenterVertically,
		horizontalArrangement = Arrangement.spacedBy(8.dp)
	) {
		Text(
			text = label,
			modifier = Modifier
				.vertical()
				.rotate(-90f)
				.alpha(0.5f),
			textAlign = TextAlign.Center,
			style = Typography.labelLarge
		)
		Column(
			verticalArrangement = Arrangement.SpaceBetween,
			modifier = Modifier
				.fillMaxHeight()
				.rotate(if (inverted) 180f else 0f)
				.pointerInput(Unit) {
					val btnPx = with(density) { buttonSize.toPx() }
					awaitEachGesture {
						val down = awaitFirstDown()
						val width = size.width.toFloat()
						val height = size.height.toFloat()

						val topBounds = Rect(0f, 0f, width, btnPx)
						val btmBounds = Rect(0f, height - btnPx, width, height)

						fun updateCommand(pos: Offset) {
							val newCommand = when {
								topBounds.contains(pos) -> "UP"
								btmBounds.contains(pos) -> "DOWN"
								else -> "RELEASED"
							}
							if (command != newCommand) {
								command = newCommand
								if (command != "RELEASED") vibrator?.strongClick()
								val angle = when (command) {
									"UP" -> 180
									"DOWN" -> 0
									else -> 90
								}
								commandController?.setServo(servoId, angle)
							}
						}

						updateCommand(down.position)

						do {
							val event = awaitPointerEvent()
							updateCommand(event.changes.first().position)
						} while (event.changes.any { it.pressed })

						if (command != "RELEASED") {
							command = "RELEASED"
							commandController?.setServo(servoId, 90)
						}
					}
				}) {
			ContinuousServoButton(
				color = ControllerUI.SERVO_C_UP_COLOR,
				iconImageVector = Icons.Default.ArrowDropUp,
				isActive = command == "UP"
			)
			ContinuousServoButton(
				color = ControllerUI.SERVO_C_DOWN_COLOR,
				iconImageVector = Icons.Default.ArrowDropDown,
				isActive = command == "DOWN"
			)
		}
	}
}

@Composable
fun ContinuousServoButton(
	modifier: Modifier = Modifier, color: Color, iconImageVector: ImageVector, isActive: Boolean
) {
	Surface(
		modifier = modifier.size(ControllerUI.SERVO_BUTTON_SIZE),
		shape = RoundedCornerShape(ControllerUI.BUTTON_ROUNDNESS),
		contentColor = White,
		color = color.copy(alpha = if (isActive) 0.3f else 0.15f)
	) {
		Icon(
			imageVector = iconImageVector,
			contentDescription = null,
			modifier = Modifier.size(ControllerUI.ICON_SIZE)
		)
	}
}