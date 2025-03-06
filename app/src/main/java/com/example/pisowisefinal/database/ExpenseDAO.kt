package com.example.pisowisefinal.database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.example.pisowisefinal.models.Expense


class ExpenseDAO(context: Context) {
    private val dbHelper = ExpenseDatabaseHelper(context)

    fun insertExpense(amount: Double, category: String, date: String, note: String?): Long {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(ExpenseDatabaseHelper.COLUMN_AMOUNT, amount)
            put(ExpenseDatabaseHelper.COLUMN_CATEGORY, category)
            put(ExpenseDatabaseHelper.COLUMN_DATE, date)
            put(ExpenseDatabaseHelper.COLUMN_NOTE, note)
        }
        val result = db.insert(ExpenseDatabaseHelper.TABLE_EXPENSES, null, values)
        db.close()
        return result
    }


    fun getAllExpenses(): List<Expense> {
        val expenseList = mutableListOf<Expense>()
        val db = dbHelper.readableDatabase
        val cursor: Cursor = db.rawQuery("SELECT * FROM ${ExpenseDatabaseHelper.TABLE_EXPENSES}", null)

        while (cursor.moveToNext()) {
            val id = cursor.getInt(cursor.getColumnIndexOrThrow(ExpenseDatabaseHelper.COLUMN_ID))
            val amount = cursor.getDouble(cursor.getColumnIndexOrThrow(ExpenseDatabaseHelper.COLUMN_AMOUNT))
            val category = cursor.getString(cursor.getColumnIndexOrThrow(ExpenseDatabaseHelper.COLUMN_CATEGORY))
            val date = cursor.getString(cursor.getColumnIndexOrThrow(ExpenseDatabaseHelper.COLUMN_DATE))
            val note = cursor.getString(cursor.getColumnIndexOrThrow(ExpenseDatabaseHelper.COLUMN_NOTE))

            // Use default values for missing fields
            val expense = Expense(id, category, date, amount, "", "", note ?: "")
            expenseList.add(expense)
        }

        cursor.close()
        db.close()
        return expenseList
    }


    // Update an expense
    fun updateExpense(id: Int, amount: Double, category: String, date: String, note: String?): Int {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(ExpenseDatabaseHelper.COLUMN_AMOUNT, amount)
            put(ExpenseDatabaseHelper.COLUMN_CATEGORY, category)
            put(ExpenseDatabaseHelper.COLUMN_DATE, date)
            put(ExpenseDatabaseHelper.COLUMN_NOTE, note)
        }
        val result = db.update(ExpenseDatabaseHelper.TABLE_EXPENSES, values, "${ExpenseDatabaseHelper.COLUMN_ID} = ?", arrayOf(id.toString()))
        db.close()
        return result
    }

    // Delete an expense
    fun deleteExpense(id: Int): Int {
        val db = dbHelper.writableDatabase
        val result = db.delete(ExpenseDatabaseHelper.TABLE_EXPENSES, "${ExpenseDatabaseHelper.COLUMN_ID} = ?", arrayOf(id.toString()))
        db.close()
        return result
    }
}
