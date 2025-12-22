package com.kaasht.croprecommendation.data.sensor

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

data class SoilSensorData(
    val nitrogen: Double,
    val phosphorus: Double,
    val potassium: Double,
    val ph: Double,
    val moisture: Double? = null,
    val temperature: Double? = null,
    val timestamp: Long = System.currentTimeMillis()
)

sealed class SensorState {
    object Disconnected : SensorState()
    object Connecting : SensorState()
    object Connected : SensorState()
    data class DataReceived(val data: SoilSensorData) : SensorState()
    data class Error(val message: String) : SensorState()
}

@Singleton
class BluetoothSensorManager @Inject constructor(
    private val context: Context
) {
    
    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private var bluetoothSocket: BluetoothSocket? = null
    private var inputStream: InputStream? = null
    private var outputStream: OutputStream? = null
    
    private val _sensorState = MutableStateFlow<SensorState>(SensorState.Disconnected)
    val sensorState: StateFlow<SensorState> = _sensorState
    
    companion object {
        private val SPP_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
        private const val BUFFER_SIZE = 1024
    }
    
    /**
     * Check if Bluetooth is supported and enabled
     */
    fun isBluetoothAvailable(): Boolean {
        return bluetoothAdapter != null && bluetoothAdapter.isEnabled
    }
    
    /**
     * Check Bluetooth permissions
     */
    fun hasBluetoothPermissions(): Boolean {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH
            ) == PackageManager.PERMISSION_GRANTED
        }
    }
    
    /**
     * Get list of paired Bluetooth devices
     */
    @SuppressLint("MissingPermission")
    fun getPairedDevices(): List<BluetoothDevice> {
        if (!hasBluetoothPermissions()) {
            Timber.w("Bluetooth permissions not granted")
            return emptyList()
        }
        
        return bluetoothAdapter?.bondedDevices?.toList() ?: emptyList()
    }
    
    /**
     * Connect to Bluetooth device
     */
    @SuppressLint("MissingPermission")
    suspend fun connectToDevice(device: BluetoothDevice) = withContext(Dispatchers.IO) {
        try {
            _sensorState.value = SensorState.Connecting
            Timber.d("Connecting to device: ${device.name}")
            
            // Close existing connection if any
            disconnect()
            
            // Create socket
            bluetoothSocket = device.createRfcommSocketToServiceRecord(SPP_UUID)
            
            // Cancel discovery to improve connection speed
            bluetoothAdapter?.cancelDiscovery()
            
            // Connect
            bluetoothSocket?.connect()
            
            // Get streams
            inputStream = bluetoothSocket?.inputStream
            outputStream = bluetoothSocket?.outputStream
            
            _sensorState.value = SensorState.Connected
            Timber.d("Connected to device: ${device.name}")
            
            // Start reading data
            startReadingData()
            
            Result.success(Unit)
        } catch (e: IOException) {
            Timber.e(e, "Connection failed")
            _sensorState.value = SensorState.Error("Connection failed: ${e.message}")
            disconnect()
            Result.failure(e)
        }
    }
    
    /**
     * Read data from sensor
     */
    private suspend fun startReadingData() = withContext(Dispatchers.IO) {
        val buffer = ByteArray(BUFFER_SIZE)
        
        while (bluetoothSocket?.isConnected == true) {
            try {
                val bytes = inputStream?.read(buffer) ?: 0
                if (bytes > 0) {
                    val data = String(buffer, 0, bytes)
                    parseSensorData(data)
                }
            } catch (e: IOException) {
                Timber.e(e, "Error reading data")
                _sensorState.value = SensorState.Error("Data read failed: ${e.message}")
                break
            }
        }
    }
    
    /**
     * Parse sensor data from raw string
     * Expected format: "N:90.5,P:42.3,K:43.1,pH:6.5,M:45.2,T:25.3"
     */
    private fun parseSensorData(rawData: String) {
        try {
            Timber.d("Raw sensor data: $rawData")
            
            val parts = rawData.trim().split(",")
            val dataMap = mutableMapOf<String, Double>()
            
            parts.forEach { part ->
                val keyValue = part.split(":")
                if (keyValue.size == 2) {
                    val key = keyValue[0].trim()
                    val value = keyValue[1].trim().toDoubleOrNull()
                    if (value != null) {
                        dataMap[key] = value
                    }
                }
            }
            
            // Create sensor data object
            val sensorData = SoilSensorData(
                nitrogen = dataMap["N"] ?: 0.0,
                phosphorus = dataMap["P"] ?: 0.0,
                potassium = dataMap["K"] ?: 0.0,
                ph = dataMap["pH"] ?: dataMap["PH"] ?: 7.0,
                moisture = dataMap["M"],
                temperature = dataMap["T"]
            )
            
            _sensorState.value = SensorState.DataReceived(sensorData)
            Timber.d("Parsed sensor data: $sensorData")
            
        } catch (e: Exception) {
            Timber.e(e, "Failed to parse sensor data")
            _sensorState.value = SensorState.Error("Data parsing failed: ${e.message}")
        }
    }
    
    /**
     * Send command to sensor
     */
    suspend fun sendCommand(command: String) = withContext(Dispatchers.IO) {
        try {
            outputStream?.write(command.toByteArray())
            outputStream?.flush()
            Timber.d("Command sent: $command")
            Result.success(Unit)
        } catch (e: IOException) {
            Timber.e(e, "Failed to send command")
            Result.failure(e)
        }
    }
    
    /**
     * Request fresh sensor reading
     */
    suspend fun requestReading() {
        sendCommand("READ\n")
    }
    
    /**
     * Calibrate sensor
     */
    suspend fun calibrateSensor() {
        sendCommand("CALIBRATE\n")
    }
    
    /**
     * Disconnect from device
     */
    fun disconnect() {
        try {
            inputStream?.close()
            outputStream?.close()
            bluetoothSocket?.close()
            
            inputStream = null
            outputStream = null
            bluetoothSocket = null
            
            _sensorState.value = SensorState.Disconnected
            Timber.d("Disconnected from device")
        } catch (e: IOException) {
            Timber.e(e, "Error during disconnect")
        }
    }
    
    /**
     * Check if currently connected
     */
    fun isConnected(): Boolean {
        return bluetoothSocket?.isConnected == true
    }
}
