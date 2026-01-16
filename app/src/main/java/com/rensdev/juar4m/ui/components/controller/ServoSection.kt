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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.rensdev.juar4m.preferences.UserPreferencesDefaults
import com.rensdev.juar4m.preferences.collectAsStateOrFallback
import com.rensdev.juar4m.ui.theme.Black
import com.rensdev.juar4m.ui.theme.Typography
import com.rensdev.juar4m.ui.theme.White
import com.rensdev.juar4m.ui.theme.colorMix
import com.rensdev.juar4m.utils.VibrationManager
import kotlin.math.roundToInt

enum class ServoState { LOW, HIGH }

val SERVO_ANGLE_BOUNDS = 0f..180f
const val DEGREE_SYMBOL = '\u00B0'

data class ToggleServo(
	val state: ServoState,
	val range: ClosedFloatingPointRange<Float> = 0f..180f,
)

fun ToggleServo.getValue(): Int {
	return when (state) {
		ServoState.LOW -> range.start
		ServoState.HIGH -> range.endInclusive
	}.roundToInt()
}

fun ToggleServo.getColor(): Color {
	return when (state) {
		ServoState.LOW -> ControllerUI.SERVO_TOGGLE_LOW_COLOR
		ServoState.HIGH -> ControllerUI.SERVO_TOGGLE_HIGH_COLOR
	}
}

