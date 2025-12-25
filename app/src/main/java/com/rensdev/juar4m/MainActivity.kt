package com.rensdev.juar4m

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.addCallback
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothDisabled
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import com.rensdev.juar4m.ui.MainContent
import com.rensdev.juar4m.ui.theme.Blue
import com.rensdev.juar4m.ui.theme.Cyan
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
	private val viewModel: MainViewModel by viewModels()

	private var showPermissionDeniedDialog by mutableStateOf(false)
	private var showBluetoothDeniedDialog by mutableStateOf(false)
	private var hasTriedAutoConnect = false

	private val bluetoothStateReceiver = object : BroadcastReceiver() {
		override fun onReceive(context: Context?, intent: Intent?) {
			when (intent?.action) {
				BluetoothAdapter.ACTION_STATE_CHANGED -> {
					val state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)
					when (state) {
						BluetoothAdapter.STATE_OFF -> {
							if (hasBluetoothPermissions()) {
								showBluetoothDeniedDialog = true
								hasTriedAutoConnect = false
								viewModel.disconnectBluetooth()
							}
						}

						BluetoothAdapter.STATE_ON -> {
							showBluetoothDeniedDialog = false
							if (hasBluetoothPermissions()) {
								tryAutoConnect()
							}
						}
					}
				}
			}
		}
	}

	private val requestPermissionLauncher = registerForActivityResult(
		ActivityResultContracts.RequestMultiplePermissions()
	) { permissions ->
		if (permissions.values.all { it }) {
			checkBluetoothEnabled()
		} else {
			showPermissionDeniedDialog = true
		}
	}

	private val enableBluetoothLauncher = registerForActivityResult(
		ActivityResultContracts.StartActivityForResult()
	) {
		showBluetoothDeniedDialog = !isBluetoothEnabled()
		if (isBluetoothEnabled()) {
			tryAutoConnect()
		}
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setupWindow()
		bluetoothSetup()

		setContent {
			MainContent(viewModel) {
				if (showPermissionDeniedDialog) PermissionDeniedDialog(onOpenSettings = ::openAppSettings)
				if (showBluetoothDeniedDialog) BluetoothDisabledDialog(onEnableBluetooth = ::requestEnableBluetooth)
			}
		}
	}

	override fun onStart() {
		super.onStart()
		registerReceiver(
			bluetoothStateReceiver, IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
		)
	}

	override fun onStop() {
		super.onStop()
		unregisterReceiver(bluetoothStateReceiver)
	}

	override fun onResume() {
		super.onResume()

		if (!hasBluetoothPermissions()) {
			showPermissionDeniedDialog = true
		} else {
			showPermissionDeniedDialog = false
			if (!isBluetoothEnabled()) {
				showBluetoothDeniedDialog = true
				hasTriedAutoConnect = false
			} else {
				showBluetoothDeniedDialog = false
				tryAutoConnect()
			}
		}
	}

	private fun setupWindow() {
		enableEdgeToEdge()

		// Keep screen on, max brightness
		window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
		window.attributes = window.attributes.apply {
			screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL
		}

		// Full screen, hide status bar and navigation bar
		WindowCompat.setDecorFitsSystemWindows(window, false)
		val controller = WindowInsetsControllerCompat(window, window.decorView)
		controller.hide(
			WindowInsetsCompat.Type.statusBars() or WindowInsetsCompat.Type.navigationBars()
		)
		controller.systemBarsBehavior =
			WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

		// Disable back press
		onBackPressedDispatcher.addCallback(this) {
			viewModel.showSnackbar("Back press disabled, use home")
		}
	}

	private fun bluetoothSetup() {
		if (!hasBluetoothPermissions()) {
			requestBluetoothPermissions()
		} else {
			checkBluetoothEnabled()
		}
	}

	private fun hasBluetoothPermissions(): Boolean {
		return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
			checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED && checkSelfPermission(
				Manifest.permission.BLUETOOTH_SCAN
			) == PackageManager.PERMISSION_GRANTED
		} else {
			true
		}
	}

	private fun requestBluetoothPermissions() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
			requestPermissionLauncher.launch(
				arrayOf(
					Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN
				)
			)
		}
	}

	private fun isBluetoothEnabled(): Boolean {
		return viewModel.bluetoothAdapter?.isEnabled == true
	}

	private fun checkBluetoothEnabled() {
		if (!isBluetoothEnabled()) {
			showBluetoothDeniedDialog = true
		} else {
			tryAutoConnect()
		}
	}

	private fun tryAutoConnect() {
		if (hasTriedAutoConnect) return
		hasTriedAutoConnect = true

		lifecycleScope.launch {
			viewModel.connectToLastDevice()
		}
	}

	private fun requestEnableBluetooth() {
		enableBluetoothLauncher.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
	}

	private fun openAppSettings() {
		startActivity(
			Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
				data = Uri.fromParts("package", packageName, null)
			})
	}
}

@Composable
private fun PermissionDeniedDialog(onOpenSettings: () -> Unit) {
	AlertDialog(
		onDismissRequest = { },
		icon = { Icon(Icons.Default.Bluetooth, contentDescription = null) },
		title = { Text("Allow Bluetooth") },
		text = { Text("Please allow Bluetooth in the settings.\n\nAllow in Permissions > Nearby devices.") },
		confirmButton = {
			TextButton(onClick = onOpenSettings, colors = ButtonDefaults.textButtonColors(containerColor = Cyan.copy(alpha=0.1f))) {
				Text("Open Settings")
			}
		})
}

@Composable
private fun BluetoothDisabledDialog(onEnableBluetooth: () -> Unit) {
	AlertDialog(
		onDismissRequest = { },
		icon = { Icon(Icons.Default.BluetoothDisabled, contentDescription = null) },
		title = { Text("Bluetooth Disabled") },
		confirmButton = {
			TextButton(onClick = onEnableBluetooth, colors = ButtonDefaults.textButtonColors(containerColor = Cyan.copy(alpha=0.1f))) {
				Text("Turn on Bluetooth")
			}
		})
}
