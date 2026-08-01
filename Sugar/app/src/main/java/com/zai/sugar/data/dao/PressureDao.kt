package com.zai.sugar.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.zai.sugar.data.entity.PressureMeasurement
import kotlinx.coroutines.flow.Flow

@Dao
interface PressureDao {

    @Query("SELECT * FROM pressure_measurements ORDER BY measuredAt DESC")
    fun observeAll(): Flow<List<PressureMeasurement>>

    @Query("SELECT * FROM pressure_measurements WHERE measuredAt BETWEEN :start AND :end ORDER BY measuredAt DESC")
    fun observeBetween(start: Long, end: Long): Flow<List<PressureMeasurement>>

    @Query("SELECT * FROM pressure_measurements WHERE measuredAt BETWEEN :start AND :end ORDER BY measuredAt ASC")
    suspend fun getBetween(start: Long, end: Long): List<PressureMeasurement>

    @Query("SELECT * FROM pressure_measurements ORDER BY measuredAt DESC LIMIT :limit")
    suspend fun getRecent(limit: Int): List<PressureMeasurement>

    @Query("SELECT * FROM pressure_measurements WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): PressureMeasurement?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: PressureMeasurement): Long

    @Update
    suspend fun update(item: PressureMeasurement)

    @Delete
    suspend fun delete(item: PressureMeasurement)

    @Query("DELETE FROM pressure_measurements WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT AVG(systolic) FROM pressure_measurements")
    suspend fun avgSystolic(): Int?

    @Query("SELECT AVG(diastolic) FROM pressure_measurements")
    suspend fun avgDiastolic(): Int?

    @Query("SELECT AVG(pulse) FROM pressure_measurements WHERE pulse > 0")
    suspend fun avgPulse(): Int?

    @Query("SELECT * FROM pressure_measurements WHERE measuredAt BETWEEN :start AND :end ORDER BY measuredAt DESC")
    suspend fun getByDateRange(start: Long, end: Long): List<PressureMeasurement>
}
