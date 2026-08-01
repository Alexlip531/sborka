package com.zai.planreminder.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.zai.planreminder.data.entity.Plan
import kotlinx.coroutines.flow.Flow

@Dao
interface PlanDao {

    @Query("SELECT * FROM plans ORDER BY isDone ASC, reminderTime ASC, createdAt DESC")
    fun observeAll(): Flow<List<Plan>>

    @Query("SELECT * FROM plans WHERE isDone = 0 ORDER BY reminderTime ASC")
    fun observeActive(): Flow<List<Plan>>

    @Query("SELECT * FROM plans WHERE isDone = 1 ORDER BY completedAt DESC")
    fun observeDone(): Flow<List<Plan>>

    @Query("SELECT * FROM plans WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): Plan?

    @Query("SELECT * FROM plans WHERE isDone = 0 AND reminderTime > 0 ORDER BY reminderTime ASC")
    suspend fun getActiveWithReminders(): List<Plan>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(plan: Plan): Long

    @Update
    suspend fun update(plan: Plan)

    @Delete
    suspend fun delete(plan: Plan)

    @Query("DELETE FROM plans WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("UPDATE plans SET isDone = :done, completedAt = :completedAt WHERE id = :id")
    suspend fun setDone(id: Long, done: Boolean, completedAt: Long)
}
