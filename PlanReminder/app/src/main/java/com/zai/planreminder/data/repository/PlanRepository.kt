package com.zai.planreminder.data.repository

import com.zai.planreminder.data.dao.PlanDao
import com.zai.planreminder.data.entity.Plan
import kotlinx.coroutines.flow.Flow

class PlanRepository(private val dao: PlanDao) {

    fun observeAll(): Flow<List<Plan>> = dao.observeAll()
    fun observeActive(): Flow<List<Plan>> = dao.observeActive()
    fun observeDone(): Flow<List<Plan>> = dao.observeDone()

    suspend fun getById(id: Long): Plan? = dao.getById(id)
    suspend fun getActiveWithReminders(): List<Plan> = dao.getActiveWithReminders()

    suspend fun insert(plan: Plan): Long = dao.insert(plan)
    suspend fun update(plan: Plan) = dao.update(plan)
    suspend fun delete(plan: Plan) = dao.delete(plan)
    suspend fun deleteById(id: Long) = dao.deleteById(id)
    suspend fun setDone(id: Long, done: Boolean, completedAt: Long = if (done) System.currentTimeMillis() else 0L) =
        dao.setDone(id, done, completedAt)
}
