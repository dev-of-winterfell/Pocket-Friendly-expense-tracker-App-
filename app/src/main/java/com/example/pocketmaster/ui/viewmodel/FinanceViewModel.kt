package com.example.pocketmaster.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.pocketmaster.data.database.AppDatabase
import com.example.pocketmaster.data.model.Category
import com.example.pocketmaster.data.model.Transaction
import com.example.pocketmaster.data.model.TransactionType
import com.example.pocketmaster.data.repository.FinanceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class FinanceViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: FinanceRepository
    val allTransactions: Flow<List<Transaction>>
    val allCategories: Flow<List<Category>>

    init {
        val database = AppDatabase.getDatabase(application)
        repository = FinanceRepository(database.transactionDao(), database.categoryDao())
        allTransactions = repository.allTransactions
        allCategories = repository.allCategories
    }

    fun getTransactionsByType(type: TransactionType) =
        repository.getTransactionsByType(type)

    fun getCategoriesByType(type: TransactionType) =
        repository.getCategoriesByType(type)

    fun getCategoryTotals(type: TransactionType, startDate: Long = 0) =
        repository.getCategoryTotals(type, startDate)

    suspend fun getTotalByType(type: TransactionType) =
        repository.getTotalByType(type)

    fun addTransaction(transaction: Transaction) = viewModelScope.launch {
        repository.addTransaction(transaction)
    }

    fun deleteTransaction(transaction: Transaction) = viewModelScope.launch {
        repository.deleteTransaction(transaction)
    }

    fun addCategory(category: Category) = viewModelScope.launch {
        repository.addCategory(category)
    }
}