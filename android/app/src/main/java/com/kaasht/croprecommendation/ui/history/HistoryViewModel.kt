package com.kaasht.croprecommendation.ui.history

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kaasht.croprecommendation.data.model.PredictionEntity
import com.kaasht.croprecommendation.data.model.Resource
import com.kaasht.croprecommendation.data.repository.CropRepository
import com.kaasht.croprecommendation.utils.ExportHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val repository: CropRepository,
    private val exportHelper: ExportHelper
) : ViewModel() {

    private val _predictions = MutableLiveData<Resource<List<PredictionEntity>>>()
    val predictions: LiveData<Resource<List<PredictionEntity>>> = _predictions

    private val _filteredPredictions = MutableLiveData<List<PredictionEntity>>()
    val filteredPredictions: LiveData<List<PredictionEntity>> = _filteredPredictions

    private val _exportStatus = MutableLiveData<Resource<String>>()
    val exportStatus: LiveData<Resource<String>> = _exportStatus

    enum class TimeRange { ALL, WEEK, MONTH, YEAR }

    fun loadPredictions() {
        viewModelScope.launch {
            _predictions.value = Resource.Loading()
            try {
                repository.getPredictionHistory().collectLatest { list ->
                    _predictions.value = Resource.Success(list)
                    _filteredPredictions.value = list
                }
            } catch (e: Exception) {
                _predictions.value = Resource.Error(e.message ?: "Failed to load history")
            }
        }
    }

    fun deletePrediction(prediction: PredictionEntity) {
        viewModelScope.launch {
            repository.deletePrediction(prediction)
        }
    }

    fun undoDelete() {
        // Logic to restore would go here if we kept a backup
    }

    fun clearAllPredictions() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    fun filterByTimeRange(range: TimeRange) {
        val all = _predictions.value?.data ?: return
        val now = System.currentTimeMillis()
        val filtered = when (range) {
            TimeRange.WEEK -> all.filter { now - it.timestamp < 7 * 24 * 3600 * 1000 }
            TimeRange.MONTH -> all.filter { now - it.timestamp < 30L * 24 * 3600 * 1000 }
            TimeRange.YEAR -> all.filter { now - it.timestamp < 365L * 24 * 3600 * 1000 }
            TimeRange.ALL -> all
        }
        _filteredPredictions.value = filtered
    }

    fun searchPredictions(query: String) {
        val all = _predictions.value?.data ?: return
        if (query.isEmpty()) {
            _filteredPredictions.value = all
        } else {
            _filteredPredictions.value = all.filter {
                it.predictedCrop.contains(query, ignoreCase = true) ||
                it.district.contains(query, ignoreCase = true)
            }
        }
    }

    fun exportToCsv(context: Context) {
        _exportStatus.value = Resource.Loading()
        val list = _filteredPredictions.value ?: emptyList()
        _exportStatus.value = exportHelper.exportToCsv(list)
    }

    fun exportToPdf(context: Context) {
        _exportStatus.value = Resource.Loading()
        val list = _filteredPredictions.value ?: emptyList()
        _exportStatus.value = exportHelper.exportToPdf(list)
    }

    fun syncWithCloud() {
        // Trigger sync repository
    }
}
