package com.zai.planreminder.util

import android.content.Context
import android.text.format.DateUtils
import com.zai.planreminder.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

object TimeFormatter {

    fun formatDateTime(context: Context, timeMs: Long): String {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply { timeInMillis = timeMs }

        val sameDay = now.get(Calendar.YEAR) == target.get(Calendar.YEAR) &&
                now.get(Calendar.DAY_OF_YEAR) == target.get(Calendar.DAY_OF_YEAR)

        val tomorrow = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }
        val isTomorrow = tomorrow.get(Calendar.YEAR) == target.get(Calendar.YEAR) &&
                tomorrow.get(Calendar.DAY_OF_YEAR) == target.get(Calendar.DAY_OF_YEAR)

        val timeFmt = SimpleDateFormat("HH:mm", Locale.getDefault())
        val time = timeFmt.format(Date(timeMs))

        return when {
            sameDay -> context.getString(R.string.section_today) + ", " + time
            isTomorrow -> context.getString(R.string.section_tomorrow) + ", " + time
            else -> SimpleDateFormat("d MMM, HH:mm", Locale("ru")).format(Date(timeMs))
        }
    }

    fun formatRelativeStatus(context: Context, timeMs: Long): String {
        val diff = timeMs - System.currentTimeMillis()
        return if (diff <= 0) {
            val overdue = -diff
            val mins = TimeUnit.MILLISECONDS.toMinutes(overdue)
            val hours = TimeUnit.MILLISECONDS.toHours(overdue)
            val days = TimeUnit.MILLISECONDS.toDays(overdue)
            when {
                days > 0 -> context.getString(R.string.overdue_days, days.toInt())
                hours > 0 -> context.getString(R.string.overdue_hours, hours.toInt())
                else -> context.getString(R.string.overdue_minutes, mins.toInt())
            }
        } else {
            val mins = TimeUnit.MILLISECONDS.toMinutes(diff)
            val hours = TimeUnit.MILLISECONDS.toHours(diff)
            val days = TimeUnit.MILLISECONDS.toDays(diff)
            when {
                days > 0 -> context.getString(R.string.in_days, days.toInt())
                hours > 0 -> context.getString(R.string.in_hours, hours.toInt())
                mins > 0 -> context.getString(R.string.in_minutes, mins.toInt())
                else -> context.getString(R.string.default_time)
            }
        }
    }

    fun formatDoneAt(context: Context, timeMs: Long): String {
        val date = SimpleDateFormat("d MMM, HH:mm", Locale("ru")).format(Date(timeMs))
        return "✓ $date"
    }

    fun formatDate(timeMs: Long): String =
        SimpleDateFormat("d MMMM yyyy", Locale("ru")).format(Date(timeMs))

    fun formatTime(timeMs: Long): String =
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(timeMs))
}
