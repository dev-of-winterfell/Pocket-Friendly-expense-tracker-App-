package com.example.pocketmaster.ui.dashboard

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.pocketmaster.R
import com.example.pocketmaster.data.model.DashboardState
import com.example.pocketmaster.data.model.ExpenseCategoryData
import com.example.pocketmaster.databinding.FragmentDashboardBinding
import com.example.pocketmaster.ui.calander.MonthYearPickerDialog
import com.example.pocketmaster.ui.viewmodel.FinanceViewModel
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

class DashboardFragment : Fragment() {
    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private val financeViewModel: FinanceViewModel by viewModels({ requireActivity() })
    private val dashboardViewModel: DashboardViewModel by viewModels()
    private val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.getDefault())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupPieChart()
       setupMonthSelector()
        observeViewModels()
        showLoading(true)
    }

    private fun setupMonthSelector() {
        binding.btnTimeRange.setOnClickListener {
            android.util.Log.d("DashboardFragment", "Time range button clicked")
            showMonthPicker()
        }
    }
    private fun showMonthPicker() {
        MonthYearPickerDialog().apply {
            setOnDateSelectedListener { year, month ->
                android.util.Log.d("DashboardFragment", "Month selected: $year-$month")
                dashboardViewModel.setSelectedMonth(year, month)
                updateTimeRangeButton()
            }
            setOnResetListener {
                android.util.Log.d("DashboardFragment", "Resetting to current month")
                dashboardViewModel.resetDateFilter()
                updateTimeRangeButton()
            }
        }.show(childFragmentManager, MonthYearPickerDialog.TAG)
    }
    private fun updateTimeRangeButton() {
        binding.btnTimeRange.text = dashboardViewModel.getFormattedSelectedDate()
    }
    private fun setupPieChart() {
        binding.pieChart.apply {
            description.isEnabled = false  // Disable description label
            isDrawHoleEnabled = true
            setHoleColor(Color.TRANSPARENT)

            // Customize the center hole
            holeRadius = 58f
            transparentCircleRadius = 61f
            setTransparentCircleColor(Color.WHITE)
            setTransparentCircleAlpha(110)

            // Customize the chart
            setUsePercentValues(true)
            setEntryLabelColor(Color.WHITE)
            setEntryLabelTextSize(12f)
            setDrawEntryLabels(false)  // Disable labels on slices for cleaner look

            // Customize the legend
            legend.apply {
                isEnabled = true
                orientation = Legend.LegendOrientation.VERTICAL
                horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
                verticalAlignment = Legend.LegendVerticalAlignment.CENTER
                setDrawInside(false)
                xEntrySpace = 10f
                yEntrySpace = 0f
                yOffset = 0f
                textSize = 12f
                formSize = 16f
            }

            // Add animation
            animateY(1400, Easing.EaseInOutQuad)
        }
    }

    private fun observeViewModels() {
        // Observe loading state first
        viewLifecycleOwner.lifecycleScope.launch {
            dashboardViewModel.isLoading.collect { isLoading ->
                android.util.Log.d("DashboardFragment", "Loading state changed: $isLoading")
                showLoading(isLoading)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            financeViewModel.dashboardState.collect { state ->
                android.util.Log.d("DashboardFragment",
                    "Dashboard state update: balance=${state.balance}, income=${state.totalIncome}, expense=${state.totalExpense}")
                binding.apply {
                    tvBalance.text = currencyFormatter.format(state.balance)
                    tvTotalIncome.text = currencyFormatter.format(state.totalIncome)
                    tvTotalExpenses.text = currencyFormatter.format(state.totalExpense)
                }
                // No need to refresh data here as it will trigger loading again
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            dashboardViewModel.pieChartState.collect { categories ->
                android.util.Log.d("DashboardFragment", "Pie chart update: ${categories.size} categories")
                if (categories.isNotEmpty()) {
                    binding.apply {
                        emptyStateText.visibility = View.GONE
                        updatePieChart(categories)
                    }
                } else {
                    binding.apply {
                        pieChart.visibility = View.GONE
                        emptyStateText.visibility = View.VISIBLE
                        emptyStateText.text = "No transactions found for ${dashboardViewModel.getFormattedSelectedDate()}"
                    }
                }
            }
        }

        updateTimeRangeButton()
    }
    private fun showLoading(show: Boolean) {
        binding.apply {
            progressBar.visibility = if (show) View.VISIBLE else View.GONE
            pieChart.visibility = if (show) View.GONE else View.VISIBLE
            emptyStateText.visibility = View.GONE
        }
    }

    private fun updatePieChart(categories: List<ExpenseCategoryData>) {
        binding.pieChart.visibility = View.VISIBLE

        val entries = categories.map { category ->
            val displayName = if (category.category.length > 15) {
                "${category.category.take(12)}..."
            } else {
                category.category
            }
            PieEntry(category.amount.toFloat(), displayName).also {
                android.util.Log.d("DashboardFragment",
                    "Adding entry: category=${category.category}, amount=${category.amount}")
            }
        }
        val colors = listOf(
            Color.rgb(64, 89, 128),
            Color.rgb(149, 165, 124),
            Color.rgb(217, 184, 162),
            Color.rgb(191, 134, 134),
            Color.rgb(179, 48, 80),
            Color.rgb(193, 37, 82),
            Color.rgb(255, 102, 0),
            Color.rgb(245, 199, 0)
        )

        val dataSet = PieDataSet(entries, "").apply {
            setColors(colors)
            valueFormatter = PercentFormatter(binding.pieChart)
            valueTextSize = 11f
            valueTextColor = Color.WHITE
            yValuePosition = PieDataSet.ValuePosition.INSIDE_SLICE
            sliceSpace = 2f  // Add space between slices
            selectionShift = 5f  // How much slices separate from center when selected
        }

        val pieData = PieData(dataSet).apply {
            setValueFormatter(PercentFormatter())
            setValueTextSize(11f)
            setValueTextColor(Color.WHITE)
        }
        binding.pieChart.apply {
            data = pieData
            highlightValues(null)

            // Ensure the chart is visible
            visibility = View.VISIBLE

            // Force a layout pass
            requestLayout()

            // Refresh the chart
            invalidate()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}