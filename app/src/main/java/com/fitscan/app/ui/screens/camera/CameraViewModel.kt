package com.fitscan.app.ui.screens.camera

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.fitscan.app.domain.model.AnalysisRequest
import com.fitscan.app.domain.model.CameraCalibration
import androidx.lifecycle.viewModelScope
import com.fitscan.app.domain.model.PoseLandmark
import com.fitscan.app.domain.model.ScanResult
import com.fitscan.app.domain.usecase.AnalyzeImageUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class CameraUiState {
    object Idle : CameraUiState()
    object Detecting : CameraUiState()
    object Measuring : CameraUiState()
    data class Complete(val result: ScanResult) : CameraUiState()
    data class Error(val message: String) : CameraUiState()
}

class CameraViewModel(
    private val analyzeImageUseCase: AnalyzeImageUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<CameraUiState>(CameraUiState.Idle)
    val uiState: StateFlow<CameraUiState> = _uiState.asStateFlow()

    private val _detectedLandmarks = MutableStateFlow<List<PoseLandmark>>(emptyList())
    val detectedLandmarks: StateFlow<List<PoseLandmark>> = _detectedLandmarks.asStateFlow()

    private var activeBitmap: Bitmap? = null
    private var activeCameraCalibration: CameraCalibration? = null

    fun onPoseDetected(landmarks: List<PoseLandmark>, bitmap: Bitmap, cameraCalibration: CameraCalibration?) {
        _detectedLandmarks.value = landmarks
        activeBitmap = bitmap
        activeCameraCalibration = cameraCalibration
    }

    fun lockAndAnalyze(heightCm: Float) {
        val bitmap = activeBitmap ?: return
        
        viewModelScope.launch {
            _uiState.value = CameraUiState.Measuring
            analyzeImageUseCase(
                AnalysisRequest(
                    bitmap = bitmap,
                    userHeightCm = heightCm,
                    cameraCalibration = activeCameraCalibration
                )
            ).fold(
                onSuccess = { result ->
                    _uiState.value = CameraUiState.Complete(result)
                },
                onFailure = { error ->
                    _uiState.value = CameraUiState.Error(error.message ?: "Analysis failed")
                }
            )
        }
    }

    fun resetState() {
        _uiState.value = CameraUiState.Idle
        _detectedLandmarks.value = emptyList()
        activeBitmap = null
        activeCameraCalibration = null
    }
}

class CameraViewModelFactory(
    private val analyzeImageUseCase: AnalyzeImageUseCase
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CameraViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CameraViewModel(analyzeImageUseCase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
