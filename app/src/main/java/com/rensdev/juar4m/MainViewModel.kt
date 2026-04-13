package com.rensdev.juar4m

import android.app.Application
import android.bluetooth.BluetoothDevice
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rensdev.juar4m.bluetooth.BluetoothController
import com.rensdev.juar4m.bluetooth.BluetoothStatus
import com.rensdev.juar4m.command.CommandController
import com.rensdev.juar4m.command.Commands
import com.rensdev.juar4m.preferences.RangeState
import com.rensdev.juar4m.preferences.ServoMode
import com.rensdev.juar4m.preferences.UserPreferences
import com.rensdev.juar4m.preferences.UserPreferencesDefaults
import com.rensdev.juar4m.stopwatch.StopwatchManager
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

	// Speed
	val speedRange: StateFlow<ClosedFloatingPointRange<Float>> = userPreferences.speedRange.stateIn(
		viewModelScope, SharingStarted.WhileSubscribed(5000), UserPreferencesDefaults.SPEED_RANGE
	)
	val speedState: StateFlow<RangeState> = userPreferences.speedState.stateIn(
		viewModelScope, SharingStarted.WhileSubscribed(5000), UserPreferencesDefaults.SPEED_STATE
	)

	fun setSpeedRange(range: ClosedFloatingPointRange<Float>) =
		viewModelScope.launch { userPreferences.setSpeedRange(range) }

	fun setSpeedState(state: RangeState) =
		viewModelScope.launch { userPreferences.setSpeedState(state) }

	// Servo A
	val servoAMode = userPreferences.servoAMode.stateIn(
		viewModelScope, SharingStarted.WhileSubscribed(5000), UserPreferencesDefaults.SERVO_A_MODE
	)
	val servoAInverted: StateFlow<Boolean> = userPreferences.servoAInverted.stateIn(
		viewModelScope, SharingStarted.WhileSubscribed(5000), UserPreferencesDefaults.SERVO_A_INVERTED
	)
	val servoARange = userPreferences.servoARange.stateIn(
		viewModelScope, SharingStarted.WhileSubscribed(5000), UserPreferencesDefaults.SERVO_A_RANGE
	)
	val servoAState = userPreferences.servoAState.stateIn(
		viewModelScope, SharingStarted.WhileSubscribed(5000), UserPreferencesDefaults.SERVO_A_STATE
	)

	fun setServoAMode(mode: ServoMode) =
		viewModelScope.launch { userPreferences.setServoAMode(mode) }
	fun setServoAInverted(inverted: Boolean) =
		viewModelScope.launch { userPreferences.setServoAInverted(inverted) }
	fun setServoARange(range: ClosedFloatingPointRange<Float>) =
		viewModelScope.launch { userPreferences.setServoARange(range) }

	fun setServoAState(state: RangeState) =
		viewModelScope.launch { userPreferences.setServoAState(state) }

	// Servo B
	val servoBMode = userPreferences.servoBMode.stateIn(
		viewModelScope, SharingStarted.WhileSubscribed(5000), UserPreferencesDefaults.SERVO_B_MODE
	)
	val servoBInverted: StateFlow<Boolean> = userPreferences.servoBInverted.stateIn(
		viewModelScope, SharingStarted.WhileSubscribed(5000), UserPreferencesDefaults.SERVO_B_INVERTED
	)
	val servoBRange= userPreferences.servoBRange.stateIn(
		viewModelScope, SharingStarted.WhileSubscribed(5000), UserPreferencesDefaults.SERVO_B_RANGE
	)
	val servoBState = userPreferences.servoBState.stateIn(
		viewModelScope, SharingStarted.WhileSubscribed(5000), UserPreferencesDefaults.SERVO_B_STATE
	)

	fun setServoBMode(mode: ServoMode) =
		viewModelScope.launch { userPreferences.setServoBMode(mode) }
	fun setServoBInverted(inverted: Boolean) =
		viewModelScope.launch { userPreferences.setServoBInverted(inverted) }
	fun setServoBRange(range: ClosedFloatingPointRange<Float>) =
		viewModelScope.launch { userPreferences.setServoBRange(range) }

	fun setServoBState(state: RangeState) =
		viewModelScope.launch { userPreferences.setServoBState(state) }

	// Servo C
	val servoCMode = userPreferences.servoCMode.stateIn(
		viewModelScope, SharingStarted.WhileSubscribed(5000), UserPreferencesDefaults.SERVO_C_MODE
	)
	val servoCInverted: StateFlow<Boolean> = userPreferences.servoCInverted.stateIn(
		viewModelScope, SharingStarted.WhileSubscribed(5000), UserPreferencesDefaults.SERVO_C_INVERTED
	)
	val servoCRange = userPreferences.servoCRange.stateIn(
		viewModelScope, SharingStarted.WhileSubscribed(5000), UserPreferencesDefaults.SERVO_C_RANGE
	)
	val servoCState = userPreferences.servoCState.stateIn(
		viewModelScope, SharingStarted.WhileSubscribed(5000), UserPreferencesDefaults.SERVO_C_STATE
	)

	fun setServoCMode(mode: ServoMode) =
		viewModelScope.launch { userPreferences.setServoCMode(mode) }
	fun setServoCInverted(inverted: Boolean) =
		viewModelScope.launch { userPreferences.setServoCInverted(inverted) }
	fun setServoCRange(range: ClosedFloatingPointRange<Float>) =
		viewModelScope.launch { userPreferences.setServoCRange(range) }
	fun setServoCState(state: RangeState) =
		viewModelScope.launch { userPreferences.setServoCState(state) }

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
					val speed = when (speedState.value) {
						RangeState.HIGH -> speedRange.value.endInclusive.toInt()
						RangeState.LOW -> speedRange.value.start.toInt()
					}
					val servoAAngle = when (servoAState.value) {
						RangeState.HIGH -> servoARange.value.endInclusive.toInt()
						RangeState.LOW -> servoARange.value.start.toInt()
					}
					val servoBAngle = when (servoBState.value) {
						RangeState.HIGH -> servoBRange.value.endInclusive.toInt()
						RangeState.LOW -> servoBRange.value.start.toInt()
					}
					val servoCAngle = when (servoCState.value) {
						RangeState.HIGH -> servoCRange.value.endInclusive.toInt()
						RangeState.LOW -> servoCRange.value.start.toInt()
					}
					commandController.syncState(
						speed,
						servoAMode.value, servoAAngle,
						servoBMode.value, servoBAngle,
						servoCMode.value, servoCAngle
					)
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
