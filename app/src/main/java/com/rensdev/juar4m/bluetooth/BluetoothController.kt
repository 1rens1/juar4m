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
import kotlinx.coroutines.channels.Channel
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

	private var targetDevice: BluetoothDevice? = null
	private var shouldAutoReconnect = false

	// Scope for IO operations (Sending/Receiving)
	private val ioScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

	// CONFLATED: Only keeps the LATEST packet. Drops old ones if busy. Prevents lag buildup. Guarantees 100% thread safety (packets never mix).
	private val sendChannel = Channel<ByteArray>(Channel.CONFLATED)

	init {
		ioScope.launch {
			for (packet in sendChannel) {
				try {
					socket?.outputStream?.apply {
						write(packet)
						flush()
					}
				} catch (_: IOException) {
					// The monitorConnection loop will handle the disconnection event
				}
			}
		}
	}

	@SuppressLint("MissingPermission")
	fun connect(device: BluetoothDevice) {
		cancelAllJobs()
		targetDevice = device
		shouldAutoReconnect = true
		_connectedDevice.value = device
		attemptConnection()
	}

	@SuppressLint("MissingPermission")
	private fun attemptConnection() {
		val device = targetDevice ?: return
		_status.value = BluetoothStatus.Connecting

		connectionJob = ioScope.launch {
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
					monitorConnection()
				}
			} catch (_: IOException) {
				handleConnectionFailure(device)
			} catch (_: SecurityException) {
				handleConnectionFailure(device)
			}
		}
	}

	private fun handleConnectionFailure(device: BluetoothDevice) {
		if (shouldAutoReconnect && device == targetDevice) {
			scheduleReconnect()
		}
	}

	private fun monitorConnection() {
		connectionJob = ioScope.launch {
			try {
				val buffer = ByteArray(1024)
				while (isActive && shouldAutoReconnect) {
					// Blocking read is fine here in IO thread
					val bytes = socket?.inputStream?.read(buffer) ?: -1
					if (bytes == -1) throw IOException("Connection lost")
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
		reconnectJob = ioScope.launch {
			delay(250)
			if (isActive && shouldAutoReconnect && targetDevice != null) {
				attemptConnection()
			}
		}
	}

	fun disconnect() {
		shouldAutoReconnect = false
		targetDevice = null
		cancelAllJobs()
		ioScope.launch { closeSocket() }
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

	fun sendCommand(bytes: ByteArray) {
		sendRaw(byteArrayOf(0xFF.toByte()) + bytes)
	}

	fun sendRaw(bytes: ByteArray) {
		if (_status.value != BluetoothStatus.Connected) return
		sendChannel.trySend(bytes)
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
