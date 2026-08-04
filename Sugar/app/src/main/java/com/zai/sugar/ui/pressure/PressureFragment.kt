package com.zai.sugar.ui.pressure

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.zai.sugar.data.entity.PressureMeasurement
import com.zai.sugar.data.repository.Repository
import com.zai.sugar.databinding.FragmentPressureBinding
import com.zai.sugar.ui.dialogs.AddPressureDialog
import com.zai.sugar.ui.main.MainActivity
import com.zai.sugar.util.DateUtils
import kotlinx.coroutines.launch

class PressureFragment : Fragment() {

    private var _binding: FragmentPressureBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: PressureAdapter
    private val activity: MainActivity? get() = getActivity() as? MainActivity
    private val repo: Repository? get() = activity?.repository

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPressureBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = PressureAdapter { item -> confirmDelete(item) }
        binding.recycler.layoutManager = LinearLayoutManager(requireContext())
        binding.recycler.adapter = adapter

        binding.fabAdd.setOnClickListener {
            AddPressureDialog.newInstance().show(parentFragmentManager, "add_pressure")
        }

        binding.btnEmptyAdd.setOnClickListener {
            AddPressureDialog.newInstance().show(parentFragmentManager, "add_pressure")
        }

        observeData()
    }

    private fun observeData() {
        val r = repo ?: return
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                r.observeAllPressure().collect { items ->
                    adapter.submitList(items)
                    binding.emptyView.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
                }
            }
        }
    }

    private fun confirmDelete(item: PressureMeasurement) {
        com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
            .setTitle("Удалить запись?")
            .setMessage("Измерение ${DateUtils.formatDateTime(item.measuredAt)} будет удалено безвозвратно.")
            .setNegativeButton("Отмена", null)
            .setPositiveButton("Удалить") { _, _ ->
                viewLifecycleOwner.lifecycleScope.launch {
                    repo?.deletePressure(item)
                }
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
