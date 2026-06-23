package com.fitscan.app

import android.app.Application
import com.fitscan.app.data.local.MeasurementDatabase
import com.fitscan.app.data.repository.MeasurementRepository
import com.fitscan.app.domain.usecase.AnalyzeImageUseCase
import com.fitscan.app.domain.usecase.GetMeasurementHistoryUseCase
import com.fitscan.app.ml.CameraCalibrationProvider
import com.fitscan.app.ml.PersonDetector
import com.fitscan.app.ml.PoseDetector

class FitScanApp : Application() {

    val database by lazy { MeasurementDatabase.getDatabase(this) }
    val repository by lazy { MeasurementRepository(database.measurementDao) }
    
    val poseDetector by lazy { PoseDetector(this) }
    val personDetector by lazy { PersonDetector(this) }
    val cameraCalibrationProvider by lazy { CameraCalibrationProvider(this) }
    
    val analyzeImageUseCase by lazy { AnalyzeImageUseCase(repository, poseDetector, personDetector) }
    val getMeasurementHistoryUseCase by lazy { GetMeasurementHistoryUseCase(repository) }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        lateinit var instance: FitScanApp
            private set
    }
}
