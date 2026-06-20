package com.fitscan.app.ui.screens.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.fitscan.app.data.repository.MeasurementRepository
import com.fitscan.app.domain.model.ScanResult
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HistoryViewModel(
    private val repository: MeasurementRepository
) : ViewModel() {

    val allScans: StateFlow<List<ScanResult>> = repository.allScans
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun deleteScan(id: Int) {
        viewModelScope.launch {
            repository.deleteScan(id)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearAllData()
        }
    }
}

class HistoryViewModelFactory(
    private val repository: MeasurementRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HistoryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HistoryViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
