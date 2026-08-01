package com.zai.sugar.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar
import com.zai.sugar.R
import com.zai.sugar.data.entity.SugarMeasurement
import com.zai.sugar.data.repository.Repository
import com.zai.sugar.databinding.DialogAddSugarBinding
import com.zai.sugar.ui.main.MainActivity
import com.zai.sugar.medical.SugarEvaluator
import kotlinx.coroutines.launch
import androidx.lifecycle.lifecycleScope

class AddSugarDialog : BottomSheetDialogFragment() {

    private var _binding: DialogAddSugarBinding? = null
    private val binding get() = _binding!!

    private val repo: Repository?
        get() = (activity as? MainActivity)?.repository

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = DialogAddSugarBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toggleMeal.check(R.id.btn_before_meal)

        binding.btnCancel.setOnClickListener { dismiss() }
        binding.btnSave.setOnClickListener { save() }
    }

    private fun save() {
        val valueStr = binding.editValue.text?.toString()?.trim().orEmpty()
        if (valueStr.isEmpty()) {
            Snackbar.make(binding.root, R.string.err_sugar_required, Snackbar.LENGTH_SHORT).show()
            return
        }
        val value = valueStr.replace(',', '.').toFloatOrNull()
        if (value == null || value < 1f || value > 35f) {
            Snackbar.make(binding.root, R.string.err_sugar_range, Snackbar.LENGTH_SHORT).show()
            return
        }

        val beforeMeal = binding.toggleMeal.checkedButtonId == R.id.btn_before_meal
        val note = binding.editNote.text?.toString()?.trim().orEmpty()

        val measurement = SugarMeasurement(
            value = value,
            beforeMeal = beforeMeal,
            note = note,
        )

        viewLifecycleOwner.lifecycleScope.launch {
            repo?.insertSugar(measurement)
            // Show evaluation result before dismissing.
            val result = SugarEvaluator.evaluate(value, beforeMeal)
            com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                .setTitle(result.label)
                .setMessage(result.advice)
                .setPositiveButton(R.string.dialog_ok) { d, _ ->
                    d.dismiss()
                    dismiss()
                }
                .show()
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        dialog.setOnShowListener {
            val bottomSheet = dialog.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.let {
                val behavior = BottomSheetBehavior.from(it)
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
                behavior.skipCollapsed = true
            }
        }
        return dialog
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(): AddSugarDialog = AddSugarDialog()
    }
}
