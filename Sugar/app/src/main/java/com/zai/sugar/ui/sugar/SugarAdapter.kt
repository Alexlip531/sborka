package com.zai.sugar.ui.sugar

import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.zai.sugar.data.entity.SugarMeasurement
import com.zai.sugar.databinding.ItemSugarBinding
import com.zai.sugar.medical.SugarEvaluator
import com.zai.sugar.util.DateUtils

class SugarAdapter(
    private val onDeleteClick: (SugarMeasurement) -> Unit,
) : ListAdapter<SugarMeasurement, SugarAdapter.VH>(DIFF) {

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<SugarMeasurement>() {
            override fun areItemsTheSame(o: SugarMeasurement, n: SugarMeasurement) = o.id == n.id
            override fun areContentsTheSame(o: SugarMeasurement, n: SugarMeasurement) = o == n
        }
    }

    inner class VH(val binding: ItemSugarBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val b = ItemSugarBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(b)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = getItem(position)
        val b = holder.binding
        val ctx = b.root.context

        b.textValue.text = String.format("%.1f", item.value)

        b.textMealType.text = if (item.beforeMeal)
            ctx.getString(com.zai.sugar.R.string.sugar_before_meal)
        else
            ctx.getString(com.zai.sugar.R.string.sugar_after_meal)

        // Time label.
        val now = System.currentTimeMillis()
        b.textTime.text = if (DateUtils.isSameDay(item.measuredAt, now)) {
            "Сегодня, ${DateUtils.formatTime(item.measuredAt)}"
        } else {
            DateUtils.formatDateTime(item.measuredAt)
        }

        if (item.note.isNotBlank()) {
            b.textNote.visibility = android.view.View.VISIBLE
            b.textNote.text = item.note
        } else {
            b.textNote.visibility = android.view.View.GONE
        }

        // Status badge.
        val result = SugarEvaluator.evaluate(item.value, item.beforeMeal)
        b.textStatus.text = result.label
        val bg = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 14f * ctx.resources.displayMetrics.density
            setColor(keepAlpha(result.color))
        }
        b.textStatus.background = bg
        b.textStatus.setTextColor(result.color)

        b.btnDelete.setOnClickListener { onDeleteClick(item) }
    }

    /** Add alpha 0x33 (20%) for background tint while keeping text color bright. */
    private fun keepAlpha(color: Int): Int {
        val r = (color shr 16) and 0xFF
        val g = (color shr 8) and 0xFF
        val bl = color and 0xFF
        return (0x33 shl 24) or (r shl 16) or (g shl 8) or bl
    }
}
