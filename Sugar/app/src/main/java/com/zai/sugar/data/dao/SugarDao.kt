package com.zai.sugar.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.zai.sugar.data.entity.SugarMeasurement
import kotlinx.coroutines.flow.Flow

@Dao
interface SugarDao {

    @Query("SELECT * FROM sugar_measurements ORDER BY measuredAt DESC")
    fun observeAll(): Flow<List<SugarMeasurement>>

    @Query("SELECT * FROM sugar_measurements WHERE measuredAt BETWEEN :start AND :end ORDER BY measuredAt DESC")
    fun observeBetween(start: Long, end: Long): Flow<List<SugarMeasurement>>

    @Query("SELECT * FROM sugar_measurements WHERE measuredAt BETWEEN :start AND :end ORDER BY measuredAt ASC")
    suspend fun getBetween(start: Long, end: Long): List<SugarMeasurement>

    @Query("SELECT * FROM sugar_measurements ORDER BY measuredAt DESC LIMIT :limit")
    suspend fun getRecent(limit: Int): List<SugarMeasurement>

    @Query("SELECT * FROM sugar_measurements WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): SugarMeasurement?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: SugarMeasurement): Long

    @Update
    suspend fun update(item: SugarMeasurement)

    @Delete
    suspend fun delete(item: SugarMeasurement)

    @Query("DELETE FROM sugar_measurements WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT COUNT(*) FROM sugar_measurements")
    suspend fun count(): Int

    @Query("SELECT AVG(value) FROM sugar_measurements WHERE beforeMeal = 1")
    suspend fun avgBeforeMeal(): Float?

    @Query("SELECT AVG(value) FROM sugar_measurements WHERE beforeMeal = 0")
    suspend fun avgAfterMeal(): Float?

    @Query("SELECT * FROM sugar_measurements WHERE measuredAt BETWEEN :start AND :end ORDER BY measuredAt DESC")
    suspend fun getByDateRange(start: Long, end: Long): List<SugarMeasurement>
}
