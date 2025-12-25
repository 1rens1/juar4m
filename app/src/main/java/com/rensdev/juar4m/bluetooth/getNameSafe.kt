package com.rensdev.juar4m.bluetooth

import android.bluetooth.BluetoothDevice

fun BluetoothDevice.getNameSafe(): String {
	return try {
		name
	} catch (_: SecurityException) {
		null
	} ?: "Unknown device"
}
