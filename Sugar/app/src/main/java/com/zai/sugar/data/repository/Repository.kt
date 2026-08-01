package com.zai.sugar.data.repository

import com.zai.sugar.data.dao.PressureDao
import com.zai.sugar.data.dao.SugarDao
import com.zai.sugar.data.entity.PressureMeasurement
import com.zai.sugar.data.entity.SugarMeasurement
import kotlinx.coroutines.flow.Flow

class Repository(
    private val sugarDao: SugarDao,
    private val pressureDao: PressureDao,
) {
    // === Sugar ===
    fun observeAllSugar(): Flow<List<SugarMeasurement>> = sugarDao.observeAll()
    fun observeSugarBetween(start: Long, end: Long): Flow<List<SugarMeasurement>> =
        sugarDao.observeBetween(start, end)
    suspend fun getSugarByDateRange(start: Long, end: Long): List<SugarMeasurement> =
        sugarDao.getByDateRange(start, end)
    suspend fun getRecentSugar(limit: Int): List<SugarMeasurement> = sugarDao.getRecent(limit)
    suspend fun getSugarById(id: Long): SugarMeasurement? = sugarDao.getById(id)
    suspend fun insertSugar(item: SugarMeasurement): Long = sugarDao.insert(item)
    suspend fun updateSugar(item: SugarMeasurement) = sugarDao.update(item)
    suspend fun deleteSugar(item: SugarMeasurement) = sugarDao.delete(item)
    suspend fun deleteSugarById(id: Long) = sugarDao.deleteById(id)
    suspend fun sugarCount(): Int = sugarDao.count()
    suspend fun avgSugarBefore(): Float? = sugarDao.avgBeforeMeal()
    suspend fun avgSugarAfter(): Float? = sugarDao.avgAfterMeal()

    // === Pressure ===
    fun observeAllPressure(): Flow<List<PressureMeasurement>> = pressureDao.observeAll()
    fun observePressureBetween(start: Long, end: Long): Flow<List<PressureMeasurement>> =
        pressureDao.observeBetween(start, end)
    suspend fun getPressureByDateRange(start: Long, end: Long): List<PressureMeasurement> =
        pressureDao.getByDateRange(start, end)
    suspend fun getRecentPressure(limit: Int): List<PressureMeasurement> = pressureDao.getRecent(limit)
    suspend fun getPressureById(id: Long): PressureMeasurement? = pressureDao.getById(id)
    suspend fun insertPressure(item: PressureMeasurement): Long = pressureDao.insert(item)
    suspend fun updatePressure(item: PressureMeasurement) = pressureDao.update(item)
    suspend fun deletePressure(item: PressureMeasurement) = pressureDao.delete(item)
    suspend fun deletePressureById(id: Long) = pressureDao.deleteById(id)
    suspend fun avgSystolic(): Int? = pressureDao.avgSystolic()
    suspend fun avgDiastolic(): Int? = pressureDao.avgDiastolic()
    suspend fun avgPulse(): Int? = pressureDao.avgPulse()
}
