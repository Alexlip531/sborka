package com.zai.sugar.ui.calendar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.card.MaterialCardView
import com.zai.sugar.R
import com.zai.sugar.data.entity.PressureMeasurement
import com.zai.sugar.data.entity.SugarMeasurement
import com.zai.sugar.data.repository.Repository
import com.zai.sugar.databinding.FragmentCalendarBinding
import com.zai.sugar.databinding.ItemRecordSmallBinding
import com.zai.sugar.medical.PressureEvaluator
import com.zai.sugar.medical.SugarEvaluator
import com.zai.sugar.ui.main.MainActivity
import com.zai.sugar.util.DateUtils
import kotlinx.coroutines.launch
import java.util.Calendar

class CalendarFragment : Fragment() {

    private var _binding: FragmentCalendarBinding? = null
    private val binding get() = _binding!!

    private val repo: Repository? get() = (activity as? MainActivity)?.repository

    private var displayedMonth: Long = DateUtils.startOfMonth(System.currentTimeMillis())
    private var selectedDate: Long = DateUtils.startOfDay(System.currentTimeMillis())

    // Cache: map of day-start-ms → has records
    private val daysWithRecords = mutableSetOf<Long>()
    private var sugarByDay: Map<Long, List<SugarMeasurement>> = emptyMap()
    private var pressureByDay: Map<Long, List<PressureMeasurement>> = emptyMap()

    private lateinit var calendarAdapter: CalendarAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCalendarBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        calendarAdapter = CalendarAdapter(
            daysWithRecords = daysWithRecords,
            selectedDate = selectedDate,
            onSelect = { dayMs ->
                selectedDate = DateUtils.startOfDay(dayMs)
                calendarAdapter.setSelected(selectedDate)
                renderSelectedDay()
            }
        )
        binding.recyclerCalendar.layoutManager = GridLayoutManager(requireContext(), 7)
        binding.recyclerCalendar.adapter = calendarAdapter

        binding.btnPrevMonth.setOnClickListener {
            val c = Calendar.getInstance(); c.timeInMillis = displayedMonth
            c.add(Calendar.MONTH, -1)
            displayedMonth = c.timeInMillis
            refresh()
        }
        binding.btnNextMonth.setOnClickListener {
            val c = Calendar.getInstance(); c.timeInMillis = displayedMonth
            c.add(Calendar.MONTH, 1)
            displayedMonth = c.timeInMillis
            refresh()
        }

        refresh()
    }

    private fun refresh() {
        binding.textMonthLabel.text = DateUtils.monthLabel(displayedMonth)
        viewLifecycleOwner.lifecycleScope.launch {
            val r = repo ?: return@launch
            // Загрузим все измерения текущего месяца (и соседних дней для сетки).
            val cal = Calendar.getInstance()
            cal.timeInMillis = displayedMonth
            cal.set(Calendar.DAY_OF_MONTH, 1)
            val monthStart = DateUtils.startOfMonth(displayedMonth)
            val monthEnd = monthStart + 35L * 24 * 3600 * 1000  // ~5 недель вперёд

            val sugarList = r.getSugarByDateRange(monthStart, monthEnd)
            val pressureList = r.getPressureByDateRange(monthStart, monthEnd)

            daysWithRecords.clear()
            sugarByDay = sugarList.groupBy { DateUtils.startOfDay(it.measuredAt) }
            pressureByDay = pressureList.groupBy { DateUtils.startOfDay(it.measuredAt) }
            daysWithRecords.addAll(sugarByDay.keys)
            daysWithRecords.addAll(pressureByDay.keys)

            calendarAdapter.updateData(
                days = DateUtils.daysInMonth(displayedMonth),
                daysWithRecords = daysWithRecords,
                monthReference = displayedMonth,
                selectedDate = selectedDate
            )
            renderSelectedDay()
        }
    }

    private fun renderSelectedDay() {
        // Header.
        val cal = Calendar.getInstance().apply { timeInMillis = selectedDate }
        val dayOfWeek = when (cal.get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> "Понедельник"
            Calendar.TUESDAY -> "Вторник"
            Calendar.WEDNESDAY -> "Среда"
            Calendar.THURSDAY -> "Четверг"
            Calendar.FRIDAY -> "Пятница"
            Calendar.SATURDAY -> "Суббота"
            Calendar.SUNDAY -> "Воскресенье"
            else -> ""
        }
        binding.textSelectedDate.text = "$dayOfWeek, ${DateUtils.formatDate(selectedDate)}"

        binding.containerRecords.removeAllViews()
        val sugar = sugarByDay[selectedDate] ?: emptyList()
        val pressure = pressureByDay[selectedDate] ?: emptyList()

        if (sugar.isEmpty() && pressure.isEmpty()) {
            binding.textNoRecords.visibility = View.VISIBLE
            return
        }
        binding.textNoRecords.visibility = View.GONE

        // Сахар записи.
        sugar.sortedByDescending { it.measuredAt }.forEach { item ->
            val b = ItemRecordSmallBinding.inflate(layoutInflater, binding.containerRecords, false)
            b.iconType.setImageResource(R.drawable.ic_sugar)
            val result = SugarEvaluator.evaluate(item.value, item.beforeMeal)
            b.textValue.text = String.format("%.1f ммоль/л", item.value)
            b.textMeal.text = if (item.beforeMeal) "до еды" else "после еды"
            b.textTime.text = DateUtils.formatTime(item.measuredAt)
            b.textStatus.text = result.label
            val bg = android.graphics.drawable.GradientDrawable().apply {
                shape = android.graphics.drawable.GradientDrawable.RECTANGLE
                cornerRadius = 14f * resources.displayMetrics.density
                setColor(keepAlpha(result.color))
            }
            b.textStatus.background = bg
            b.textStatus.setTextColor(result.color)
            binding.containerRecords.addView(b.root)
        }

        // Давление записи.
        pressure.sortedByDescending { it.measuredAt }.forEach { item ->
            val b = ItemRecordSmallBinding.inflate(layoutInflater, binding.containerRecords, false)
            b.iconType.setImageResource(R.drawable.ic_pressure)
            val result = PressureEvaluator.evaluate(item.systolic, item.diastolic)
            b.textValue.text = "${item.systolic}/${item.diastolic} мм рт. ст."
            b.textMeal.text = if (item.pulse > 0) "${item.pulse} уд/мин, ${item.arm}" else item.arm
            b.textTime.text = DateUtils.formatTime(item.measuredAt)
            b.textStatus.text = result.label
            val bg = android.graphics.drawable.GradientDrawable().apply {
                shape = android.graphics.drawable.GradientDrawable.RECTANGLE
                cornerRadius = 14f * resources.displayMetrics.density
                setColor(keepAlpha(result.color))
            }
            b.textStatus.background = bg
            b.textStatus.setTextColor(result.color)
            binding.containerRecords.addView(b.root)
        }
    }

    private fun keepAlpha(color: Int): Int {
        val r = (color shr 16) and 0xFF
        val g = (color shr 8) and 0xFF
        val bl = color and 0xFF
        return (0x33 shl 24) or (r shl 16) or (g shl 8) or bl
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
