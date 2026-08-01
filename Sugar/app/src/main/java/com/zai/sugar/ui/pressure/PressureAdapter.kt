package com.zai.sugar.ui.pressure

import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.zai.sugar.data.entity.PressureMeasurement
import com.zai.sugar.databinding.ItemPressureBinding
import com.zai.sugar.medical.PressureEvaluator
import com.zai.sugar.util.DateUtils

class PressureAdapter(
    private val onDeleteClick: (PressureMeasurement) -> Unit,
) : ListAdapter<PressureMeasurement, PressureAdapter.VH>(DIFF) {

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<PressureMeasurement>() {
            override fun areItemsTheSame(o: PressureMeasurement, n: PressureMeasurement) = o.id == n.id
            override fun areContentsTheSame(o: PressureMeasurement, n: PressureMeasurement) = o == n
        }
    }

    inner class VH(val binding: ItemPressureBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val b = ItemPressureBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(b)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = getItem(position)
        val b = holder.binding
        val ctx = b.root.context

        b.textSystolic.text = item.systolic.toString()
        b.textDiastolic.text = item.diastolic.toString()
        b.textPulse.text = if (item.pulse > 0) "${item.pulse} уд/мин" else "—"

        b.textArm.text = item.arm + " рука"

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

        // Status.
        val result = PressureEvaluator.evaluate(item.systolic, item.diastolic)
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

    private fun keepAlpha(color: Int): Int {
        val r = (color shr 16) and 0xFF
        val g = (color shr 8) and 0xFF
        val bl = color and 0xFF
        return (0x33 shl 24) or (r shl 16) or (g shl 8) or bl
    }
}
