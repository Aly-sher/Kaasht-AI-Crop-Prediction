package com.kaasht.croprecommendation.ui.soil

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kaasht.croprecommendation.data.sensor.BluetoothSensorManager
import com.kaasht.croprecommendation.data.sensor.SensorState
import com.kaasht.croprecommendation.data.sensor.SoilSensorData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import android.bluetooth.BluetoothDevice

@HiltViewModel
class SoilSensorViewModel @Inject constructor(
    private val bluetoothManager: BluetoothSensorManager
) : ViewModel() {
    
    private val _sensorState = MutableLiveData<SensorState>()
    val sensorState: LiveData<SensorState> = _sensorState
    
    private val _pairedDevices = MutableLiveData<List<BluetoothDevice>>()
    val pairedDevices: LiveData<List<BluetoothDevice>> = _pairedDevices
    
    private val _latestReading = MutableLiveData<SoilSensorData>()
    val latestReading: LiveData<SoilSensorData> = _latestReading
    
    init {
        observeSensorState()
    }
    
    private fun observeSensorState() {
        viewModelScope.launch {
            bluetoothManager.sensorState.collect { state ->
                _sensorState.value = state
                if (state is SensorState.DataReceived) {
                    _latestReading.value = state.data
                }
            }
        }
    }
    
    fun checkBluetoothAvailable(): Boolean {
        return bluetoothManager.isBluetoothAvailable()
    }
    
    fun hasBluetoothPermissions(): Boolean {
        return bluetoothManager.hasBluetoothPermissions()
    }
    
    fun loadPairedDevices() {
        viewModelScope.launch {
            val devices = bluetoothManager.getPairedDevices()
            _pairedDevices.value = devices
            Timber.d("Found ${devices.size} paired devices")
        }
    }
    
    fun connectToDevice(device: BluetoothDevice) {
        viewModelScope.launch {
            bluetoothManager.connectToDevice(device)
        }
    }
    
    fun requestReading() {
        viewModelScope.launch {
            bluetoothManager.requestReading()
        }
    }
    
    fun calibrateSensor() {
        viewModelScope.launch {
            bluetoothManager.calibrateSensor()
        }
    }
    
    fun disconnect() {
        bluetoothManager.disconnect()
    }
    
    override fun onCleared() {
        super.onCleared()
        bluetoothManager.disconnect()
    }
}
