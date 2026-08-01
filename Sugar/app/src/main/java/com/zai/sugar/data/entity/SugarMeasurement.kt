package com.zai.sugar.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Запись об уровне сахара в крови.
 * Значение хранится в ммоль/л.
 */
@Entity(tableName = "sugar_measurements")
data class SugarMeasurement(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    /** Уровень глюкозы в ммоль/л. */
    val value: Float,
    /** Когда измерено (epoch millis). */
    val measuredAt: Long = System.currentTimeMillis(),
    /** true — до еды, false — после еды. */
    val beforeMeal: Boolean = true,
    /** Заметка пользователя. */
    val note: String = "",
    /** Цвет метки (для удобства). */
    val colorIndex: Int = 0,
)
