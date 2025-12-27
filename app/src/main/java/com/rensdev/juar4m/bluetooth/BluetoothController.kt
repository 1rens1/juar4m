package com.rensdev.juar4m.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.UUID

class BluetoothController(context: Context) {
	private val bluetoothManager =
		context.getSystemService(Context.BLUETOOTH_SERVICE) as android.bluetooth.BluetoothManager
	val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter

	private var socket: BluetoothSocket? = null
	private var connectionJob: Job? = null
	private var reconnectJob: Job? = null

	private val _status = MutableStateFlow(BluetoothStatus.Disconnected)
	val status: StateFlow<BluetoothStatus> = _status.asStateFlow()

	private val _connectedDevice = MutableStateFlow<BluetoothDevice?>(null)
	val connectedDevice: StateFlow<BluetoothDevice?> = _connectedDevice.asStateFlow()

	// Track device to reconnect to and whether we should auto-reconnect
	private var targetDevice: BluetoothDevice? = null
	private var shouldAutoReconnect = false

	@SuppressLint("MissingPermission")
	fun connect(device: BluetoothDevice) {
		// Cancel any existing connections/reconnections
		cancelAllJobs()

		// Update target device and enable auto-reconnect
		targetDevice = device
		shouldAutoReconnect = true
		_connectedDevice.value = device

		attemptConnection()
	}

	@SuppressLint("MissingPermission")
	private fun attemptConnection() {
		val device = targetDevice ?: return

		_status.value = BluetoothStatus.Connecting

		connectionJob = CoroutineScope(Dispatchers.IO).launch {
			try {
				try {
					socket?.close()
				} catch (_: IOException) {
				}

				socket = device.createRfcommSocketToServiceRecord(
					UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
				)
				socket?.connect()

				if (isActive && shouldAutoReconnect && device == targetDevice) {
					_status.value = BluetoothStatus.Connected
					// Monitor connection
					monitorConnection()
				}
			} catch (_: IOException) {
				if (isActive && shouldAutoReconnect && device == targetDevice) {
					scheduleReconnect()
				}
			}
		}
	}

	private fun monitorConnection() {
		connectionJob = CoroutineScope(Dispatchers.IO).launch {
			try {
				// Keep reading to detect disconnection
				val buffer = ByteArray(1024)
				while (isActive && shouldAutoReconnect) {
					val bytes = socket?.inputStream?.read(buffer)
					if (bytes == -1) {
						// Connection lost
						throw IOException("Connection lost")
					}
				}
			} catch (_: IOException) {
				if (isActive && shouldAutoReconnect && targetDevice != null) {
					_status.value = BluetoothStatus.Connecting
					scheduleReconnect()
				}
			}
		}
	}

	private fun scheduleReconnect() {
		reconnectJob?.cancel()
		reconnectJob = CoroutineScope(Dispatchers.IO).launch {
			// Minimal delay for stability (50ms is very fast, adjust if needed)
			delay(50)
			if (isActive && shouldAutoReconnect && targetDevice != null) {
				attemptConnection()
			}
		}
	}

	fun disconnect() {
		shouldAutoReconnect = false
		targetDevice = null
		cancelAllJobs()
		closeSocket()
		_status.value = BluetoothStatus.Disconnected
		_connectedDevice.value = null
	}

	private fun cancelAllJobs() {
		connectionJob?.cancel()
		connectionJob = null
		reconnectJob?.cancel()
		reconnectJob = null
	}

	private fun closeSocket() {
		try {
			socket?.close()
		} catch (_: IOException) {
		}
		socket = null
	}

	private val ioScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

	fun sendCommand(bytes: ByteArray) {
		if (_status.value != BluetoothStatus.Connected) return
		ioScope.launch {
			synchronized(this) { // Simple locking to maintain order
				try {
					socket?.outputStream?.let { stream ->
						stream.write(bytes)
						stream.flush()
					}
				} catch (_: IOException) {
					// Monitor will handle disconnection
				}
			}
		}
	}

	@SuppressLint("MissingPermission")
	fun connectToAddress(address: String): Boolean {
		val device = bluetoothAdapter?.getRemoteDevice(address)
		return if (device != null) {
			connect(device)
			true
		} else {
			false
		}
	}
}
