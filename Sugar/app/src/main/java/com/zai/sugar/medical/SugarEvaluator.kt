package com.zai.sugar.medical

import android.content.Context
import com.zai.sugar.R

/**
 * Оценка уровня сахара в крови для диабетика 2 типа.
 *
 * Источник: рекомендации ВОЗ и клинические рекомендации РФ по СД 2 типа.
 */
object SugarEvaluator {

    enum class Status { LOW, NORMAL, ELEVATED, HIGH }

    data class Result(
        val status: Status,
        val label: String,
        val color: Int,
        val advice: String,
    )

    /**
     * Оценка в зависимости от того, до еды или после.
     *
     * Целевые значения для СД 2 типа:
     *  - Натощак (до еды): 4.4 – 7.2 ммоль/л
     *  - Через 2 часа после еды: до 10.0 ммоль/л
     */
    fun evaluate(value: Float, beforeMeal: Boolean): Result {
        val v = value.toDouble()
        if (beforeMeal) {
            return when {
                v < 3.9 -> Result(
                    Status.LOW, "Низкий сахар", COLOR_LOW,
                    "Возможна гипогликемия. Съешьте 3-4 таблетки глюкозы или 100 мл сока. Если не пройдёт — вызовите скорую."
                )
                v <= 7.2 -> Result(
                    Status.NORMAL, "Норма", COLOR_NORMAL,
                    "Сахар в целевом диапазоне. Продолжайте соблюдать диету и схему лечения."
                )
                v <= 8.0 -> Result(
                    Status.ELEVATED, "Повышен", COLOR_ELEVATED,
                    "Слегка повышен. Выпейте воды, погуляйте 20-30 минут. Пересмотрите углеводы в рационе."
                )
                else -> Result(
                    Status.HIGH, "Высокий", COLOR_HIGH,
                    "Сахар значительно повышен. При повторных высоких показателях обратитесь к эндокринологу для коррекции лечения."
                )
            }
        } else {
            return when {
                v < 3.9 -> Result(
                    Status.LOW, "Низкий сахар", COLOR_LOW,
                    "Возможна гипогликемия. Съешьте что-то сладкое (3-4 таблетки глюкозы или 100 мл сока)."
                )
                v <= 10.0 -> Result(
                    Status.NORMAL, "Норма", COLOR_NORMAL,
                    "Сахар после еды в целевом диапазоне — отлично!"
                )
                v <= 11.1 -> Result(
                    Status.ELEVATED, "Повышен", COLOR_ELEVATED,
                    "Слегка повышен после еды. Больше гуляйте, уменьшите порцию углеводов."
                )
                else -> Result(
                    Status.HIGH, "Высокий", COLOR_HIGH,
                    "Сахар высокий. При стабильном повышении обратитесь к врачу для коррекции терапии."
                )
            }
        }
    }

    const val COLOR_LOW = 0xFF3B82F6.toInt()      // синий
    const val COLOR_NORMAL = 0xFF10B981.toInt()   // зелёный
    const val COLOR_ELEVATED = 0xFFF59E0B.toInt() // оранжевый
    const val COLOR_HIGH = 0xFFEF4444.toInt()     // красный

    fun colorFor(status: Status): Int = when (status) {
        Status.LOW -> COLOR_LOW
        Status.NORMAL -> COLOR_NORMAL
        Status.ELEVATED -> COLOR_ELEVATED
        Status.HIGH -> COLOR_HIGH
    }
}
