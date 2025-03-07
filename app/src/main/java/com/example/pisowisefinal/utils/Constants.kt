package com.example.pisowisefinal.utils

import com.example.pisowisefinal.R

object Constants {

    const val TYPE_EXPENSE = "Expense"
    const val TYPE_INCOME = "Income"

    const val DEFAULT_TRANSACTION_TYPE = "N/A"
    const val DEFAULT_TITLE = "N/A"
    const val DEFAULT_MESSAGE = "N/A"

    const val DATE_FORMAT = "yyyy-MM-dd"

    const val ALERT_DIALOG_TITLE = "Delete Transaction"
    const val ALERT_DIALOG_MESSAGE = "Are you sure you want to delete this transaction?"
    const val ALERT_DIALOG_POSITIVE = "Yes"
    const val ALERT_DIALOG_NEGATIVE = "No"

    const val RECENT_TRANSACTIONS_LIMIT = 20

    val DEFAULT_ICON = R.drawable.ic_placeholder

    val categoryIcons = mapOf(
        "Food" to R.drawable.foodicon,
        "Shopping" to R.drawable.shopping,
        "Grocery" to R.drawable.grocery,
        "Rent" to R.drawable.rent,
        "Transport" to R.drawable.transpo,
        "Bills" to R.drawable.bills,
        "Entertainment" to R.drawable.entertainment,
        "Other Expense" to R.drawable.other_exp,
        "Salary" to R.drawable.salary,
        "Freelancing" to R.drawable.freelancing,
        "Investments" to R.drawable.investment,
        "Gifts" to R.drawable.gift,
        "Other income" to R.drawable.other_income
    )

    val MONTHS = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )
}
