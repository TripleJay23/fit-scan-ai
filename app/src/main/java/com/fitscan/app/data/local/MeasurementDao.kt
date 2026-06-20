package com.fitscan.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MeasurementDao {

    @Query("SELECT * FROM measurements ORDER BY timestamp DESC")
    fun getAll(): Flow<List<MeasurementEntity>>

    @Query("SELECT * FROM measurements WHERE id = :id LIMIT 1")
    suspend fun getById(id: Int): MeasurementEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: MeasurementEntity): Long

    @Query("DELETE FROM measurements WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("DELETE FROM measurements")
    suspend fun deleteAll()
}
