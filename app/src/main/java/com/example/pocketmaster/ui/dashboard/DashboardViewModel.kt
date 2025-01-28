package com.example.pocketmaster.ui.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.pocketmaster.data.database.AppDatabase
import com.example.pocketmaster.data.model.TransactionType
import com.example.pocketmaster.data.model.CategoryTotal
import com.example.pocketmaster.data.model.DashboardState
import com.example.pocketmaster.data.model.ExpenseCategoryData
import com.example.pocketmaster.data.repository.FinanceRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


class DashboardViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: FinanceRepository = FinanceRepository(
        AppDatabase.getDatabase(application).transactionDao(),
        AppDatabase.getDatabase(application).categoryDao()
    )

    private val _updateTrigger = MutableSharedFlow<Unit>(replay = 1)
    private val _selectedDate = MutableStateFlow<Pair<Int, Int>?>(null)
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val expenseCategoriesFlow = combine(
        repository.getCategoryTotals(TransactionType.EXPENSE),
        _selectedDate,
        _updateTrigger.onStart { emit(Unit) }
    ) { categoryTotals, selectedDate, _ ->
        android.util.Log.d("DashboardViewModel", "Starting data processing")

        try {
            val currentCalendar = Calendar.getInstance()
            val filteredTotals = if (selectedDate != null) {
                val (year, month) = selectedDate
                categoryTotals.filter { categoryTotal ->
                    val calendar = Calendar.getInstance().apply {
                        timeInMillis = categoryTotal.date ?: System.currentTimeMillis()
                    }
                    calendar.get(Calendar.YEAR) == year &&
                            calendar.get(Calendar.MONTH) == month
                }
            } else {
                categoryTotals.filter { categoryTotal ->
                    val calendar = Calendar.getInstance().apply {
                        timeInMillis = categoryTotal.date ?: System.currentTimeMillis()
                    }
                    calendar.get(Calendar.YEAR) == currentCalendar.get(Calendar.YEAR) &&
                            calendar.get(Calendar.MONTH) == currentCalendar.get(Calendar.MONTH)
                }
            }

            android.util.Log.d("DashboardViewModel", "Filtered totals size: ${filteredTotals.size}")

            val totalExpense = filteredTotals.sumOf { it.total }
            filteredTotals.map { categoryTotal ->
                ExpenseCategoryData(
                    category = categoryTotal.name,
                    amount = categoryTotal.total,
                    percentage = if (totalExpense > 0) (categoryTotal.total / totalExpense) * 100 else 0.0
                )
            }
        } finally {
            android.util.Log.d("DashboardViewModel", "Processing complete, setting loading to false")
            _isLoading.value = false
        }
    }

    val pieChartState: StateFlow<List<ExpenseCategoryData>> = expenseCategoriesFlow
        .onStart {
            android.util.Log.d("DashboardViewModel", "Starting pie chart state collection")
            _isLoading.value = true
        }
        .catch { error ->
            android.util.Log.e("DashboardViewModel", "Error in categories flow", error)
            _isLoading.value = false
            emit(emptyList())
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun setSelectedMonth(year: Int, month: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            android.util.Log.d("DashboardViewModel", "Setting month: $year-$month")
            _selectedDate.value = year to month
            _updateTrigger.emit(Unit)
        }
    }

    fun resetDateFilter() {
        viewModelScope.launch {
            _isLoading.value = true
            android.util.Log.d("DashboardViewModel", "Resetting date filter")
            _selectedDate.value = null
            _updateTrigger.emit(Unit)
        }
    }

    fun getFormattedSelectedDate(): String {
        return _selectedDate.value?.let { (year, month) ->
            val calendar = Calendar.getInstance().apply {
                set(Calendar.YEAR, year)
                set(Calendar.MONTH, month)
            }
            val monthFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
            monthFormat.format(calendar.time)
        } ?: "Current Month"
    }

    fun refreshData() {
        viewModelScope.launch {
            _isLoading.value = true
            _updateTrigger.emit(Unit)
        }
    }

    init {
        viewModelScope.launch {
            _updateTrigger.emit(Unit)
        }
    }
}