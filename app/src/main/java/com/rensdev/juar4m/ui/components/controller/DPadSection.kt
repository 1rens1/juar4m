package com.rensdev.juar4m.ui.components.controller

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.KeyboardDoubleArrowDown
import androidx.compose.material.icons.filled.KeyboardDoubleArrowLeft
import androidx.compose.material.icons.filled.KeyboardDoubleArrowRight
import androidx.compose.material.icons.filled.KeyboardDoubleArrowUp
import androidx.compose.material.icons.filled.NorthEast
import androidx.compose.material.icons.filled.NorthWest
import androidx.compose.material.icons.filled.SouthEast
import androidx.compose.material.icons.filled.SouthWest
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.DpSize
import com.rensdev.juar4m.MainViewModel
import com.rensdev.juar4m.command.CommandController
import com.rensdev.juar4m.command.Commands
import com.rensdev.juar4m.preferences.UserPreferencesDefaults
import com.rensdev.juar4m.preferences.collectAsStateOrFallback
import com.rensdev.juar4m.stopwatch.StopwatchManager
import com.rensdev.juar4m.utils.VibrationManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class DPadController(
	private val commandController: CommandController?,
	private val stopwatchManager: StopwatchManager?,
	private val vibrator: VibrationManager?,
	private val scope: CoroutineScope
) {
	private var parentCoordinates: LayoutCoordinates? = null
	private val componentBounds = mutableMapOf<Int, Rect>()

	var activeKey by mutableIntStateOf(Commands.STOP_MOTORS)
		private set

	var activeNudge by mutableStateOf<Int?>(null)
		private set

	fun updateParentCoordinates(coordinates: LayoutCoordinates) {
		parentCoordinates = coordinates
	}

	fun updateComponentBounds(command: Int, coordinates: LayoutCoordinates) {
		val parent = parentCoordinates ?: return
		if (!parent.isAttached) return
		val topLeft = parent.localPositionOf(coordinates, Offset.Zero)
		val size = coordinates.size
		componentBounds[command] = Rect(topLeft, Size(size.width.toFloat(), size.height.toFloat()))
	}

	suspend fun processGestures(inputScope: PointerInputScope) {
		inputScope.awaitEachGesture {
			val down = awaitFirstDown()

			val startCommand =
				componentBounds.entries.firstOrNull { it.value.contains(down.position) }?.key

			if (startCommand != null && isNudge(startCommand)) {
				// Handle Nudge (Click)
				activeKey = Commands.STOP_MOTORS
				vibrator?.cancel()
				commandController?.stop()
				triggerNudge(startCommand)

				// Wait for release
				do {
					val event = awaitPointerEvent()
				} while (event.changes.any { it.pressed })

			} else {
				// Handle Drive (Drag)
				fun processDPadLogic(position: Offset) {
					val hitCommand = componentBounds.entries.firstOrNull { it.value.contains(position) }?.key

					if (hitCommand != null && !isNudge(hitCommand)) {
						if (activeKey != hitCommand) {
							activeKey = hitCommand
							stopwatchManager?.onInputDetected()
							vibrator?.click()
							vibrator?.weakContinuous()
							commandController?.drive(hitCommand) // Direct Int pass
						}
					} else {
						// Finger drifted off valid buttons
						activeKey = Commands.STOP_MOTORS
						vibrator?.cancel()
						commandController?.stop()
					}
				}

				processDPadLogic(down.position)

				do {
					val event = awaitPointerEvent()
					val change = event.changes.firstOrNull()
					if (change != null && change.pressed) {
						processDPadLogic(change.position)
					}
				} while (event.changes.any { it.pressed })
			}

			// Release
			activeKey = Commands.STOP_MOTORS
			vibrator?.cancel()
			commandController?.stop()
		}
	}

	private fun triggerNudge(command: Int) {
		vibrator?.click()
		commandController?.nudge(command)
		scope.launch {
			activeNudge = command
			delay(ControllerUI.NUDGE_FLASH_DURATION_MILLIS)
			if (activeNudge == command) activeNudge = null
		}
	}

	private fun isNudge(command: Int): Boolean {
		return command in Commands.NUDGE_FORWARD..Commands.NUDGE_SPIN_LEFT
	}
}

