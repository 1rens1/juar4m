package com.rensdev.juar4m.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun AnimatedSideDialog(
	onDismissRequest: () -> Unit,
	content: @Composable (dismiss: () -> Unit) -> Unit
) {
	var visible by remember { mutableStateOf(false) }
	val scope = rememberCoroutineScope()
	val animationDurationMillis = 200

	LaunchedEffect(Unit) { visible = true }

	fun dismiss() {
		scope.launch {
			visible = false
			delay(animationDurationMillis.toLong())
			onDismissRequest()
		}
	}

	Dialog(
		onDismissRequest = ::dismiss,
		properties = DialogProperties(usePlatformDefaultWidth = false)
	) {
		val window = (LocalView.current.parent as? DialogWindowProvider)?.window
		SideEffect {
			window?.setDimAmount(0f)
			window?.setWindowAnimations(-1)
		}

		Box(modifier = Modifier.fillMaxSize()) {
			AnimatedVisibility(
				visible = visible,
				enter = fadeIn(tween(animationDurationMillis)),
				exit = fadeOut(tween(animationDurationMillis))
			) {
				Box(
					modifier = Modifier
						.fillMaxSize()
						.background(Color.Black.copy(alpha = 0.5f))
						.clickable(
							indication = null,
							interactionSource = remember { MutableInteractionSource() }) { dismiss() }
				)
			}
			AnimatedVisibility(
				visible = visible,
				enter = slideInHorizontally(
					initialOffsetX = { -it }, animationSpec = tween(animationDurationMillis)
				) + fadeIn(animationSpec = tween(animationDurationMillis)),
				exit = slideOutHorizontally(
					targetOffsetX = { -it }, animationSpec = tween(animationDurationMillis)
				) + fadeOut(animationSpec = tween(animationDurationMillis)),
				modifier = Modifier.align(Alignment.CenterStart)
			) {
				content(::dismiss)
			}
		}
	}
}

