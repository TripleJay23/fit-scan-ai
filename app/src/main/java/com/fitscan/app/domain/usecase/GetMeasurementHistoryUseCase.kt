package com.fitscan.app.domain.usecase

import com.fitscan.app.data.repository.MeasurementRepository
import com.fitscan.app.domain.model.ScanResult
import kotlinx.coroutines.flow.Flow

class GetMeasurementHistoryUseCase(private val repository: MeasurementRepository) {
    operator fun invoke(): Flow<List<ScanResult>> {
        return repository.allScans
    }
}
