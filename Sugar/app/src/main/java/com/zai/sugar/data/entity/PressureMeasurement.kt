package com.zai.sugar.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Запись об артериальном давлении.
 * systolic / diastolic в мм рт. ст., pulse — уд/мин.
 */
@Entity(tableName = "pressure_measurements")
data class PressureMeasurement(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val systolic: Int,
    val diastolic: Int,
    val pulse: Int = 0,
    /** Когда измерено. */
    val measuredAt: Long = System.currentTimeMillis(),
    /** На какой руке. */
    val arm: String = "Левая",
    /** Заметка. */
    val note: String = "",
)
