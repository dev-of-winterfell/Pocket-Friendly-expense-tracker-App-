package com.example.pocketmaster.data.repository

import com.example.pocketmaster.data.dao.CategoryDao
import com.example.pocketmaster.data.dao.TransactionDao
import com.example.pocketmaster.data.model.Category
import com.example.pocketmaster.data.model.CategoryTotal
import com.example.pocketmaster.data.model.Transaction
import com.example.pocketmaster.data.model.TransactionType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class FinanceRepository(
    private val transactionDao: TransactionDao,
    private val categoryDao: CategoryDao
) {
    val allTransactions = transactionDao.getAllTransactions()
    val allCategories = categoryDao.getAllCategories()

    fun getTransactionsByType(type: TransactionType) =
        transactionDao.getTransactionsByType(type)

    fun getCategoriesByType(type: TransactionType) =
        categoryDao.getCategoriesByType(type)

    // In FinanceRepository
    fun getCategoryTotals(type: TransactionType, startDate: Long = 0): Flow<List<CategoryTotal>> =
        categoryDao.getCategoryTotals(type, startDate)

    suspend fun getTotalByType(type: TransactionType) =
        transactionDao.getTotalByType(type) ?: 0.0

    suspend fun addTransaction(transaction: Transaction) {
        transactionDao.insert(transaction)
    }

    suspend fun deleteTransaction(transaction: Transaction) {
        transactionDao.delete(transaction)
    }

    suspend fun addCategory(category: Category) {
        categoryDao.insert(category)
    }
}
