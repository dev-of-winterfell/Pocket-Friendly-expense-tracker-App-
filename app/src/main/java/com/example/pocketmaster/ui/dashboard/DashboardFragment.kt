package com.example.pocketmaster.ui.dashboard

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.pocketmaster.data.model.DashboardState
import com.example.pocketmaster.data.model.ExpenseCategoryData
import com.example.pocketmaster.databinding.FragmentDashboardBinding
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

    private val viewModel: DashboardViewModel by viewModels()
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
        observeDashboardState()
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

    private fun observeDashboardState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.dashboardState.collect { state: DashboardState ->
                updateDashboardUI(state)
            }
        }
    }
    private fun updateDashboardUI(state: DashboardState) {
        binding.apply {
            // Update summary amounts
            tvBalance.text = currencyFormatter.format(state.balance)
            tvTotalIncome.text = currencyFormatter.format(state.totalIncome)
            tvTotalExpenses.text = currencyFormatter.format(state.totalExpense)

            // Update pie chart
            updatePieChart(state.expenseCategories)
        }
    }

    private fun updatePieChart(categories: List<ExpenseCategoryData>) {
        if (categories.isEmpty()) return

        val entries = categories.map { category ->
            PieEntry(category.amount.toFloat(), category.category)
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

        val dataSet = PieDataSet(entries, "Expense Categories").apply {
            setColors(colors)
            valueFormatter = PercentFormatter(binding.pieChart)
            valueTextSize = 11f
            valueTextColor = Color.WHITE
            yValuePosition = PieDataSet.ValuePosition.INSIDE_SLICE
        }

        val pieData = PieData(dataSet)
        binding.pieChart.apply {
            data = pieData
            invalidate()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}