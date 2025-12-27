package com.rensdev.juar4m.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rensdev.juar4m.MainViewModel
import com.rensdev.juar4m.ui.theme.Theme
import com.rensdev.juar4m.ui.theme.White

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MenuButton(modifier: Modifier = Modifier, viewModel: MainViewModel?) {
	var showMenu by remember { mutableStateOf(false) }

	Surface(
		modifier = modifier
			.size(40.dp)
			.clip(CircleShape)
			.combinedClickable(
				onClick = {
					viewModel?.showSnackbar("Long press to open menu")
				},
				onLongClick = {
					showMenu = true
				}
			),
		shape = CircleShape,
		color = White.copy(alpha = 0.1f),
		contentColor = White.copy(alpha = 0.8f)
	) {
		Box(contentAlignment = Alignment.Center) {
			Icon(
				imageVector = Icons.Default.Menu,
				contentDescription = null
			)
		}
	}

	if (showMenu) {
		MenuDialog(viewModel = viewModel, onDismissRequest = { showMenu = false })
	}
}

@Preview
@Composable
fun MenuButtonPreview() {
	Theme {
		MenuButton(viewModel = null)
	}
}
