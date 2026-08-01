package com.zai.planreminder.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.zai.planreminder.data.entity.Plan
import com.zai.planreminder.data.repository.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ReminderActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val planId = intent.getLongExtra(ReminderReceiver.EXTRA_PLAN_ID, -1L)
        if (planId <= 0) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val repo = AppDatabase.get(context).planDao()
                when (intent.action) {
                    ACTION_DONE -> {
                        repo.setDone(planId, true, System.currentTimeMillis())
                        // Отменить таймер на случай если он ещё активен.
                        ReminderScheduler(context).cancel(planId)
                    }
                    ACTION_SNOOZE -> {
                        val plan = repo.getById(planId) ?: return@launch
                        val newTime = System.currentTimeMillis() + 15L * 60 * 1000
                        repo.update(plan.copy(reminderTime = newTime))
                        ReminderScheduler(context).schedule(plan.copy(reminderTime = newTime))
                    }
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        const val ACTION_DONE = "com.zai.planreminder.action.DONE"
        const val ACTION_SNOOZE = "com.zai.planreminder.action.SNOOZE"
    }
}