@Composable
fun rememberDPadController(viewModel: MainViewModel?): DPadController {
	val scope = rememberCoroutineScope()
	val vibrator = viewModel?.vibrator
	val commandController = viewModel?.commandController
	val stopwatchManager = viewModel?.stopwatchManager
	return remember(vibrator, commandController) {
		DPadController(commandController, stopwatchManager, vibrator, scope)
	}
}


fun Modifier.dpadController(controller: DPadController): Modifier =
	this
		.onGloballyPositioned { controller.updateParentCoordinates(it) }
		.pointerInput(controller) { controller.processGestures(this) }

fun Modifier.dpadTag(controller: DPadController, command: Int): Modifier =
	this.onGloballyPositioned { controller.updateComponentBounds(command, it) }


@Composable
fun DPadSection(viewModel: MainViewModel?, modifier: Modifier = Modifier) {
	val controller = rememberDPadController(viewModel)
	val cornerDPadButtons by viewModel?.cornerDPadButtonsEnabled.collectAsStateOrFallback(
		UserPreferencesDefaults.APP_CORNER_DPAD_BUTTONS_ENABLED
	)

	Box(
		modifier = modifier
			.fillMaxSize()
			.dpadController(controller),
		contentAlignment = Alignment.Center
	) {
		Column(
			horizontalAlignment = Alignment.CenterHorizontally,
			verticalArrangement = Arrangement.spacedBy(ControllerUI.DPAD_GAP)
		) {
			NudgeButton(
				orientation = Orientation.Horizontal,
				isActive = controller.activeNudge == Commands.NUDGE_FORWARD,
				iconImageVector = Icons.Default.KeyboardDoubleArrowUp,
				modifier = Modifier.dpadTag(controller, Commands.NUDGE_FORWARD)
			)

			Row(
				horizontalArrangement = Arrangement.spacedBy(ControllerUI.DPAD_GAP),
				verticalAlignment = Alignment.Bottom
			) {
				if (cornerDPadButtons) DPadButton(
					isCorner = true,
					isActive = controller.activeKey == Commands.MOVE_FORWARD_LEFT,
					iconImageVector = Icons.Default.NorthWest,
					modifier = Modifier.dpadTag(controller, Commands.MOVE_FORWARD_LEFT)
				)
				DPadButton(
					isActive = controller.activeKey == Commands.MOVE_FORWARD,
					iconImageVector = Icons.Default.ArrowUpward,
					modifier = Modifier.dpadTag(controller, Commands.MOVE_FORWARD)
				)
				if (cornerDPadButtons) DPadButton(
					isCorner = true,
					isActive = controller.activeKey == Commands.MOVE_FORWARD_RIGHT,
					iconImageVector = Icons.Default.NorthEast,
					modifier = Modifier.dpadTag(controller, Commands.MOVE_FORWARD_RIGHT)
				)
			}


			Row(horizontalArrangement = Arrangement.spacedBy(ControllerUI.DPAD_GAP)) {
				NudgeButton(
					orientation = Orientation.Vertical,
					isActive = controller.activeNudge == Commands.NUDGE_SPIN_LEFT,
					iconImageVector = Icons.Default.KeyboardDoubleArrowLeft,
					modifier = Modifier.dpadTag(controller, Commands.NUDGE_SPIN_LEFT)
				)
				DPadButton(
					isActive = controller.activeKey == Commands.SPIN_LEFT,
					iconImageVector = Icons.AutoMirrored.Default.ArrowBack,
					modifier = Modifier.dpadTag(controller, Commands.SPIN_LEFT)
				)
				Spacer(modifier = Modifier.size(ControllerUI.BUTTON_SIZE))
				DPadButton(
					isActive = controller.activeKey == Commands.SPIN_RIGHT,
					iconImageVector = Icons.AutoMirrored.Default.ArrowForward,
					modifier = Modifier.dpadTag(controller, Commands.SPIN_RIGHT)
				)
				NudgeButton(
					orientation = Orientation.Vertical,
					isActive = controller.activeNudge == Commands.NUDGE_SPIN_RIGHT,
					iconImageVector = Icons.Default.KeyboardDoubleArrowRight,
					modifier = Modifier.dpadTag(controller, Commands.NUDGE_SPIN_RIGHT)
				)
			}

			Row(horizontalArrangement = Arrangement.spacedBy(ControllerUI.DPAD_GAP)) {
				if (cornerDPadButtons) DPadButton(
					isCorner = true,
					isActive = controller.activeKey == Commands.MOVE_BACKWARD_LEFT,
					iconImageVector = Icons.Default.SouthWest,
					modifier = Modifier.dpadTag(controller, Commands.MOVE_BACKWARD_LEFT)
				)
				DPadButton(
					isActive = controller.activeKey == Commands.MOVE_BACKWARD,
					iconImageVector = Icons.Default.ArrowDownward,
					modifier = Modifier.dpadTag(controller, Commands.MOVE_BACKWARD)
				)
				if (cornerDPadButtons) DPadButton(
					isCorner = true,
					isActive = controller.activeKey == Commands.MOVE_BACKWARD_RIGHT,
					iconImageVector = Icons.Default.SouthEast,
					modifier = Modifier.dpadTag(controller, Commands.MOVE_BACKWARD_RIGHT)
				)
			}

			NudgeButton(
				orientation = Orientation.Horizontal,
				isActive = controller.activeNudge == Commands.NUDGE_BACKWARD,
				iconImageVector = Icons.Default.KeyboardDoubleArrowDown,
				modifier = Modifier.dpadTag(controller, Commands.NUDGE_BACKWARD)
			)
		}
	}
}

