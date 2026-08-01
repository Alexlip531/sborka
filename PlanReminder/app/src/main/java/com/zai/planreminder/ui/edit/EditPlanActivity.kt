package com.zai.planreminder.ui.edit

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.zai.planreminder.R
import com.zai.planreminder.data.entity.Plan
import com.zai.planreminder.data.repository.AppDatabase
import com.zai.planreminder.data.repository.PlanRepository
import com.zai.planreminder.databinding.ActivityEditBinding
import com.zai.planreminder.notification.ReminderScheduler
import com.zai.planreminder.util.TimeFormatter
import kotlinx.coroutines.launch
import java.util.Calendar

class EditPlanActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditBinding
    private lateinit var repo: PlanRepository
    private lateinit var scheduler: ReminderScheduler

    private var planId: Long = -1L
    private var existing: Plan? = null

    private var pickedCalendar: Calendar? = null
    private var colorIndex: Int = 0

    private val palette = intArrayOf(
        0xFF10B981.toInt(), 0xFF0F766E.toInt(), 0xFFF59E0B.toInt(),
        0xFFEF4444.toInt(), 0xFF3B82F6.toInt(), 0xFF8B5CF6.toInt(),
        0xFFEC4899.toInt(), 0xFF14B8A6.toInt(),
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        repo = PlanRepository(AppDatabase.get(this).planDao())
        scheduler = ReminderScheduler(this)

        planId = intent.getLongExtra(EXTRA_PLAN_ID, -1L)

        setupToolbar()
        setupColorPicker()

        if (planId > 0) {
            loadPlan()
        } else {
            binding.toolbar.title = getString(R.string.title_new_plan)
        }

        binding.cardDate.setOnClickListener { pickDate() }
        binding.cardTime.setOnClickListener { pickTime() }
        binding.fabSave.setOnClickListener { save() }
        binding.btnClearReminder.setOnClickListener {
            pickedCalendar = null
            updateReminderUI()
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupColorPicker() {
        val container = binding.containerColors
        val size = (48 * resources.displayMetrics.density).toInt()
        val margin = (8 * resources.displayMetrics.density).toInt()
        palette.forEachIndexed { idx, color ->
            val view = View(this).apply {
                layoutParams = LinearLayout.LayoutParams(size, size).apply {
                    setMargins(margin, 0, margin, 0)
                }
                setBackgroundResource(android.R.color.transparent)
                val drawable = android.graphics.drawable.GradientDrawable().apply {
                    shape = android.graphics.drawable.GradientDrawable.OVAL
                    setColor(color)
                }
                background = drawable
                setOnClickListener {
                    colorIndex = idx
                    highlightColor()
                }
            }
            container.addView(view)
        }
    }

    private fun highlightColor() {
        val container = binding.containerColors
        for (i in 0 until container.childCount) {
            val v = container.getChildAt(i)
            val pad = if (i == colorIndex) (6 * resources.displayMetrics.density).toInt() else 0
            v.setPadding(pad, pad, pad, pad)
            v.background = android.graphics.drawable.GradientDrawable().apply {
                shape = android.graphics.drawable.GradientDrawable.OVAL
                setColor(palette[i])
                if (i == colorIndex) {
                    setStroke((3 * resources.displayMetrics.density).toInt(), 0xFFFFFFFF.toInt())
                }
            }
        }
    }

    private fun loadPlan() {
        lifecycleScope.launch {
            existing = repo.getById(planId)
            existing?.let { plan ->
                binding.toolbar.title = getString(R.string.title_edit_plan)
                binding.editTitle.setText(plan.title)
                binding.editDescription.setText(plan.description)
                colorIndex = plan.colorIndex
                highlightColor()
                if (plan.reminderTime > 0) {
                    pickedCalendar = Calendar.getInstance().apply { timeInMillis = plan.reminderTime }
                }
                updateReminderUI()
            }
        }
    }

    private fun pickDate() {
        val cal = pickedCalendar ?: Calendar.getInstance()
        DatePickerDialog(this, { _, y, m, d ->
            ensureCalendar().set(y, m, d)
            pickTime()
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun pickTime() {
        val cal = pickedCalendar ?: Calendar.getInstance()
        TimePickerDialog(this, { _, h, min ->
            ensureCalendar().set(
                ensureCalendar().get(Calendar.YEAR),
                ensureCalendar().get(Calendar.MONTH),
                ensureCalendar().get(Calendar.DAY_OF_MONTH),
                h, min, 0
            )
            updateReminderUI()
        }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
    }

    private fun ensureCalendar(): Calendar {
        if (pickedCalendar == null) {
            pickedCalendar = Calendar.getInstance()
        }
        return pickedCalendar!!
    }

    private fun updateReminderUI() {
        val cal = pickedCalendar
        if (cal == null) {
            binding.textDate.text = "Не выбрано"
            binding.textTime.text = "Не выбрано"
            binding.textSummary.visibility = View.GONE
            binding.btnClearReminder.visibility = View.GONE
        } else {
            binding.textDate.text = TimeFormatter.formatDate(cal.timeInMillis)
            binding.textTime.text = TimeFormatter.formatTime(cal.timeInMillis)
            binding.textSummary.visibility = View.VISIBLE
            binding.textSummary.text = TimeFormatter.formatRelativeStatus(this, cal.timeInMillis)
                .replaceFirstChar { it.uppercase() }
            binding.btnClearReminder.visibility = View.VISIBLE
        }
    }

    private fun save() {
        val title = binding.editTitle.text?.toString()?.trim().orEmpty()
        if (title.isEmpty()) {
            Snackbar.make(binding.root, R.string.error_title_required, Snackbar.LENGTH_SHORT).show()
            return
        }

        val reminderMs = pickedCalendar?.timeInMillis ?: 0L
        if (reminderMs > 0 && reminderMs <= System.currentTimeMillis()) {
            Snackbar.make(binding.root, R.string.error_past_time, Snackbar.LENGTH_SHORT).show()
            return
        }

        val description = binding.editDescription.text?.toString()?.trim().orEmpty()

        lifecycleScope.launch {
            val plan = if (existing == null) {
                Plan(
                    title = title,
                    description = description,
                    reminderTime = reminderMs,
                    colorIndex = colorIndex,
                )
            } else {
                existing!!.copy(
                    title = title,
                    description = description,
                    reminderTime = reminderMs,
                    colorIndex = colorIndex,
                )
            }
            val id = repo.insert(plan)
            // Запланировать напоминание.
            val saved = plan.copy(id = if (plan.id == 0L) id else plan.id)
            if (reminderMs > 0 && !saved.isDone) {
                scheduler.schedule(saved)
            } else {
                scheduler.cancel(saved.id)
            }
            Snackbar.make(binding.root, R.string.msg_saved, Snackbar.LENGTH_SHORT).show()
            finish()
        }
    }

    companion object {
        private const val EXTRA_PLAN_ID = "extra_plan_id"

        fun newIntent(context: Context, planId: Long): Intent =
            Intent(context, EditPlanActivity::class.java).apply {
                putExtra(EXTRA_PLAN_ID, planId)
            }
    }
}
