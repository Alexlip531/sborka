package com.zai.sugar.ui.calendar

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.zai.sugar.databinding.ItemCalendarDayBinding
import com.zai.sugar.util.DateUtils
import java.util.Calendar

class CalendarAdapter(
    private var days: List<Long> = emptyList(),
    private var daysWithRecords: Set<Long> = emptySet(),
    private var monthReference: Long = 0L,
    private var selectedDate: Long = 0L,
    private val onSelect: (Long) -> Unit,
) : RecyclerView.Adapter<CalendarAdapter.VH>() {

    inner class VH(val binding: ItemCalendarDayBinding) : RecyclerView.ViewHolder(binding.root)

    fun updateData(
        days: List<Long>,
        daysWithRecords: Set<Long>,
        monthReference: Long,
        selectedDate: Long,
    ) {
        this.days = days
        this.daysWithRecords = daysWithRecords
        this.monthReference = monthReference
        this.selectedDate = selectedDate
        notifyDataSetChanged()
    }

    fun setSelected(date: Long) {
        val oldPos = days.indexOfFirst { DateUtils.startOfDay(it) == DateUtils.startOfDay(selectedDate) }
        selectedDate = date
        val newPos = days.indexOfFirst { DateUtils.startOfDay(it) == DateUtils.startOfDay(selectedDate) }
        if (oldPos >= 0) notifyItemChanged(oldPos)
        if (newPos >= 0) notifyItemChanged(newPos)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val b = ItemCalendarDayBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(b)
    }

    override fun getItemCount(): Int = days.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val dayMs = days[position]
        val b = holder.binding
        val cal = Calendar.getInstance().apply { timeInMillis = dayMs }
        val dayNum = cal.get(Calendar.DAY_OF_MONTH)

        b.textDay.text = dayNum.toString()

        // Бледный шрифт для соседних месяцев.
        val sameMonth = DateUtils.startOfMonth(dayMs) == DateUtils.startOfMonth(monthReference)
        b.textDay.alpha = if (sameMonth) 1.0f else 0.35f

        val isSelected = DateUtils.startOfDay(dayMs) == DateUtils.startOfDay(selectedDate)
        b.cellContainer.isSelected = isSelected
        b.textDay.setTextColor(
            if (isSelected) Color.WHITE
            else if (sameMonth) 0xFF0F172A.toInt()
            else 0xFF94A3B8.toInt()
        )

        val hasRecord = daysWithRecords.contains(DateUtils.startOfDay(dayMs))
        b.viewMarker.visibility = if (hasRecord && !isSelected) View.VISIBLE else View.GONE

        b.root.setOnClickListener { onSelect(dayMs) }
    }
}
