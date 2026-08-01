package com.zai.sugar.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

object DateUtils {

    fun now(): Long = System.currentTimeMillis()

    fun startOfDay(timeMs: Long): Long {
        val c = Calendar.getInstance()
        c.timeInMillis = timeMs
        c.set(Calendar.HOUR_OF_DAY, 0)
        c.set(Calendar.MINUTE, 0)
        c.set(Calendar.SECOND, 0)
        c.set(Calendar.MILLISECOND, 0)
        return c.timeInMillis
    }

    fun endOfDay(timeMs: Long): Long = startOfDay(timeMs) + TimeUnit.DAYS.toMillis(1) - 1

    fun startOfMonth(timeMs: Long): Long {
        val c = Calendar.getInstance()
        c.timeInMillis = timeMs
        c.set(Calendar.DAY_OF_MONTH, 1)
        c.set(Calendar.HOUR_OF_DAY, 0)
        c.set(Calendar.MINUTE, 0)
        c.set(Calendar.SECOND, 0)
        c.set(Calendar.MILLISECOND, 0)
        return c.timeInMillis
    }

    fun startOfWeek(timeMs: Long): Long {
        val c = Calendar.getInstance()
        c.timeInMillis = timeMs
        c.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        c.set(Calendar.HOUR_OF_DAY, 0)
        c.set(Calendar.MINUTE, 0)
        c.set(Calendar.SECOND, 0)
        c.set(Calendar.MILLISECOND, 0)
        return c.timeInMillis
    }

    fun formatTime(timeMs: Long): String =
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(timeMs))

    fun formatDate(timeMs: Long): String =
        SimpleDateFormat("d MMMM yyyy", Locale("ru")).format(Date(timeMs))

    fun formatDateShort(timeMs: Long): String =
        SimpleDateFormat("d.MM.yy", Locale.getDefault()).format(Date(timeMs))

    fun formatDateTime(timeMs: Long): String {
        val date = SimpleDateFormat("d MMM, HH:mm", Locale("ru")).format(Date(timeMs))
        return date
    }

    fun formatRelative(timeMs: Long): String {
        val diff = now() - timeMs
        return when {
            diff < TimeUnit.MINUTES.toMillis(1) -> "только что"
            diff < TimeUnit.HOURS.toMillis(1) -> "${TimeUnit.MILLISECONDS.toMinutes(diff)} мин назад"
            diff < TimeUnit.DAYS.toMillis(1) -> "${TimeUnit.MILLISECONDS.toHours(diff)} ч назад"
            diff < TimeUnit.DAYS.toMillis(7) -> "${TimeUnit.MILLISECONDS.toDays(diff)} дн назад"
            else -> formatDateShort(timeMs)
        }
    }

    fun isSameDay(t1: Long, t2: Long): Boolean =
        startOfDay(t1) == startOfDay(t2)

    fun isToday(timeMs: Long): Boolean = isSameDay(timeMs, now())

    fun daysAgo(days: Int): Long = startOfDay(now() - TimeUnit.DAYS.toMillis(days.toLong()))

    fun monthLabel(timeMs: Long): String =
        SimpleDateFormat("LLLL yyyy", Locale("ru")).format(Date(timeMs)).replaceFirstChar { it.uppercase() }

    fun dayOfMonth(timeMs: Long): Int =
        Calendar.getInstance().apply { this.timeInMillis = timeMs }.get(Calendar.DAY_OF_MONTH)

    /**
     * Возвращает список дней в текущем месяце с метаданными.
     */
    fun daysInMonth(timeMs: Long): List<Long> {
        val cal = Calendar.getInstance()
        cal.timeInMillis = timeMs
        cal.set(Calendar.DAY_OF_MONTH, 1)
        val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
        // Понедельник = 2 в Calendar, нужно сместить.
        val offset = (firstDayOfWeek - Calendar.MONDAY + 7) % 7
        cal.add(Calendar.DAY_OF_MONTH, -offset)

        val result = mutableListOf<Long>()
        val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        val totalCells = ((daysInMonth + offset + 6) / 7) * 7
        for (i in 0 until totalCells) {
            result.add(cal.timeInMillis)
            cal.add(Calendar.DAY_OF_MONTH, 1)
        }
        return result
    }
}
