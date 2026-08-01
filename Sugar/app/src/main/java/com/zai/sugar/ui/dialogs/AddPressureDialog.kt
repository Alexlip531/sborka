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
import com.zai.sugar.data.entity.PressureMeasurement
import com.zai.sugar.data.repository.Repository
import com.zai.sugar.databinding.DialogAddPressureBinding
import com.zai.sugar.medical.PressureEvaluator
import com.zai.sugar.ui.main.MainActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class AddPressureDialog : BottomSheetDialogFragment() {

    private var _binding: DialogAddPressureBinding? = null
    private val binding get() = _binding!!

    private val repo: Repository?
        get() = (activity as? MainActivity)?.repository

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = DialogAddPressureBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toggleArm.check(R.id.btn_arm_left)

        binding.btnCancel.setOnClickListener { dismiss() }
        binding.btnSave.setOnClickListener { save() }
    }

    private fun save() {
        val sysStr = binding.editSystolic.text?.toString()?.trim().orEmpty()
        val diaStr = binding.editDiastolic.text?.toString()?.trim().orEmpty()
        val pulseStr = binding.editPulse.text?.toString()?.trim().orEmpty()

        if (sysStr.isEmpty() || diaStr.isEmpty()) {
            Snackbar.make(binding.root, R.string.err_pressure_required, Snackbar.LENGTH_SHORT).show()
            return
        }
        val sys = sysStr.toIntOrNull()
        val dia = diaStr.toIntOrNull()
        if (sys == null || dia == null || sys < 50 || sys > 250 || dia < 30 || dia > 150) {
            Snackbar.make(binding.root, R.string.err_pressure_range, Snackbar.LENGTH_SHORT).show()
            return
        }
        var pulse = 0
        if (pulseStr.isNotEmpty()) {
            pulse = pulseStr.toIntOrNull() ?: 0
            if (pulse < 30 || pulse > 220) {
                Snackbar.make(binding.root, R.string.err_pulse_range, Snackbar.LENGTH_SHORT).show()
                return
            }
        }

        val arm = if (binding.toggleArm.checkedButtonId == R.id.btn_arm_left)
            getString(R.string.pressure_arm_left)
        else
            getString(R.string.pressure_arm_right)

        val note = binding.editNote.text?.toString()?.trim().orEmpty()

        val item = PressureMeasurement(
            systolic = sys,
            diastolic = dia,
            pulse = pulse,
            arm = arm,
            note = note,
        )

        viewLifecycleOwner.lifecycleScope.launch {
            repo?.insertPressure(item)
            val result = PressureEvaluator.evaluate(sys, dia)
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
        fun newInstance(): AddPressureDialog = AddPressureDialog()
    }
}
