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

class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: FinanceRepository = FinanceRepository(
        AppDatabase.getDatabase(application).transactionDao(),
        AppDatabase.getDatabase(application).categoryDao()
    )

    // Create reactive flows using the repository's existing functions
    private val incomeTotalFlow = repository.getTransactionsByType(TransactionType.INCOME)
        .map { transactions ->
            transactions.sumOf { it.amount }
        }

    private val expenseTotalFlow = repository.getTransactionsByType(TransactionType.EXPENSE)
        .map { transactions ->
            transactions.sumOf { it.amount }
        }

    val dashboardState: StateFlow<DashboardState> = combine(
        incomeTotalFlow,
        expenseTotalFlow,
        repository.getCategoryTotals(TransactionType.EXPENSE)
    ) { income: Double, expense: Double, categoryTotals: List<CategoryTotal> ->
        DashboardState(
            totalIncome = income,
            totalExpense = expense,
            balance = income - expense,
            expenseCategories = categoryTotals.map { categoryTotal ->
                ExpenseCategoryData(
                    category = categoryTotal.name,
                    amount = categoryTotal.total
                )
            }
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardState()
    )
}