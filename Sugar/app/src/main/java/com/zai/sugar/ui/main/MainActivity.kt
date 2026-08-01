package com.zai.sugar.ui.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.zai.sugar.R
import com.zai.sugar.data.repository.AppDatabase
import com.zai.sugar.data.repository.Repository
import com.zai.sugar.databinding.ActivityMainBinding
import com.zai.sugar.ui.calendar.CalendarFragment
import com.zai.sugar.ui.pressure.PressureFragment
import com.zai.sugar.ui.stats.StatsFragment
import com.zai.sugar.ui.sugar.SugarFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    lateinit var repository: Repository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        repository = Repository(
            AppDatabase.get(this).sugarDao(),
            AppDatabase.get(this).pressureDao()
        )

        setSupportActionBar(binding.toolbar)
        binding.toolbar.title = getString(R.string.app_name)

        binding.bottomNav.setOnItemSelectedListener { item ->
            val frag: Fragment = when (item.itemId) {
                R.id.nav_sugar -> SugarFragment()
                R.id.nav_pressure -> PressureFragment()
                R.id.nav_calendar -> CalendarFragment()
                R.id.nav_stats -> StatsFragment()
                else -> SugarFragment()
            }
            supportFragmentManager.beginTransaction()
                .replace(R.id.nav_host_container, frag)
                .commitAllowingStateLoss()
            true
        }

        if (savedInstanceState == null) {
            binding.bottomNav.selectedItemId = R.id.nav_sugar
        }
    }
}
