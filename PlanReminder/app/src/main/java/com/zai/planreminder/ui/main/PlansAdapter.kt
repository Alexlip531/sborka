package com.zai.planreminder.ui.main

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.zai.planreminder.R
import com.zai.planreminder.data.entity.Plan
import com.zai.planreminder.databinding.ItemPlanBinding
import com.zai.planreminder.util.TimeFormatter
import java.util.Calendar

class PlansAdapter(
    private val onDoneClick: (Plan) -> Unit,
    private val onEditClick: (Plan) -> Unit,
) : ListAdapter<Plan, PlansAdapter.PlanVH>(DIFF) {

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<Plan>() {
            override fun areItemsTheSame(o: Plan, n: Plan) = o.id == n.id
            override fun areContentsTheSame(o: Plan, n: Plan) = o == n
        }
        val PALETTE = intArrayOf(
            0xFF10B981.toInt(), 0xFF0F766E.toInt(), 0xFFF59E0B.toInt(),
            0xFFEF4444.toInt(), 0xFF3B82F6.toInt(), 0xFF8B5CF6.toInt(),
            0xFFEC4899.toInt(), 0xFF14B8A6.toInt(),
        )
    }

    inner class PlanVH(val binding: ItemPlanBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlanVH {
        val binding = ItemPlanBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PlanVH(binding)
    }

    override fun onBindViewHolder(holder: PlanVH, position: Int) {
        val plan = getItem(position)
        val b = holder.binding

        b.textTitle.text = plan.title
        if (plan.description.isNotBlank()) {
            b.textDescription.visibility = View.VISIBLE
            b.textDescription.text = plan.description
        } else {
            b.textDescription.visibility = View.GONE
        }

        // Цвет метки.
        val color = PALETTE[plan.colorIndex % PALETTE.size]
        b.viewColorBar.setBackgroundColor(color)

        if (plan.isDone) {
            // Выполнено.
            b.textTitle.alpha = 0.55f
            b.textTitle.paint.isStrikeThruText = true
            b.iconTime.visibility = View.GONE
            b.textTime.text = if (plan.completedAt > 0)
                TimeFormatter.formatDoneAt(holder.itemView.context, plan.completedAt)
            else "Готово"
            b.chipStatus.text = "✓"
            b.chipStatus.setTextColor(color)
            b.btnDone.setImageResource(R.drawable.ic_check)
        } else {
            b.textTitle.alpha = 1f
            b.textTitle.paint.isStrikeThruText = false
            b.iconTime.visibility = View.VISIBLE
            if (plan.reminderTime > 0) {
                val ctx = holder.itemView.context
                b.textTime.text = TimeFormatter.formatDateTime(ctx, plan.reminderTime)
                val statusText = TimeFormatter.formatRelativeStatus(ctx, plan.reminderTime)
                b.chipStatus.text = statusText
                b.chipStatus.setTextColor(
                    if (plan.reminderTime <= System.currentTimeMillis())
                        Color.parseColor("#EF4444")
                    else color
                )
            } else {
                b.textTime.text = "Без напоминания"
                b.chipStatus.text = ""
            }
            b.btnDone.setImageResource(R.drawable.ic_check)
        }

        b.btnDone.setOnClickListener { onDoneClick(plan) }
        b.btnEdit.setOnClickListener { onEditClick(plan) }

        // Тоже открывать редактирование по клику на карточку.
        b.cardPlan.setOnClickListener { onEditClick(plan) }
    }
}
