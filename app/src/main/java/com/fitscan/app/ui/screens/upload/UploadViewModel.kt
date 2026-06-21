package com.fitscan.app.ui.screens.upload

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.fitscan.app.domain.model.ScanResult
import com.fitscan.app.domain.usecase.AnalyzeImageUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class UploadUiState {
    object Idle : UploadUiState()
    object Processing : UploadUiState()
    data class Complete(val result: ScanResult) : UploadUiState()
    data class Error(val message: String) : UploadUiState()
}

class UploadViewModel(
    private val analyzeImageUseCase: AnalyzeImageUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<UploadUiState>(UploadUiState.Idle)
    val uiState: StateFlow<UploadUiState> = _uiState.asStateFlow()

    fun analyzeSelectedPhoto(bitmap: Bitmap, heightCm: Float) {
        viewModelScope.launch {
            _uiState.value = UploadUiState.Processing
            analyzeImageUseCase(bitmap, heightCm).fold(
                onSuccess = { result ->
                    _uiState.value = UploadUiState.Complete(result)
                },
                onFailure = { error ->
                    _uiState.value = UploadUiState.Error(error.message ?: "Failed to process photo")
                }
            )
        }
    }

    fun resetState() {
        _uiState.value = UploadUiState.Idle
    }
}

class UploadViewModelFactory(
    private val analyzeImageUseCase: AnalyzeImageUseCase
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UploadViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return UploadViewModel(analyzeImageUseCase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
