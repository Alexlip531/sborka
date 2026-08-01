package com.zai.planreminder.ui.main

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.zai.planreminder.R
import com.zai.planreminder.data.repository.AppDatabase
import com.zai.planreminder.data.repository.PlanRepository
import com.zai.planreminder.databinding.ActivityMainBinding
import com.zai.planreminder.notification.NotificationHelper
import com.zai.planreminder.notification.ReminderScheduler
import com.zai.planreminder.ui.edit.EditPlanActivity
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var repo: PlanRepository
    private lateinit var scheduler: ReminderScheduler
    private lateinit var adapter: PlansAdapter

    private var selectedTab = 0

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (!granted) {
            Snackbar.make(binding.root, R.string.msg_permission_required, Snackbar.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        NotificationHelper.ensureChannel(this)
        repo = PlanRepository(AppDatabase.get(this).planDao())
        scheduler = ReminderScheduler(this)

        // Просим разрешение на уведомления (Android 13+).
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        setupRecycler()
        setupTabs()
        setupFab()

        observePlans()

        // Если пришли из уведомления — можно сразу открыть редактирование.
        intent?.getLongExtra("extra_plan_id", -1L)?.let { id ->
            if (id > 0) {
                startActivity(EditPlanActivity.newIntent(this, id))
            }
        }
    }

    private fun setupRecycler() {
        adapter = PlansAdapter(
            onDoneClick = { plan ->
                lifecycleScope.launch {
                    val newDone = !plan.isDone
                    repo.setDone(plan.id, newDone)
                    if (newDone) {
                        scheduler.cancel(plan.id)
                    } else if (plan.reminderTime > System.currentTimeMillis()) {
                        scheduler.schedule(plan)
                    }
                }
            },
            onEditClick = { plan ->
                startActivity(EditPlanActivity.newIntent(this, plan.id))
            }
        )
        binding.recyclerPlans.layoutManager = LinearLayoutManager(this)
        binding.recyclerPlans.adapter = adapter
    }

    private fun setupTabs() {
        binding.tabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                selectedTab = tab.position
                observePlans()
            }
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }

    private fun setupFab() {
        binding.fabAdd.setOnClickListener {
            startActivity(EditPlanActivity.newIntent(this, -1L))
        }
    }

    private fun observePlans() {
        val flow = when (selectedTab) {
            1 -> repo.observeActive()
            2 -> repo.observeDone()
            else -> repo.observeAll()
        }
        lifecycleScope.launch {
            flow.collectLatest { plans ->
                adapter.submitList(plans)
                binding.emptyView.visibility =
                    if (plans.isEmpty()) View.VISIBLE else View.GONE
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Перепроверяем, что точные будильники разрешены (Android 12+).
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val am = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (!am.canScheduleExactAlarms()) {
                // Тихо игнорируем — fallback в ReminderScheduler.
            }
        }
    }
}
