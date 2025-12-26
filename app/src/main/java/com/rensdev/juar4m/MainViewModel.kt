package com.rensdev.juar4m

import android.app.Application
import android.bluetooth.BluetoothDevice
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rensdev.juar4m.bluetooth.BluetoothController
import com.rensdev.juar4m.bluetooth.BluetoothStatus
import com.rensdev.juar4m.ui.components.controller.ServoState
import com.rensdev.juar4m.ui.components.controller.ToggleServoDefaults
import com.rensdev.juar4m.utils.VibrationManager
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
	///////////////////////// VIBRATOR //////////////////////////
	val vibrationManager = VibrationManager(application.applicationContext)

	init {
		viewModelScope.launch {
			userPreferences.vibrationsEnabled.collect { isEnabled ->
				vibrationManager.enabled = isEnabled
			}
		}
	}


	///////////////////////// SNACKBAR //////////////////////////
	data class UserMessage(
		val message: String, val withDismissAction: Boolean
	)

	private val _userMessages = Channel<UserMessage>()
	val userMessages = _userMessages.receiveAsFlow()

	fun showSnackbar(message: String, withDismissAction: Boolean = false) {
		viewModelScope.launch {
			_userMessages.send(
				UserMessage(
					message = message, withDismissAction = withDismissAction
				)
			)
		}
	}

	//////////////////////// PREFERENCES ////////////////////////
	private val userPreferences = UserPreferences(application)

	val lastDeviceAddress = userPreferences.lastDeviceAddress

	fun saveLastDeviceAddress(name: String?) {
		viewModelScope.launch {
			userPreferences.saveLastDeviceAddress(name)
		}
	}

	val vibrationsEnabled = userPreferences.vibrationsEnabled

	fun saveVibrationsEnabled(enabled: Boolean) {
		viewModelScope.launch {
			userPreferences.saveVibrationsEnabled(enabled)
		}
	}

	val disconnectLock = userPreferences.disconnectLock

	fun saveDisconnectLock(enabled: Boolean) {
		viewModelScope.launch {
			userPreferences.saveDisconnectLock(enabled)
		}
	}

	val stopwatchModeEnabled = userPreferences.stopwatchModeEnabled

	fun saveStopwatchModeEnabled(enabled: Boolean) {
		viewModelScope.launch {
			userPreferences.saveStopwatchModeEnabled(enabled)
		}
	}

	// Servo A Preferences
	val servoARange: StateFlow<ClosedFloatingPointRange<Float>> = userPreferences.servoARange.stateIn(
			viewModelScope,
			SharingStarted.WhileSubscribed(5000),
			ToggleServoDefaults.servoA.range // Initial value
		)

	val servoAState: StateFlow<ServoState> = userPreferences.servoAState.stateIn(
			viewModelScope,
			SharingStarted.WhileSubscribed(5000),
			ToggleServoDefaults.servoA.state // Initial value
		)

	fun saveServoARange(range: ClosedFloatingPointRange<Float>) {
		viewModelScope.launch {
			userPreferences.saveServoARange(range)
		}
	}

	fun saveServoAState(state: ServoState) {
		viewModelScope.launch {
			userPreferences.saveServoAState(state)
		}
	}

	// Servo B Preferences
	val servoBRange: StateFlow<ClosedFloatingPointRange<Float>> = userPreferences.servoBRange.stateIn(
			viewModelScope,
			SharingStarted.WhileSubscribed(5000),
			ToggleServoDefaults.servoB.range // Initial value
		)

	val servoBState: StateFlow<ServoState> = userPreferences.servoBState.stateIn(
			viewModelScope,
			SharingStarted.WhileSubscribed(5000),
			ToggleServoDefaults.servoB.state // Initial value
		)

	fun saveServoBRange(range: ClosedFloatingPointRange<Float>) {
		viewModelScope.launch {
			userPreferences.saveServoBRange(range)
		}
	}

	fun saveServoBState(state: ServoState) {
		viewModelScope.launch {
			userPreferences.saveServoBState(state)
		}
	}

	///////////////////////// BLUETOOTH /////////////////////////
	private val bluetoothController = BluetoothController(application.applicationContext)

	val bluetoothAdapter = bluetoothController.bluetoothAdapter
	val bluetoothStatus: StateFlow<BluetoothStatus> = bluetoothController.status
	val connectedDevice: StateFlow<BluetoothDevice?> = bluetoothController.connectedDevice

	fun connectToDevice(device: BluetoothDevice) {
		saveLastDeviceAddress(device.address)
		bluetoothController.connect(device)
	}

	suspend fun connectToLastDevice() {
		val address = lastDeviceAddress.first()
		if (address != null) {
			bluetoothController.connectToAddress(address)
		}
	}

	fun disconnectBluetooth() {
		bluetoothController.disconnect()
	}

	fun sendBluetoothCommand(bytes: ByteArray) {
		bluetoothController.sendCommand(bytes)
	}
}
