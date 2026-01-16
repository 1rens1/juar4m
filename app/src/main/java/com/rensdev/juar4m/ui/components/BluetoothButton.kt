package com.rensdev.juar4m.ui.components

import android.annotation.SuppressLint
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothConnected
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rensdev.juar4m.MainViewModel
import com.rensdev.juar4m.bluetooth.BluetoothStatus
import com.rensdev.juar4m.bluetooth.getNameSafe
import com.rensdev.juar4m.ui.theme.Green
import com.rensdev.juar4m.ui.theme.Red
import com.rensdev.juar4m.ui.theme.Theme
import com.rensdev.juar4m.ui.theme.Typography
import com.rensdev.juar4m.ui.theme.White
import com.rensdev.juar4m.ui.theme.Yellow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class BluetoothDeviceInfo(
	val name: String, val address: String
)

@SuppressLint("MissingPermission")
@Composable
fun BluetoothButton(
	viewModel: MainViewModel?, bluetoothStatus: BluetoothStatus, modifier: Modifier = Modifier
) {
	val lastDeviceAddress by (viewModel?.lastDeviceAddress
		?: flowOf(null)).collectAsState(initial = null)
	val favoriteDevices by (viewModel?.favoriteDevices
		?: flowOf(emptySet())).collectAsState(initial = emptySet())

	val connectedDevice by (viewModel?.connectedDevice ?: flowOf(null)).collectAsState(initial = null)

	var showDialog by remember { mutableStateOf(false) }
	var bondedDevices by remember { mutableStateOf<List<BluetoothDeviceInfo>>(emptyList()) }
	var isRefreshing by remember { mutableStateOf(false) }
	val scope = rememberCoroutineScope()

	@SuppressLint("MissingPermission")
	fun loadDevices() {
		scope.launch {
			isRefreshing = true
			val devices = withContext(Dispatchers.IO) {
				if (viewModel?.bluetoothAdapter?.isEnabled != true) return@withContext emptyList()

				viewModel.bluetoothAdapter.bondedDevices?.map { device ->
					BluetoothDeviceInfo(
						name = device.name, address = device.address
					)
				}?.sortedBy { it.name } ?: emptyList()
			}
			bondedDevices = devices
			isRefreshing = false
		}
	}

	BluetoothButtonDisplay(
		modifier = modifier, status = bluetoothStatus, onClick = {
			showDialog = true
			if (bondedDevices.isEmpty() && !isRefreshing) {
				loadDevices()
			}
		}, connectedDeviceName = connectedDevice?.getNameSafe() ?: "???"
	)

	if (showDialog) {
		BluetoothDevicesListDialog(
			lastDeviceAddress = lastDeviceAddress,
			favoriteDevices = favoriteDevices,
			toggleFavoriteDevice = { viewModel?.toggleFavoriteDevice(it) },
			bluetoothStatus = bluetoothStatus,
			connectedDevice = connectedDevice,
			bondedDevices = bondedDevices,
			onClose = { showDialog = false },
			onConnect = { deviceInfo ->
				val device = viewModel?.bluetoothAdapter?.bondedDevices?.find {
					it.address == deviceInfo.address
				}
				device?.let { viewModel.connectToDevice(it) }
				showDialog = false
			},
			onRefresh = { loadDevices() },
			onDisconnect = {
				viewModel?.disconnectBluetooth()
				// showDialog = false
			},
			isRefreshing = isRefreshing
		)
	}
}

@Composable
private fun BluetoothButtonDisplay(
	status: BluetoothStatus,
	onClick: () -> Unit,
	modifier: Modifier = Modifier,
	connectedDeviceName: String = ""
) {
	Surface(
		color = when (status) {
			BluetoothStatus.Connected -> Green.copy(alpha = 0.6f)
			BluetoothStatus.Connecting -> Yellow.copy(alpha = 0.6f)
			BluetoothStatus.Disconnected -> Red.copy(alpha = 0.9f)
		},
		contentColor = White,
		shape = ButtonDefaults.shape,
		modifier = modifier
			.height(28.dp)
			.pointerInput(onClick) {
				awaitEachGesture {
					awaitFirstDown()
					onClick()
					waitForUpOrCancellation()
				}
			}
	) {
		Row(
			modifier = Modifier.padding(horizontal = 12.dp),
			verticalAlignment = Alignment.CenterVertically,
			horizontalArrangement = Arrangement.spacedBy(4.dp)
		) {
			val iconSize = 16.dp
			when (status) {
				BluetoothStatus.Connected -> {
					Icon(
						imageVector = Icons.Filled.BluetoothConnected,
						contentDescription = null,
						modifier = Modifier.size(iconSize)
					)
				}

				BluetoothStatus.Connecting -> {
					CircularProgressIndicator(
						modifier = Modifier.size(iconSize),
						strokeWidth = 2.dp,
						color = White
					)
				}

				else -> {
					Icon(
						imageVector = Icons.Filled.Bluetooth,
						contentDescription = null,
						modifier = Modifier.size(iconSize)
					)
				}
			}
			Text(
				text = when (status) {
					BluetoothStatus.Connected -> "Connected to $connectedDeviceName"
					BluetoothStatus.Connecting -> "Connecting to $connectedDeviceName ..."
					BluetoothStatus.Disconnected -> "Disconnected"
				},
				style = Typography.labelLarge
			)
		}
	}
}

@Preview
@Composable
fun BluetoothButtonPreview() {
	Theme {
		Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
			BluetoothButtonDisplay(BluetoothStatus.Disconnected, {})
			BluetoothButtonDisplay(BluetoothStatus.Connecting, {}, connectedDeviceName = "0000-00-0000")
			BluetoothButtonDisplay(BluetoothStatus.Connected, {}, connectedDeviceName = "0000-00-0000")
		}
	}
}
