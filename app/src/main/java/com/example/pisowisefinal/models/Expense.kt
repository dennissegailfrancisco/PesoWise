package com.example.pisowisefinal.models

import com.example.pisowisefinal.utils.Constants

data class Expense(
    val id: Int,  // Make it non-nullable to ensure deletion works properly
    val category: String,
    val date: String,
    val amount: Double,
    val transactionType: String = Constants.DEFAULT_TRANSACTION_TYPE,
    val title: String = Constants.DEFAULT_TITLE,
    val message: String = Constants.DEFAULT_MESSAGE
)