fun ToggleServo.getHighLow(state: ServoState): Int {
	return when (state) {
		ServoState.LOW -> range.start
		ServoState.HIGH -> range.endInclusive
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

	var isEditingRanges by rememberSaveable { mutableStateOf(false) }

	val storedRangeA by viewModel?.servoARange.collectAsStateOrFallback(UserPreferencesDefaults.SERVO_A_RANGE)
	val storedStateA by viewModel?.servoAState.collectAsStateOrFallback(UserPreferencesDefaults.SERVO_A_STATE)

	val storedRangeB by viewModel?.servoBRange.collectAsStateOrFallback(UserPreferencesDefaults.SERVO_B_RANGE)
	val storedStateB by viewModel?.servoBState.collectAsStateOrFallback(UserPreferencesDefaults.SERVO_B_STATE)

	val servoCInverted by viewModel?.servoCInverted.collectAsStateOrFallback(UserPreferencesDefaults.SERVO_C_INVERTED)

	var editRangeA by remember { mutableStateOf(storedRangeA) }
	var editRangeB by remember { mutableStateOf(storedRangeB) }

	LaunchedEffect(storedRangeA, storedRangeB) {
		if (!isEditingRanges) {
			editRangeA = storedRangeA
			editRangeB = storedRangeB
		}
	}

	val servoA = ToggleServo(storedStateA, editRangeA)
	val servoB = ToggleServo(storedStateB, editRangeB)

	val servoALabel = remember { servoLabel("A") }
	val servoBLabel = remember { servoLabel("B") }

	Column(
		modifier = modifier,
		verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
	) {
		Row(
			horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.Top
		) {
			Column(
				verticalArrangement = Arrangement.SpaceBetween,
				modifier = Modifier.height(ControllerUI.SERVO_CONTAINER_HEIGHT)
			) {
				ToggleServoButton(label = servoALabel, servo = servoA, onToggle = {
					val newState = if (servoA.state == ServoState.LOW) ServoState.HIGH else ServoState.LOW
					viewModel?.setServoAState(newState)
					vibrator?.click()
					commandController?.setServo(1, servoA.copy(state = newState).getValue())
				})
				ToggleServoButton(label = servoBLabel, servo = servoB, onToggle = {
					val newState = if (servoB.state == ServoState.LOW) ServoState.HIGH else ServoState.LOW
					viewModel?.setServoBState(newState)
					vibrator?.click()
					commandController?.setServo(2, servoB.copy(state = newState).getValue())
				})
			}

			if (!isEditingRanges) {
				ServoCControls(
					vibrator = vibrator,
					commandController = commandController,
					inverted = servoCInverted
				)
			} else {
				Column(
					verticalArrangement = Arrangement.spacedBy(8.dp),
					modifier = Modifier.height(ControllerUI.SERVO_CONTAINER_HEIGHT)
				) {
					ToggleServoRangeSlider(servo = servoA, label = servoALabel, onValueChange = {
						editRangeA = it
					})
					Spacer(modifier = Modifier.height(24.dp))
					ToggleServoRangeSlider(servo = servoB, label = servoBLabel, onValueChange = {
						editRangeB = it
					})
				}
			}
		}

		Row(
			horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.End),
			verticalAlignment = Alignment.CenterVertically,
			modifier = Modifier.fillMaxWidth()
		) {
			if (isEditingRanges) {
				Row(
					verticalAlignment = Alignment.CenterVertically,
					horizontalArrangement = Arrangement.spacedBy((-8).dp)
				) {
					Switch(
						modifier = Modifier
							.rotate(90f)
							.scale(0.7f)
							.height(ButtonDefaults.MinHeight),
						checked = servoCInverted,
						onCheckedChange = {
							viewModel?.setServoCInverted(it)
						},
						thumbContent = {
							Icon(
								imageVector = Icons.Default.ArrowDropUp,
								contentDescription = null,
								modifier = Modifier.rotate(if (!servoCInverted) -90f else 90f),
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
					Text("Invert Servo C", style = Typography.labelMedium)
				}
				Surface(
					modifier = Modifier
						.height(ButtonDefaults.MinHeight)
						.clip(RoundedCornerShape(ControllerUI.BUTTON_ROUNDNESS))
						.clickable {
							vibrator?.click()
							editRangeA = UserPreferencesDefaults.SERVO_A_RANGE
							editRangeB = UserPreferencesDefaults.SERVO_B_RANGE
							viewModel?.setServoCInverted(UserPreferencesDefaults.SERVO_C_INVERTED)
						},
					shape = ButtonDefaults.shape,
					color = White.copy(alpha = 0.1f),
					contentColor = White
				) {
					Box(
						modifier = Modifier.padding(ButtonDefaults.ContentPadding),
						contentAlignment = Alignment.Center
					) {
						Text(
							text = "Reset to defaults",
							style = Typography.labelLarge
						)
					}
				}
			}
			EditServoRangeButton(isEditingRanges = isEditingRanges, viewModel = viewModel, onEdit = {
				isEditingRanges = !isEditingRanges
				vibrator?.click()
			}, onSave = {
				viewModel?.setServoARange(editRangeA)
				viewModel?.setServoBRange(editRangeB)

				isEditingRanges = !isEditingRanges
				vibrator?.click()
			})
		}
	}
}

@Composable
fun ToggleServoButton(
	modifier: Modifier = Modifier, label: AnnotatedString, servo: ToggleServo, onToggle: () -> Unit
) {
	val shape = when (servo.state) {
		ServoState.LOW -> RoundedCornerShape(
			topStart = ControllerUI.BUTTON_ROUNDNESS,
			topEnd = ControllerUI.BUTTON_ROUNDNESS,
			bottomStart = 0.dp,
			bottomEnd = ControllerUI.BUTTON_ROUNDNESS
		)

		ServoState.HIGH -> RoundedCornerShape(
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
					modifier = Modifier.weight(1f),
					targetState = ServoState.LOW,
					servo = servo,
				)
				ToggleServoHighLowIndicator(
					modifier = Modifier.weight(1f),
					targetState = ServoState.HIGH,
					servo = servo,
				)
			}
		}
	}
}

@Composable
fun ToggleServoHighLowIndicator(
	modifier: Modifier = Modifier, targetState: ServoState, servo: ToggleServo
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
	viewModel: MainViewModel?, isEditingRanges: Boolean, onEdit: () -> Unit, onSave: () -> Unit
) {
	val backgroundColor =
		if (isEditingRanges) IconButtonDefaults.filledIconButtonColors().containerColor
		else White.copy(alpha = 0.1f)
	val contentColor = if (isEditingRanges) IconButtonDefaults.filledIconButtonColors().contentColor
	else White.copy(alpha = 0.8f)

	Surface(
		modifier = Modifier
			.size(ButtonDefaults.MinHeight)
			.clip(IconButtonDefaults.filledShape)
			.combinedClickable(
				onClick = {
					if (isEditingRanges) onSave()
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
				imageVector = if (isEditingRanges) Icons.Default.Save else Icons.Default.Edit,
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
	Text(
		text = label, style = Typography.labelLarge, modifier = Modifier.alpha(0.5f)
	)
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

@Composable
fun ToggleServoRangeSliderThumb(color: Color, value: Float) {
	Box(
		modifier = Modifier
			.size(4.dp, 32.dp)
			.background(color = color, shape = RoundedCornerShape(16.dp)),
		contentAlignment = Alignment.BottomCenter
	) {
		Text(
			text = value.toInt().toString(),
			style = Typography.labelSmall,
			modifier = Modifier
				.offset(y = 20.dp)
				.wrapContentSize(unbounded = true)
		)
	}
}

@Composable
fun ServoCControls(
	modifier: Modifier = Modifier,
	vibrator: VibrationManager?,
	commandController: CommandController?,
	inverted: Boolean = false
) {
	var command by remember { mutableStateOf("RELEASED") }
	val density = LocalDensity.current
	val buttonSize = ControllerUI.SERVO_BUTTON_SIZE
	val servoCLabel = remember { servoLabel("C") }

	Row(
		modifier = modifier.height(ControllerUI.SERVO_CONTAINER_HEIGHT),
		verticalAlignment = Alignment.CenterVertically,
		horizontalArrangement = Arrangement.spacedBy(8.dp)
	) {
		Text(
			text = servoCLabel,
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
								commandController?.setServo(3, angle)
							}
						}

						updateCommand(down.position)

						do {
							val event = awaitPointerEvent()
							updateCommand(event.changes.first().position)
						} while (event.changes.any { it.pressed })

						if (command != "RELEASED") {
							command = "RELEASED"
							commandController?.setServo(3, 90)
						}
					}
				}) {
			ServoCButton(
				color = ControllerUI.SERVO_C_UP_COLOR,
				iconImageVector = Icons.Default.ArrowDropUp,
				isActive = command == "UP"
			)
			ServoCButton(
				color = ControllerUI.SERVO_C_DOWN_COLOR,
				iconImageVector = Icons.Default.ArrowDropDown,
				isActive = command == "DOWN"
			)
		}
	}
}

@Composable
fun ServoCButton(
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
