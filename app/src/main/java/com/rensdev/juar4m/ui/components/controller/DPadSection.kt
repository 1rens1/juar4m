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
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.DpSize
import com.rensdev.juar4m.MainViewModel
import com.rensdev.juar4m.utils.VibrationManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class DPadController(private val vibrator: VibrationManager?, private val scope: CoroutineScope) {
	private var parentCoordinates: LayoutCoordinates? = null
	private val componentBounds = mutableMapOf<String, Rect>()

	var activeKey by mutableStateOf("STOP")
		private set
	var activeNudge by mutableStateOf<String?>(null)
		private set

	fun updateParentCoordinates(coordinates: LayoutCoordinates) {
		parentCoordinates = coordinates
	}

	fun updateComponentBounds(key: String, coordinates: LayoutCoordinates) {
		val parent = parentCoordinates ?: return
		if (!parent.isAttached) return
		val topLeft = parent.localPositionOf(coordinates, Offset.Zero)
		val size = coordinates.size
		componentBounds[key] = Rect(topLeft, Size(size.width.toFloat(), size.height.toFloat()))
	}

	suspend fun processGestures(inputScope: PointerInputScope) {
		inputScope.awaitEachGesture {
			val down = awaitFirstDown()

			val startKey = componentBounds.entries.firstOrNull { it.value.contains(down.position) }?.key

			if (startKey != null && startKey.startsWith("NUDGE")) {
				activeKey = "STOP"
				vibrator?.cancel()
				triggerNudge(startKey)

				do {
					val event = awaitPointerEvent()
				} while (event.changes.any { it.pressed })

			} else {

				fun processDPadLogic(position: Offset) {
					val hitKey = componentBounds.entries.firstOrNull { it.value.contains(position) }?.key

					if (hitKey != null && !hitKey.startsWith("NUDGE")) {
						if (activeKey != hitKey) {
							activeKey = hitKey
							vibrator?.weakContinuous()
						}
					} else {
						activeKey = "STOP"
						vibrator?.cancel()
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

			activeKey = "STOP"
			vibrator?.cancel()
		}
	}

	private fun triggerNudge(key: String) {
		vibrator?.click()
		scope.launch {
			activeNudge = key
			delay(ControllerUI.NUDGE_FLASH_DURATION_MILLIS)
			if (activeNudge == key) activeNudge = null
		}
	}
}

@Composable
fun rememberDPadController(vibrator: VibrationManager?): DPadController {
	val scope = rememberCoroutineScope()
	return remember { DPadController(vibrator, scope) }
}


fun Modifier.dpadController(controller: DPadController): Modifier =
	this
		.onGloballyPositioned { controller.updateParentCoordinates(it) }
		.pointerInput(controller) { controller.processGestures(this) }

fun Modifier.dpadTag(controller: DPadController, key: String): Modifier =
	this.onGloballyPositioned { controller.updateComponentBounds(key, it) }


@Composable
fun DPadSection(viewModel: MainViewModel?, modifier: Modifier = Modifier) {
	val vibrator = viewModel?.vibrationManager
	val controller = rememberDPadController(vibrator)

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
				isActive = controller.activeNudge == "NUDGE_UP",
				modifier = Modifier.dpadTag(controller, "NUDGE_UP")
			) {
				Icon(
					modifier = Modifier.size(ControllerUI.ICON_SIZE),
					imageVector = Icons.Default.KeyboardDoubleArrowUp,
					contentDescription = null
				)
			}

			DPadButton(
				isActive = controller.activeKey == "UP", modifier = Modifier.dpadTag(controller, "UP")
			) {
				Icon(
					modifier = Modifier.size(ControllerUI.ICON_SIZE),
					imageVector = Icons.Default.ArrowUpward,
					contentDescription = null
				)
			}

			Row(horizontalArrangement = Arrangement.spacedBy(ControllerUI.DPAD_GAP)) {
				NudgeButton(
					orientation = Orientation.Vertical,
					isActive = controller.activeNudge == "NUDGE_LEFT",
					modifier = Modifier.dpadTag(controller, "NUDGE_LEFT")
				) {
					Icon(
						modifier = Modifier.size(ControllerUI.ICON_SIZE),
						imageVector = Icons.Default.KeyboardDoubleArrowLeft,
						contentDescription = null
					)
				}
				DPadButton(
					isActive = controller.activeKey == "LEFT", modifier = Modifier.dpadTag(controller, "LEFT")
				) {
					Icon(
						modifier = Modifier.size(ControllerUI.ICON_SIZE),
						imageVector = Icons.AutoMirrored.Default.ArrowBack,
						contentDescription = null
					)
				}
				Spacer(modifier = Modifier.size(ControllerUI.BUTTON_SIZE))
				DPadButton(
					isActive = controller.activeKey == "RIGHT",
					modifier = Modifier.dpadTag(controller, "RIGHT")
				) {
					Icon(
						modifier = Modifier.size(ControllerUI.ICON_SIZE),
						imageVector = Icons.AutoMirrored.Default.ArrowForward,
						contentDescription = null
					)
				}
				NudgeButton(
					orientation = Orientation.Vertical,
					isActive = controller.activeNudge == "NUDGE_RIGHT",
					modifier = Modifier.dpadTag(controller, "NUDGE_RIGHT")
				) {
					Icon(
						modifier = Modifier.size(ControllerUI.ICON_SIZE),
						imageVector = Icons.Default.KeyboardDoubleArrowRight,
						contentDescription = null
					)
				}
			}

			DPadButton(
				isActive = controller.activeKey == "DOWN", modifier = Modifier.dpadTag(controller, "DOWN")
			) {
				Icon(
					modifier = Modifier.size(ControllerUI.ICON_SIZE),
					imageVector = Icons.Default.ArrowDownward,
					contentDescription = null
				)
			}

			NudgeButton(
				orientation = Orientation.Horizontal,
				isActive = controller.activeNudge == "NUDGE_DOWN",
				modifier = Modifier.dpadTag(controller, "NUDGE_DOWN")
			) {
				Icon(
					modifier = Modifier.size(ControllerUI.ICON_SIZE),
					imageVector = Icons.Default.KeyboardDoubleArrowDown,
					contentDescription = null
				)
			}
		}
	}
}

@Composable
fun DPadButton(
	modifier: Modifier = Modifier, isActive: Boolean = false, content: @Composable () -> Unit
) {
	Surface(
		modifier = modifier.size(ControllerUI.BUTTON_SIZE),
		color = if (isActive) ControllerUI.DPAD_ACTIVE_COLOR else ControllerUI.DPAD_COLOR,
		contentColor = if (isActive) ControllerUI.DPAD_ACTIVE_CONTENT_COLOR else ControllerUI.DPAD_CONTENT_COLOR,
		shape = RoundedCornerShape(ControllerUI.BUTTON_ROUNDNESS)
	) {
		Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { content() }
	}
}

@Composable
fun NudgeButton(
	modifier: Modifier = Modifier,
	orientation: Orientation,
	isActive: Boolean = false,
	content: @Composable () -> Unit
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
		Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { content() }
	}
}
