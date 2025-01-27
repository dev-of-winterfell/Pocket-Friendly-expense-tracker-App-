package com.example.pocketmaster.utils

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateFormatter {
    private val fullFormatter = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
    private val shortFormatter = SimpleDateFormat("MMM dd", Locale.getDefault())

    fun formatFull(timestamp: Long): String {
        return fullFormatter.format(Date(timestamp))
    }

    fun formatShort(timestamp: Long): String {
        return shortFormatter.format(Date(timestamp))
    }

    fun getMonthStart(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }
}