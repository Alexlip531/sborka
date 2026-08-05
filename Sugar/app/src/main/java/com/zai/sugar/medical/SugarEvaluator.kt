package com.zai.sugar.medical

import android.content.Context
import com.zai.sugar.R

/**
 * Оценка уровня сахара в крови для взрослого с диабетом 2 типа.
 *
 * Целевые показатели (ADA / ЭНЦ) для СД 2 типа, ммоль/л:
 *
 *   Состояние                       | Натощак / до еды | Через 2 ч после еды
 *   --------------------------------|------------------|---------------------
 *   Пониженный (гипогликемия)       | < 4.0            | < 4.0
 *   Целевая норма                   | 4.4 – 7.2        | < 10.0
 *   Высокий (гипергликемия)         | > 7.2            | > 10.0
 *
 * Индивидуальная цель может меняться в зависимости от возраста и наличия
 * осложнений (например, для пожилых натощак — до 7.5–8.0, после еды — до 10.0–11.0).
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
     * Пороговые значения по таблице целевых показателей для СД 2 типа.
     */
    private const val LOW_THRESHOLD = 4.0       // ниже — гипогликемия
    private const val BEFORE_MEAL_UPPER = 7.2   // целевой верх до еды
    private const val AFTER_MEAL_UPPER = 10.0   // целевой верх после еды

    fun evaluate(value: Float, beforeMeal: Boolean): Result {
        val v = value.toDouble()
        if (beforeMeal) {
            return when {
                // Гипогликемия: < 4.0 — нужны быстрые углеводы.
                v < LOW_THRESHOLD -> Result(
                    Status.LOW,
                    "Низкий сахар (гипогликемия)",
                    COLOR_LOW,
                    "Гипогликемия. Съешьте 3-4 таблетки глюкозы или выпейте 100 мл сока (быстрые углеводы). Если состояние не улучшается — вызовите скорую."
                )
                // Целевая норма до еды: 4.0 – 7.2.
                v <= BEFORE_MEAL_UPPER -> Result(
                    Status.NORMAL,
                    "Норма",
                    COLOR_NORMAL,
                    "Сахар в целевом диапазоне до еды (4.0–7.2 ммоль/л). Продолжайте соблюдать диету и схему лечения."
                )
                // Выше 7.2 — гипергликемия.
                else -> Result(
                    Status.HIGH,
                    "Высокий (гипергликемия)",
                    COLOR_HIGH,
                    "Сахар выше цели (> 7.2 ммоль/л натощак). Выпейте воды, погуляйте 20–30 минут. При повторных высоких показателях обратитесь к эндокринологу для коррекции лечения."
                )
            }
        } else {
            return when {
                // Гипогликемия после еды: < 4.0.
                v < LOW_THRESHOLD -> Result(
                    Status.LOW,
                    "Низкий сахар (гипогликемия)",
                    COLOR_LOW,
                    "Гипогликемия. Съешьте 3-4 таблетки глюкозы или выпейте 100 мл сока (быстрые углеводы). При повторении — обратитесь к врачу."
                )
                // Целевая норма после еды: до 10.0.
                v <= AFTER_MEAL_UPPER -> Result(
                    Status.NORMAL,
                    "Норма",
                    COLOR_NORMAL,
                    "Сахар после еды в целевом диапазоне (до 10.0 ммоль/л) — отлично!"
                )
                // Выше 10.0 — гипергликемия.
                else -> Result(
                    Status.HIGH,
                    "Высокий (гипергликемия)",
                    COLOR_HIGH,
                    "Сахар выше цели (> 10.0 ммоль/л после еды). Уменьшите порцию углеводов, больше гуляйте. При стабильном повышении — обратитесь к врачу для коррекции терапии."
                )
            }
        }
    }

    const val COLOR_LOW = 0xFF3B82F6.toInt()      // синий
    const val COLOR_NORMAL = 0xFF10B981.toInt()   // зелёный
    const val COLOR_ELEVATED = 0xFFF59E0B.toInt() // оранжевый (зарезервировано, не используется в оценке)
    const val COLOR_HIGH = 0xFFEF4444.toInt()     // красный

    fun colorFor(status: Status): Int = when (status) {
        Status.LOW -> COLOR_LOW
        Status.NORMAL -> COLOR_NORMAL
        Status.ELEVATED -> COLOR_ELEVATED
        Status.HIGH -> COLOR_HIGH
    }
}
