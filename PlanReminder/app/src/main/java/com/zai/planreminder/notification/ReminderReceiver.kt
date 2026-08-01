package com.zai.planreminder.notification

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.zai.planreminder.R
import com.zai.planreminder.data.repository.AppDatabase
import com.zai.planreminder.ui.main.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val planId = intent.getLongExtra(EXTRA_PLAN_ID, -1L)
        val title = intent.getStringExtra(EXTRA_TITLE) ?: context.getString(R.string.app_name)
        val description = intent.getStringExtra(EXTRA_DESCRIPTION).orEmpty()

        if (planId <= 0) return

        NotificationHelper.ensureChannel(context)

        val contentIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_PLAN_ID, planId)
        }
        val contentPi = PendingIntent.getActivity(
            context, planId.toInt(), contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Действие «Отметить выполненным».
        val doneIntent = Intent(context, ReminderActionReceiver::class.java).apply {
            action = ReminderActionReceiver.ACTION_DONE
            putExtra(EXTRA_PLAN_ID, planId)
        }
        val donePi = PendingIntent.getBroadcast(
            context, planId.toInt() xor 0x10_000, doneIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Действие «Отложить на 15 минут».
        val snoozeIntent = Intent(context, ReminderActionReceiver::class.java).apply {
            action = ReminderActionReceiver.ACTION_SNOOZE
            putExtra(EXTRA_PLAN_ID, planId)
            putExtra(EXTRA_TITLE, title)
            putExtra(EXTRA_DESCRIPTION, description)
        }
        val snoozePi = PendingIntent.getBroadcast(
            context, planId.toInt() xor 0x20_000, snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification: Notification = NotificationCompat.Builder(context, NotificationHelper.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_bell)
            .setContentTitle(context.getString(R.string.notification_title))
            .setContentText(title)
            .setStyle(NotificationCompat.BigTextStyle().bigText(buildString {
                append(title)
                if (description.isNotEmpty()) {
                    append("\n")
                    append(description)
                }
            }))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)
            .setContentIntent(contentPi)
            .addAction(R.drawable.ic_check, context.getString(R.string.notification_action_done), donePi)
            .addAction(R.drawable.ic_clock, context.getString(R.string.notification_action_snooze), snoozePi)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(planId.toInt(), notification)
    }

    companion object {
        const val EXTRA_PLAN_ID = "extra_plan_id"
        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_DESCRIPTION = "extra_description"

        fun newIntent(context: Context, planId: Long, title: String, description: String): Intent =
            Intent(context, ReminderReceiver::class.java).apply {
                putExtra(EXTRA_PLAN_ID, planId)
                putExtra(EXTRA_TITLE, title)
                putExtra(EXTRA_DESCRIPTION, description)
            }
    }
}
