package com.zai.planreminder.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.zai.planreminder.data.entity.Plan
import com.zai.planreminder.data.repository.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Планировщик напоминаний через AlarmManager.
 * Использует setAlarmClock для точного пробуждения.
 */
class ReminderScheduler(private val context: Context) {

    private val alarmManager: AlarmManager =
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    /** Запланировать напоминание для конкретного плана. */
    fun schedule(plan: Plan) {
        if (plan.reminderTime <= 0L) {
            cancel(plan.id)
            return
        }
        if (plan.isDone) {
            cancel(plan.id)
            return
        }
        val intent = ReminderReceiver.newIntent(context, plan.id, plan.title, plan.description)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            plan.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setAlarmClock(
                        AlarmManager.AlarmClockInfo(plan.reminderTime, pendingIntent),
                        pendingIntent
                    )
                } else {
                    // Fallback — неточный таймер.
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP, plan.reminderTime, pendingIntent
                    )
                }
            } else {
                alarmManager.setAlarmClock(
                    AlarmManager.AlarmClockInfo(plan.reminderTime, pendingIntent),
                    pendingIntent
                )
            }
        } catch (_: SecurityException) {
            // На некоторых устройствах без SCHEDULE_EXACT_ALARM падает — fallback.
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP, plan.reminderTime, pendingIntent
            )
        }
    }

    /** Отменить напоминание. */
    fun cancel(planId: Long) {
        val intent = Intent(context, ReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            planId.toInt(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }

    /** Перепланировать все активные напоминания (вызывается после BOOT_COMPLETED). */
    suspend fun rescheduleAll() = withContext(Dispatchers.IO) {
        val dao = AppDatabase.get(context).planDao()
        dao.getActiveWithReminders().forEach { schedule(it) }
    }
}
