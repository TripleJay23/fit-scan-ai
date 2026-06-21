package com.fitscan.app

import android.app.Application
import com.fitscan.app.data.local.MeasurementDatabase
import com.fitscan.app.data.repository.MeasurementRepository
import com.fitscan.app.domain.usecase.AnalyzeImageUseCase
import com.fitscan.app.domain.usecase.GetMeasurementHistoryUseCase
import com.fitscan.app.ml.PoseDetector

class FitScanApp : Application() {

    val database by lazy { MeasurementDatabase.getDatabase(this) }
    val repository by lazy { MeasurementRepository(database.measurementDao) }
    
    val poseDetector by lazy { PoseDetector(this) }
    
    val analyzeImageUseCase by lazy { AnalyzeImageUseCase(this, repository) }
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
