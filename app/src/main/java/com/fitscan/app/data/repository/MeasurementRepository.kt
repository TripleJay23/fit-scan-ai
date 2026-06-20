package com.fitscan.app.data.repository

import com.fitscan.app.data.local.MeasurementDao
import com.fitscan.app.data.local.MeasurementEntity
import com.fitscan.app.domain.model.ScanResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class MeasurementRepository(private val measurementDao: MeasurementDao) {

    val allScans: Flow<List<ScanResult>> = measurementDao.getAllMeasurements().map { list ->
        list.map { it.toScanResult() }
    }

    suspend fun getScanById(id: Int): ScanResult? {
        return measurementDao.getMeasurementById(id.toLong())?.toScanResult()
    }

    suspend fun saveScan(scanResult: ScanResult): Int {
        val entity = MeasurementEntity.fromScanResult(scanResult)
        val rowId = measurementDao.insertMeasurement(entity)
        return rowId.toInt()
    }

    suspend fun deleteScan(id: Int) {
        measurementDao.deleteById(id.toLong())
    }

    suspend fun clearAllData() {
        measurementDao.deleteAllMeasurements()
    }
}
