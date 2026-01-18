package com.rensdev.juar4m

import android.app.Application
import android.bluetooth.BluetoothDevice
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rensdev.juar4m.bluetooth.BluetoothController
import com.rensdev.juar4m.bluetooth.BluetoothStatus
import com.rensdev.juar4m.command.CommandController
import com.rensdev.juar4m.command.Commands
import com.rensdev.juar4m.preferences.UserPreferences
import com.rensdev.juar4m.preferences.UserPreferencesDefaults
import com.rensdev.juar4m.stopwatch.StopwatchManager
import com.rensdev.juar4m.ui.components.controller.ServoState
import com.rensdev.juar4m.utils.VibrationManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@Suppress("KotlinConstantConditions")
class MainViewModel(application: Application) : AndroidViewModel(application) {
	///////////////////////// SNACKBAR //////////////////////////
	data class UserMessage(val message: String, val withDismissAction: Boolean)

	private val _userMessages = Channel<UserMessage>()
	val userMessages = _userMessages.receiveAsFlow()

	fun showSnackbar(message: String, withDismissAction: Boolean = false) {
		viewModelScope.launch {
			_userMessages.send(UserMessage(message, withDismissAction))
		}
	}

	//////////////////////// PREFERENCES ////////////////////////
	private val userPreferences = UserPreferences(application)

	val lastDeviceAddress = userPreferences.lastDeviceAddress
	fun setLastDeviceAddress(address: String?) =
		viewModelScope.launch { userPreferences.setLastDeviceAddress(address) }

	val favoriteDevices = userPreferences.favoriteDevices
	fun toggleFavoriteDevice(address: String) =
		viewModelScope.launch { userPreferences.toggleFavoriteDevice(address) }

	private val _vibrationsEnabled = MutableStateFlow(UserPreferencesDefaults.APP_VIBRATIONS_ENABLED)
	val vibrationsEnabled = _vibrationsEnabled.asStateFlow()
	fun setVibrationsEnabled(enabled: Boolean) {
		_vibrationsEnabled.value = enabled
	}

	private val _disconnectLockEnabled =
		MutableStateFlow(UserPreferencesDefaults.APP_DISCONNECT_LOCK_ENABLED)
	val disconnectLockEnabled = _disconnectLockEnabled.asStateFlow()
	fun setDisconnectLockEnabled(enabled: Boolean) {
		_disconnectLockEnabled.value = enabled
	}

	private val _stopwatchModeEnabled =
		MutableStateFlow(UserPreferencesDefaults.APP_STOPWATCH_MODE_ENABLED)
	val stopwatchModeEnabled = _stopwatchModeEnabled.asStateFlow()
	fun setStopwatchModeEnabled(enabled: Boolean) {
		_stopwatchModeEnabled.value = enabled
		if (!enabled) {
			stopwatchManager.stop()
			stopwatchManager.reset()
		}
	}

	val marqueeAnimationEnabled = userPreferences.marqueeAnimationEnabled
	fun setMarqueeAnimationEnabled(enabled: Boolean) = viewModelScope.launch {
		userPreferences.setMarqueeAnimationEnabled(enabled)
	}

	val cornerDPadButtonsEnabled = userPreferences.cornerDPadButtonsEnabled
	fun setCornerDPadButtonsEnabled(enabled: Boolean) = viewModelScope.launch {
		userPreferences.setCornerDPadButtonsEnabled(enabled)
	}

	// Servo A
	val servoARange: StateFlow<ClosedFloatingPointRange<Float>> = userPreferences.servoARange.stateIn(
		viewModelScope, SharingStarted.WhileSubscribed(5000), UserPreferencesDefaults.SERVO_A_RANGE
	)
	val servoAState: StateFlow<ServoState> = userPreferences.servoAState.stateIn(
		viewModelScope, SharingStarted.WhileSubscribed(5000), UserPreferencesDefaults.SERVO_A_STATE
	)

	fun setServoARange(range: ClosedFloatingPointRange<Float>) =
		viewModelScope.launch { userPreferences.setServoARange(range) }

	fun setServoAState(state: ServoState) =
		viewModelScope.launch { userPreferences.setServoAState(state) }

	// Servo B
	val servoBRange: StateFlow<ClosedFloatingPointRange<Float>> = userPreferences.servoBRange.stateIn(
		viewModelScope, SharingStarted.WhileSubscribed(5000), UserPreferencesDefaults.SERVO_B_RANGE
	)
	val servoBState: StateFlow<ServoState> = userPreferences.servoBState.stateIn(
		viewModelScope, SharingStarted.WhileSubscribed(5000), UserPreferencesDefaults.SERVO_B_STATE
	)

	fun setServoBRange(range: ClosedFloatingPointRange<Float>) =
		viewModelScope.launch { userPreferences.setServoBRange(range) }

	fun setServoBState(state: ServoState) =
		viewModelScope.launch { userPreferences.setServoBState(state) }

	// Servo C
	val servoCInverted: StateFlow<Boolean> = userPreferences.servoCInverted.stateIn(
		viewModelScope, SharingStarted.WhileSubscribed(5000), UserPreferencesDefaults.SERVO_C_INVERTED
	)

	fun setServoCInverted(inverted: Boolean) =
		viewModelScope.launch { userPreferences.setServoCInverted(inverted) }

	///////////////////////// VIBRATOR //////////////////////////
	val vibrator = VibrationManager(application.applicationContext)

	init {
		viewModelScope.launch { vibrationsEnabled.collect { vibrator.enabled = it } }
	}

	///////////////////////// BLUETOOTH /////////////////////////
	private val bluetoothController = BluetoothController(application.applicationContext)

	val bluetoothAdapter = bluetoothController.bluetoothAdapter
	val bluetoothStatus: StateFlow<BluetoothStatus> = bluetoothController.status
	val connectedDevice: StateFlow<BluetoothDevice?> = bluetoothController.connectedDevice

	fun connectToDevice(device: BluetoothDevice) {
		setLastDeviceAddress(device.address)
		bluetoothController.connect(device)
	}

	suspend fun connectToLastDevice() {
		lastDeviceAddress.first()?.let { bluetoothController.connectToAddress(it) }
	}

	fun disconnectBluetooth() = bluetoothController.disconnect()
	// fun sendBluetoothCommand(bytes: ByteArray) = bluetoothController.sendCommand(bytes)


	///////////////////////// STOPWATCH /////////////////////////
	val stopwatchManager = StopwatchManager(viewModelScope)

	val stopwatchElapsed = stopwatchManager.elapsedMillis
	val isStopwatchRunning = stopwatchManager.isRunning

	fun finishStopwatch() {
		stopwatchManager.stop()
	}

	fun resetStopwatch() {
		stopwatchManager.reset()
	}

	val stopwatchPB = userPreferences.stopwatchPB
	fun setStopwatchPB(pb: Long?) = viewModelScope.launch { userPreferences.setStopwatchPB(pb) }

	//////////////////// COMMAND CONTROLLER /////////////////////
	private val controllerScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
	val commandController = CommandController(controllerScope, { bytes ->
		bluetoothController.sendCommand(bytes)
	}, { command ->
		if (command != Commands.STOP_MOTORS && stopwatchModeEnabled.value) {
			stopwatchManager.start()
		}
	})

	init {
		viewModelScope.launch {
			bluetoothStatus.collect { status ->
				if (status == BluetoothStatus.Connected) {
					commandController.start()
				} else {
					commandController.stopTicker()
				}
			}
		}
	}

	override fun onCleared() {
		super.onCleared()
		commandController.stopTicker()
	}
}
