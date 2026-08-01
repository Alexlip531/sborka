package com.zai.planreminder.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Сущность плана с напоминанием.
 */
@Entity(tableName = "plans")
data class Plan(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    /** Epoch millis когда напомнить. 0 = без напоминания. */
    val reminderTime: Long = 0L,
    /** Когда план создан (epoch millis). */
    val createdAt: Long = System.currentTimeMillis(),
    /** Выполнен ли план. */
    val isDone: Boolean = false,
    /** Выполнен в (epoch millis) или 0. */
    val completedAt: Long = 0L,
    /** Цвет акцента (для визуальной метки). */
    val colorIndex: Int = 0,
)
