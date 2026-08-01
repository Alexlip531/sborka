package com.zai.sugar.medical

/**
 * Оценка артериального давления.
 *
 * Классификация по ESH/ESC (Европейское общество гипертензии):
 *   Оптимальное: < 120 / 80
 *   Нормальное: 120-129 / 80-84
 *   Высокое нормальное: 130-139 / 85-89
 *   Гипертония 1 ст.: 140-159 / 90-99
 *   Гипертония 2 ст.: 160-179 / 100-109
 *   Гипертония 3 ст.: ≥ 180 / ≥ 110
 *   Изолированная систолическая: ≥ 140 / < 90
 *
 * Гипотония: < 90 / 60
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
        // Гипотония.
        if (systolic < 90 || diastolic < 60) {
            return Result(
                Status.HYPOTENSION, "Пониженное", COLOR_LOW,
                "Пониженное давление. Выпейте воды, прилягте. Если кружится голова или слабость — обратитесь к врачу."
            )
        }
        // Гипертония 3 степени.
        if (systolic >= 180 || diastolic >= 110) {
            return Result(
                Status.HYPERTENSION_3, "Очень высокое", COLOR_HIGH,
                "Очень высокое давление! При появлении боли в груди, нарушении речи или слабости в конечностях — немедленно вызывайте скорую."
            )
        }
        // Гипертония 2 степени.
        if (systolic >= 160 || diastolic >= 100) {
            return Result(
                Status.HYPERTENSION_2, "Высокое", COLOR_HIGH,
                "Высокое давление. Примите назначенный врачом препарат. Если не снижается — обратитесь к врачу."
            )
        }
        // Гипертония 1 степени.
        if (systolic >= 140 || diastolic >= 90) {
            return Result(
                Status.HYPERTENSION_1, "Повышенное", COLOR_ELEVATED,
                "Давление повышено. Измерьте повторно через 5 минут в покое. При стабильном повышении — обратитесь к врачу."
            )
        }
        // Высокое нормальное.
        if (systolic >= 130 || diastolic >= 85) {
            return Result(
                Status.HIGH_NORMAL, "Высокое нормальное", COLOR_ELEVATED,
                "Давление на верхней границе нормы. Сократите соль, больше гуляйте, следите за весом."
            )
        }
        // Нормальное.
        if (systolic >= 120 || diastolic >= 80) {
            return Result(
                Status.NORMAL, "Нормальное", COLOR_NORMAL,
                "Давление в норме. Продолжайте здоровый образ жизни."
            )
        }
        // Оптимальное.
        return Result(
            Status.OPTIMAL, "Оптимальное", COLOR_NORMAL,
            "Отличное давление! Так держать."
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
        Status.HYPERTENSION_1 -> COLOR_ELEVATED
        Status.HYPERTENSION_2 -> COLOR_HIGH
        Status.HYPERTENSION_3 -> COLOR_HIGH
    }
}
