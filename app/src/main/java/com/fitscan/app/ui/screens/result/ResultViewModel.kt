package com.fitscan.app.ui.screens.result

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.fitscan.app.data.repository.MeasurementRepository
import com.fitscan.app.domain.model.ScanResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class ResultUiState {
    object Loading : ResultUiState()
    data class Success(val scanResult: ScanResult) : ResultUiState()
    data class Error(val message: String) : ResultUiState()
}

class ResultViewModel(
    private val repository: MeasurementRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ResultUiState>(ResultUiState.Loading)
    val uiState: StateFlow<ResultUiState> = _uiState.asStateFlow()

    fun loadScanResult(scanId: Int) {
        viewModelScope.launch {
            _uiState.value = ResultUiState.Loading
            try {
                val result = repository.getScanById(scanId)
                if (result != null) {
                    _uiState.value = ResultUiState.Success(result)
                } else {
                    _uiState.value = ResultUiState.Error("Session not found")
                }
            } catch (e: Exception) {
                _uiState.value = ResultUiState.Error(e.message ?: "Failed to load result")
            }
        }
    }
}

class ResultViewModelFactory(
    private val repository: MeasurementRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ResultViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ResultViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
