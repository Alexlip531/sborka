package com.zai.sugar.ui.stats

import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.zai.sugar.R
import com.zai.sugar.data.repository.Repository
import com.zai.sugar.databinding.FragmentStatsBinding
import com.zai.sugar.medical.PressureEvaluator
import com.zai.sugar.medical.SugarEvaluator
import com.zai.sugar.ui.main.MainActivity
import kotlinx.coroutines.launch

class StatsFragment : Fragment() {

    private var _binding: FragmentStatsBinding? = null
    private val binding get() = _binding!!

    private val repo: Repository? get() = (activity as? MainActivity)?.repository

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadStats()
    }

    private fun loadStats() {
        viewLifecycleOwner.lifecycleScope.launch {
            val r = repo ?: return@launch

            val sugarAll = r.getRecentSugar(Int.MAX_VALUE)
            val pressureAll = r.getRecentPressure(Int.MAX_VALUE)

            if (sugarAll.isEmpty() && pressureAll.isEmpty()) {
                binding.textNoData.visibility = View.VISIBLE
                return@launch
            }
            binding.textNoData.visibility = View.GONE

            // Sugar.
            binding.textSugarTotal.text = sugarAll.size.toString()
            binding.textSugarAvgBefore.text = r.avgSugarBefore()?.let {
                String.format("%.1f", it)
            } ?: "—"
            binding.textSugarAvgAfter.text = r.avgSugarAfter()?.let {
                String.format("%.1f", it)
            } ?: "—"

            // Sugar distribution.
            val lowCount = sugarAll.count {
                SugarEvaluator.evaluate(it.value, it.beforeMeal).status == SugarEvaluator.Status.LOW
            }
            val normalCount = sugarAll.count {
                SugarEvaluator.evaluate(it.value, it.beforeMeal).status == SugarEvaluator.Status.NORMAL
            }
            val elevatedCount = sugarAll.count {
                SugarEvaluator.evaluate(it.value, it.beforeMeal).status == SugarEvaluator.Status.ELEVATED
            }
            val highCount = sugarAll.count {
                SugarEvaluator.evaluate(it.value, it.beforeMeal).status == SugarEvaluator.Status.HIGH
            }
            binding.textCountLow.text = lowCount.toString()
            binding.textCountNormal.text = normalCount.toString()
            binding.textCountElevated.text = elevatedCount.toString()
            binding.textCountHigh.text = highCount.toString()

            renderDistributionBar(
                binding.barSugarDistribution,
                listOf(
                    lowCount to SugarEvaluator.COLOR_LOW,
                    normalCount to SugarEvaluator.COLOR_NORMAL,
                    elevatedCount to SugarEvaluator.COLOR_ELEVATED,
                    highCount to SugarEvaluator.COLOR_HIGH,
                )
            )

            // Pressure.
            binding.textPressureTotal.text = pressureAll.size.toString()
            binding.textAvgSystolic.text = r.avgSystolic()?.toString() ?: "—"
            binding.textAvgDiastolic.text = r.avgDiastolic()?.toString() ?: "—"
            binding.textAvgPulse.text = r.avgPulse()?.toString() ?: "—"

            val hypoCount = pressureAll.count {
                PressureEvaluator.evaluate(it.systolic, it.diastolic).status == PressureEvaluator.Status.HYPOTENSION
            }
            val normalPCount = pressureAll.count {
                val s = PressureEvaluator.evaluate(it.systolic, it.diastolic).status
                s == PressureEvaluator.Status.OPTIMAL || s == PressureEvaluator.Status.NORMAL
            }
            val elevatedPCount = pressureAll.count {
                val s = PressureEvaluator.evaluate(it.systolic, it.diastolic).status
                s == PressureEvaluator.Status.HIGH_NORMAL || s == PressureEvaluator.Status.HYPERTENSION_1
            }
            val hypertensionCount = pressureAll.count {
                val s = PressureEvaluator.evaluate(it.systolic, it.diastolic).status
                s == PressureEvaluator.Status.HYPERTENSION_2 || s == PressureEvaluator.Status.HYPERTENSION_3
            }
            binding.textCountHypotension.text = hypoCount.toString()
            binding.textCountNormalPressure.text = normalPCount.toString()
            binding.textCountElevatedPressure.text = elevatedPCount.toString()
            binding.textCountHypertension.text = hypertensionCount.toString()

            renderDistributionBar(
                binding.barPressureDistribution,
                listOf(
                    hypoCount to PressureEvaluator.COLOR_LOW,
                    normalPCount to PressureEvaluator.COLOR_NORMAL,
                    elevatedPCount to PressureEvaluator.COLOR_ELEVATED,
                    hypertensionCount to PressureEvaluator.COLOR_HIGH,
                )
            )
        }
    }

    private fun renderDistributionBar(bar: LinearLayout, segments: List<Pair<Int, Int>>) {
        val total = segments.sumOf { it.first }
        if (total == 0) {
            bar.visibility = View.GONE
            return
        }
        bar.visibility = View.VISIBLE
        bar.removeAllViews()
        segments.forEach { (count, color) ->
            if (count == 0) return@forEach
            val weight = count.toFloat() / total
            val view = View(bar.context)
            val lp = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, weight)
            view.layoutParams = lp
            view.background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                setColor(color)
            }
            bar.addView(view)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
