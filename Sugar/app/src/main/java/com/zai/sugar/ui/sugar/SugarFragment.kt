package com.zai.sugar.ui.sugar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.zai.sugar.data.entity.SugarMeasurement
import com.zai.sugar.data.repository.Repository
import com.zai.sugar.databinding.FragmentSugarBinding
import com.zai.sugar.ui.dialogs.AddSugarDialog
import com.zai.sugar.ui.main.MainActivity
import com.zai.sugar.util.DateUtils
import kotlinx.coroutines.launch

class SugarFragment : Fragment() {

    private var _binding: FragmentSugarBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: SugarAdapter
    private val activity: MainActivity? get() = getActivity() as? MainActivity
    private val repo: Repository? get() = activity?.repository

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSugarBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = SugarAdapter { item -> confirmDelete(item) }
        binding.recycler.layoutManager = LinearLayoutManager(requireContext())
        binding.recycler.adapter = adapter

        binding.fabAdd.setOnClickListener {
            AddSugarDialog.newInstance().show(parentFragmentManager, "add_sugar")
        }

        binding.btnEmptyAdd.setOnClickListener {
            AddSugarDialog.newInstance().show(parentFragmentManager, "add_sugar")
        }

        observeData()
    }

    private fun observeData() {
        val r = repo ?: return
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                r.observeAllSugar().collect { items ->
                    adapter.submitList(items)
                    binding.emptyView.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
                }
            }
        }
    }

    private fun confirmDelete(item: SugarMeasurement) {
        com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
            .setTitle("Удалить запись?")
            .setMessage("Измерение ${DateUtils.formatDateTime(item.measuredAt)} будет удалено безвозвратно.")
            .setNegativeButton("Отмена", null)
            .setPositiveButton("Удалить") { _, _ ->
                viewLifecycleOwner.lifecycleScope.launch {
                    repo?.deleteSugar(item)
                }
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
