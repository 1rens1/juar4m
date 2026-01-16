package com.rensdev.juar4m.ui.components

import android.bluetooth.BluetoothDevice
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import com.rensdev.juar4m.bluetooth.BluetoothStatus
import com.rensdev.juar4m.bluetooth.getNameSafe
import com.rensdev.juar4m.ui.theme.Black
import com.rensdev.juar4m.ui.theme.ColorScheme
import com.rensdev.juar4m.ui.theme.Green
import com.rensdev.juar4m.ui.theme.Red
import com.rensdev.juar4m.ui.theme.Theme
import com.rensdev.juar4m.ui.theme.Transparent
import com.rensdev.juar4m.ui.theme.Typography
import com.rensdev.juar4m.ui.theme.White
import com.rensdev.juar4m.ui.theme.Yellow
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun BluetoothDevicesListDialog(
	lastDeviceAddress: String?,
	favoriteDevices: Set<String>,
	toggleFavoriteDevice: (String) -> Unit,
	bluetoothStatus: BluetoothStatus?,
	connectedDevice: BluetoothDevice?,
	bondedDevices: List<BluetoothDeviceInfo>,
	onClose: () -> Unit,
	onConnect: (BluetoothDeviceInfo) -> Unit,
	onRefresh: () -> Unit,
	onDisconnect: () -> Unit,
	isRefreshing: Boolean = false
) {
	var showDisconnectDialog by remember { mutableStateOf(false) }

	Dialog(
		onDismissRequest = onClose,
		properties = DialogProperties(
			usePlatformDefaultWidth = false
		),
	) {
		val dialogWindow = (LocalView.current.parent as? DialogWindowProvider)?.window

		SideEffect {
			dialogWindow?.apply {
				setWindowAnimations(0)
				setDimAmount(0f)
			}
		}

		Box(
			modifier = Modifier
				.fillMaxSize()
				.clickable(
					interactionSource = remember { MutableInteractionSource() },
					indication = null,
					onClick = onClose
				)
				.background(Black.copy(alpha = 0.5f)), contentAlignment = Alignment.Center
		) {
			Card(
				modifier = Modifier
					.fillMaxWidth(0.9f)
					.fillMaxHeight(0.9f)
					.clickable(
						interactionSource = remember { MutableInteractionSource() },
						indication = null,
						onClick = {}), colors = CardDefaults.cardColors(
					containerColor = ColorScheme.surfaceContainer,
					contentColor = AlertDialogDefaults.titleContentColor
				)
			) {
				Column(
					modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(0.dp)
				) {
					Row(
						modifier = Modifier.fillMaxWidth(),
						horizontalArrangement = Arrangement.SpaceBetween,
						verticalAlignment = Alignment.CenterVertically
					) {
						Row(
							horizontalArrangement = Arrangement.spacedBy(4.dp),
							verticalAlignment = Alignment.CenterVertically
						) {
							Icon(
								imageVector = Icons.Filled.Bluetooth, contentDescription = null
							)
							Text("Select Device", style = Typography.titleLarge)
						}
						Row(
							horizontalArrangement = Arrangement.spacedBy(4.dp),
							verticalAlignment = Alignment.CenterVertically
						) {
							Text(
								"Hold to save as favorite",
								style = Typography.labelMedium,
								color = White.copy(alpha = 0.25f),
								modifier = Modifier.padding(end = 8.dp)
							)
							if (bluetoothStatus != BluetoothStatus.Disconnected) {
								TextButton(
									onClick = { showDisconnectDialog = true },
									colors = ButtonDefaults.textButtonColors(
										containerColor = Red.copy(alpha = 0.1f)
									)
								) {
									Text("Disconnect")
								}
							}
							IconButton(onClick = onRefresh, enabled = !isRefreshing) {
								if (isRefreshing) {
									CircularProgressIndicator(
										modifier = Modifier.size(24.dp), strokeWidth = 2.dp
									)
								} else {
									Icon(
										imageVector = Icons.Filled.Refresh, contentDescription = "Refresh"
									)
								}
							}
							IconButton(onClick = onClose) {
								Icon(
									imageVector = Icons.Filled.Close, contentDescription = "Close"
								)
							}
						}
					}

					if (bondedDevices.isEmpty()) {
						Box(
							modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
						) {
							Text(
								"No paired devices\n\nPair a device from your phone's bluetooth menu",
								color = White.copy(alpha = 0.5f),
								textAlign = TextAlign.Center
							)
						}
					} else {
						val (favorites, normal) = bondedDevices.partition { it.address in favoriteDevices }

						LazyVerticalGrid(
							columns = GridCells.Adaptive(150.dp),
							modifier = Modifier.weight(1f),
							horizontalArrangement = Arrangement.spacedBy(4.dp),
							verticalArrangement = Arrangement.spacedBy(4.dp)
						) {
							deviceItems(
								devices = favorites,
								lastDeviceAddress = lastDeviceAddress,
								bluetoothStatus = bluetoothStatus,
								onConnect = onConnect,
								toggleFavoriteDevice = toggleFavoriteDevice
							)

							if (favorites.isNotEmpty() && normal.isNotEmpty()) {
								item(span = { GridItemSpan(maxLineSpan) }) {
									HorizontalDivider(
										modifier = Modifier.padding(vertical = 8.dp), color = White.copy(alpha = 0.1f)
									)
								}
							}

							deviceItems(
								devices = normal,
								lastDeviceAddress = lastDeviceAddress,
								bluetoothStatus = bluetoothStatus,
								onConnect = onConnect,
								toggleFavoriteDevice = toggleFavoriteDevice
							)
						}

						Text(
							"*Greyed out devices are not likely to be a Gigo S4A device (doesn't have \"BT\" in the name)",
							style = Typography.labelMedium,
							color = White.copy(alpha = 0.25f)
						)
					}
				}
			}
		}
	}

	if (showDisconnectDialog) {
		AlertDialog(
			onDismissRequest = { showDisconnectDialog = false },
			containerColor = ColorScheme.surfaceContainer,
			title = {
				Text("Are you sure?")
			},
			text = {
				Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
					Text("You're about to disconnect from this device.")
					BluetoothDeviceItem(
						modifier = Modifier.fillMaxWidth(),
						name = connectedDevice?.getNameSafe() ?: "???",
						address = connectedDevice?.address ?: "???",
						onClick = {},
						isLastConnected = true
					)
				}
			},
			confirmButton = {
				TextButton(
					onClick = {
						showDisconnectDialog = false
						onDisconnect()
					}, colors = ButtonDefaults.textButtonColors(
						containerColor = Red.copy(alpha = 0.1f)
					)
				) {
					Text("Disconnect")
				}
			},
			dismissButton = {
				TextButton(onClick = { showDisconnectDialog = false }) {
					Text("Cancel")
				}
			})
	}
}

