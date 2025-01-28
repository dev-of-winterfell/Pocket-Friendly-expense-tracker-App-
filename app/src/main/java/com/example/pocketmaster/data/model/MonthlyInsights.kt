package com.example.pocketmaster.data.model

import android.graphics.Color

data class MonthlyInsights(
    val topExpenseCategory: String = "",
    val topExpenseAmount: Double = 0.0,
    val dailyAverageExpense: Double = 0.0,
    val savingsRate: Double = 0.0,
    val expenseTrendPercentage: Double = 0.0
)