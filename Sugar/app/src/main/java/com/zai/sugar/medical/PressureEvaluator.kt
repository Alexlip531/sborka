package com.zai.sugar.medical

/**
 * Оценка артериального давления при диабете 2 типа.
 *
 * Целевые показатели для взрослых с диабетом 2 типа (мм рт. ст.):
 *
 *   Состояние                 | Верхнее (систолическое) | Нижнее (диастолическое)
 *   --------------------------|-------------------------|------------------------
 *   Пониженное (гипотония)    | < 100                   | и/или < 60
 *   Целевая норма             | 120 – 130               | и 70 – 80
 *   Повышенное (гипертония)   | ≥ 130                   | и/или ≥ 80
 *
 * Показатели выше 130/80 мм рт. ст. при диабете уже рассматриваются как повод
 * для коррекции образа жизни или терапии. Внутри категории «повышенное»
 * дополнительно выделяются степени тяжести гипертонии (1, 2, 3) для предупреждения.
 */
object PressureEvaluator {

    enum class Status {
        HYPOTENSION, OPTIMAL, NORMAL, HIGH_NORMAL, HYPERTENSION_1, HYPERTENSION_2, HYPERTENSION_3
    }

    data class Result(
        val status: Status,
        val label: String,
        val color: Int,
        val advice: String,
    )

    fun evaluate(systolic: Int, diastolic: Int): Result {
        // Гипотония: < 100 и/или < 60.
        if (systolic < 100 || diastolic < 60) {
            return Result(
                Status.HYPOTENSION, "Пониженное", COLOR_LOW,
                "Пониженное давление. Выпейте воды, прилягте. Если кружится голова или слабость — обратитесь к врачу."
            )
        }
        // Гипертония 3 степени: ≥ 180 и/или ≥ 110 — критически высокое.
        if (systolic >= 180 || diastolic >= 110) {
            return Result(
                Status.HYPERTENSION_3, "Очень высокое", COLOR_HIGH,
                "Очень высокое давление! При появлении боли в груди, нарушении речи или слабости в конечностях — немедленно вызывайте скорую."
            )
        }
        // Гипертония 2 степени: ≥ 160 и/или ≥ 100.
        if (systolic >= 160 || diastolic >= 100) {
            return Result(
                Status.HYPERTENSION_2, "Высокое", COLOR_HIGH,
                "Высокое давление. Примите назначенный врачом препарат. Если не снижается — обратитесь к врачу."
            )
        }
        // Гипертония 1 степени: ≥ 140 и/или ≥ 90.
        if (systolic >= 140 || diastolic >= 90) {
            return Result(
                Status.HYPERTENSION_1, "Высокое", COLOR_HIGH,
                "Высокое давление. Измерьте повторно через 5 минут в покое. При стабильном повышении — обратитесь к врачу для коррекции терапии."
            )
        }
        // Повышенное (выше цели при диабете): ≥ 130 и/или ≥ 80.
        if (systolic >= 130 || diastolic >= 80) {
            return Result(
                Status.HIGH_NORMAL, "Повышенное", COLOR_ELEVATED,
                "Давление выше целевых значений при диабете (≥ 130 и/или ≥ 80). Сократите соль, больше гуляйте, следите за весом. При стойком повышении — обратитесь к врачу."
            )
        }
        // Целевая норма: 120–130 / 70–80. Значения ниже (100–119 / 60–69)
        // также считаются приемлемыми — относим их к NORMAL.
        if (systolic >= 120 && diastolic >= 70) {
            return Result(
                Status.NORMAL, "Целевая норма", COLOR_NORMAL,
                "Давление в целевом диапазоне (120–130 / 70–80). Продолжайте здоровый образ жизни."
            )
        }
        // Ниже целевой, но не гипотония.
        return Result(
            Status.OPTIMAL, "Ниже целевой", COLOR_NORMAL,
            "Давление ниже целевого, но в пределах нормы. Если чувствуете себя хорошо — ничего делать не нужно."
        )
    }

    const val COLOR_LOW = 0xFF3B82F6.toInt()
    const val COLOR_NORMAL = 0xFF10B981.toInt()
    const val COLOR_ELEVATED = 0xFFF59E0B.toInt()
    const val COLOR_HIGH = 0xFFEF4444.toInt()

    fun colorFor(status: Status): Int = when (status) {
        Status.HYPOTENSION -> COLOR_LOW
        Status.OPTIMAL -> COLOR_NORMAL
        Status.NORMAL -> COLOR_NORMAL
        Status.HIGH_NORMAL -> COLOR_ELEVATED
        Status.HYPERTENSION_1 -> COLOR_HIGH
        Status.HYPERTENSION_2 -> COLOR_HIGH
        Status.HYPERTENSION_3 -> COLOR_HIGH
    }
}