private fun LazyGridScope.deviceItems(
	devices: List<BluetoothDeviceInfo>,
	lastDeviceAddress: String?,
	bluetoothStatus: BluetoothStatus?,
	onConnect: (BluetoothDeviceInfo) -> Unit,
	toggleFavoriteDevice: (String) -> Unit
) {
	items(devices, key = { it.address }) { deviceInfo ->
		BluetoothDeviceItem(
			name = deviceInfo.name,
			address = deviceInfo.address,
			onClick = { onConnect(deviceInfo) },
			onHold = { toggleFavoriteDevice(deviceInfo.address) },
			isLastConnected = deviceInfo.address == lastDeviceAddress,
			bluetoothStatus = bluetoothStatus
		)
	}
}

@Composable
private fun BluetoothDeviceItem(
	modifier: Modifier = Modifier,
	name: String,
	address: String,
	onClick: () -> Unit,
	onHold: (() -> Unit)? = null,
	isLastConnected: Boolean = false,
	bluetoothStatus: BluetoothStatus? = null
) {
	val interactionSource = remember { MutableInteractionSource() }
	val scope = rememberCoroutineScope()
	var lastPress by remember { mutableStateOf<PressInteraction.Press?>(null) }

	LaunchedEffect(interactionSource) {
		interactionSource.interactions.collect { interaction ->
			when (interaction) {
				is PressInteraction.Press -> lastPress = interaction
				is PressInteraction.Release, is PressInteraction.Cancel -> lastPress = null
			}
		}
	}

	Surface(
		modifier = modifier
			.combinedClickable(
				interactionSource = interactionSource,
				indication = LocalIndication.current,
				onClick = {
					if (!isLastConnected || bluetoothStatus == BluetoothStatus.Disconnected) onClick()
				},
				onLongClick = {
					onHold?.invoke()
					lastPress?.let { press ->
						scope.launch {
							delay(200)
							interactionSource.emit(PressInteraction.Release(press))
						}
					}
				})
			.background(
				when {
					isLastConnected -> {
						when (bluetoothStatus) {
							BluetoothStatus.Connected -> Green.copy(0.05f)
							BluetoothStatus.Connecting -> Yellow.copy(0.05f)
							else -> ColorScheme.surfaceContainerHigh
						}
					}

					else -> Transparent
				}
			)
			.padding(12.dp), color = Transparent
	) {
		Column(modifier = Modifier.alpha(if (name.contains("BT")) 1f else 0.6f)) {
			Text(
				name, maxLines = 1, overflow = TextOverflow.MiddleEllipsis, style = Typography.labelLarge
			)
			Text(
				address,
				maxLines = 1,
				overflow = TextOverflow.Ellipsis,
				style = Typography.labelSmall,
				color = White.copy(alpha = 0.5f)
			)
		}
	}
}


@Preview(widthDp = 915, heightDp = 412)
@Composable
fun BluetoothDevicesListDialogPreview() {
	Theme {
		BluetoothDevicesListDialog(
			bondedDevices = emptyList(),
			lastDeviceAddress = null,
			favoriteDevices = emptySet(),
			toggleFavoriteDevice = { },
			bluetoothStatus = BluetoothStatus.Disconnected,
			connectedDevice = null,
			onClose = { },
			onConnect = { },
			onRefresh = { },
			onDisconnect = { },
			isRefreshing = false
		)
	}
}
