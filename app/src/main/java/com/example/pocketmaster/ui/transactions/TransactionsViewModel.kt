package com.example.pocketmaster.ui.transactions

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.pocketmaster.data.database.AppDatabase
import com.example.pocketmaster.data.model.Transaction
import com.example.pocketmaster.data.model.TransactionFilter
import com.example.pocketmaster.data.model.TransactionType
import com.example.pocketmaster.data.repository.FinanceRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TransactionsViewModel(
    application: Application
) : AndroidViewModel(application) {

    // Initialize repository first
    private val repository: FinanceRepository = FinanceRepository(
        AppDatabase.getDatabase(application).transactionDao(),
        AppDatabase.getDatabase(application).categoryDao()
    )

    private val _transactionFilter = MutableStateFlow(TransactionFilter.ALL)

    // Now we can safely use repository since it's already initialized

    @OptIn(ExperimentalCoroutinesApi::class)
    val transactions = _transactionFilter
        .flatMapLatest { filter ->
            when (filter) {
                TransactionFilter.ALL -> repository.allTransactions
                TransactionFilter.INCOME -> repository.getTransactionsByType(TransactionType.INCOME)
                TransactionFilter.EXPENSE -> repository.getTransactionsByType(TransactionType.EXPENSE)
            }
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    fun setTransactionFilter(filter: TransactionFilter) {
        _transactionFilter.value = filter
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
        }
    }
}
