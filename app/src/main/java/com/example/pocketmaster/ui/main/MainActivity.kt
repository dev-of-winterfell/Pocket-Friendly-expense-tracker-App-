package com.example.pocketmaster.ui.main

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.example.pocketmaster.R
import com.example.pocketmaster.databinding.ActivityMainBinding
import com.example.pocketmaster.ui.dialogs.AddTransactionDialog
import com.example.pocketmaster.ui.viewmodel.FinanceViewModel
import com.google.android.material.tabs.TabLayoutMediator

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: FinanceViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[FinanceViewModel::class.java]

        setupViewPager()
        setupFab()
    }

    private fun setupViewPager() {
        val adapter = FinancePagerAdapter(this)
        binding.viewPager.adapter = adapter

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when(position) {
                0 -> getString(R.string.tab_dashboard)
                1 -> getString(R.string.tab_transactions)
                else -> getString(R.string.tab_categories)
            }
        }.attach()
    }

    private fun setupFab() {
        binding.fabAddTransaction.setOnClickListener {
            showAddTransactionDialog()
        }
    }

    private fun showAddTransactionDialog() {
        AddTransactionDialog().show(supportFragmentManager, "AddTransaction")
    }
}