@Composable
fun DPadButton(
	modifier: Modifier = Modifier,
	isActive: Boolean = false,
	iconImageVector: ImageVector,
	isCorner: Boolean = false
) {
	Surface(
		modifier = modifier.size(if (isCorner) ControllerUI.CORNER_BUTTON_SIZE else ControllerUI.BUTTON_SIZE),
		color = if (isActive) ControllerUI.DPAD_ACTIVE_COLOR else if (isCorner) ControllerUI.DPAD_CORNER_COLOR else ControllerUI.DPAD_COLOR,
		contentColor = if (isActive) ControllerUI.DPAD_ACTIVE_CONTENT_COLOR else ControllerUI.DPAD_CONTENT_COLOR,
		shape = RoundedCornerShape(ControllerUI.BUTTON_ROUNDNESS)
	) {
		Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
			Icon(
				modifier = Modifier.size(ControllerUI.ICON_SIZE),
				imageVector = iconImageVector,
				contentDescription = null
			)
		}
	}
}

@Composable
fun NudgeButton(
	modifier: Modifier = Modifier,
	orientation: Orientation,
	isActive: Boolean = false,
	iconImageVector: ImageVector
) {
	Surface(
		modifier = modifier
			.padding(
				when (orientation) {
					Orientation.Vertical -> PaddingValues(horizontal = ControllerUI.NUDGE_PADDING)
					Orientation.Horizontal -> PaddingValues(vertical = ControllerUI.NUDGE_PADDING)
				}
			)
			.size(
				when (orientation) {
					Orientation.Vertical -> DpSize(ControllerUI.NUDGE_THINNESS, ControllerUI.BUTTON_SIZE)
					Orientation.Horizontal -> DpSize(ControllerUI.BUTTON_SIZE, ControllerUI.NUDGE_THINNESS)
				}
			),
		color = if (isActive) ControllerUI.NUDGE_ACTIVE_COLOR else ControllerUI.NUDGE_COLOR,
		contentColor = if (isActive) ControllerUI.NUDGE_ACTIVE_CONTENT_COLOR else ControllerUI.NUDGE_CONTENT_COLOR,
		shape = RoundedCornerShape(ControllerUI.BUTTON_ROUNDNESS)
	) {
		Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
			Icon(
				modifier = Modifier.size(ControllerUI.ICON_SIZE),
				imageVector = iconImageVector,
				contentDescription = null
			)
		}
	}
}
