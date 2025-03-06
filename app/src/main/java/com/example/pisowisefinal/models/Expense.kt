package com.example.pisowisefinal.models

data class Expense(
    val id: Int? = null,  // Nullable since it’s auto-incremented
    val category: String,
    val date: String,
    val amount: Double,
    val transactionType: String? = null,  // Nullable, avoids storing empty strings
    val title: String? = null,
    val message: String? = null
)
