package com.example.pisowisefinal.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.pisowisefinal.models.Expense
import com.example.pisowisefinal.utils.Constants

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "pesowise.db"
        private const val DATABASE_VERSION = 3  // Increase the version

        const val TABLE_EXPENSES = "expenses"
        const val COLUMN_ID = "id"
        const val COLUMN_CATEGORY = "category"
        const val COLUMN_DATE = "date"
        const val COLUMN_AMOUNT = "amount"
        const val COLUMN_TRANSACTION_TYPE = "transaction_type"
        const val COLUMN_TITLE = "title"
        const val COLUMN_NOTE = "note"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTableQuery = """
            CREATE TABLE $TABLE_EXPENSES (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_CATEGORY TEXT NOT NULL,
                $COLUMN_DATE TEXT NOT NULL,
                $COLUMN_AMOUNT REAL NOT NULL,
                $COLUMN_NOTE TEXT,
                $COLUMN_TRANSACTION_TYPE TEXT NOT NULL,
                $COLUMN_TITLE TEXT
            )
        """.trimIndent()
        db.execSQL(createTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE $TABLE_EXPENSES ADD COLUMN $COLUMN_NOTE TEXT DEFAULT ''")
        }
        if (oldVersion < 3) {
            db.execSQL("ALTER TABLE $TABLE_EXPENSES ADD COLUMN $COLUMN_TITLE TEXT DEFAULT ''")
            db.execSQL("ALTER TABLE $TABLE_EXPENSES ADD COLUMN $COLUMN_TRANSACTION_TYPE TEXT DEFAULT ''")
        }
    }

    fun addExpense(expense: Expense): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_CATEGORY, expense.category)
            put(COLUMN_DATE, expense.date)
            put(COLUMN_AMOUNT, expense.amount)
            put(COLUMN_NOTE, expense.message)  // Now it won't cause errors
            put(COLUMN_TRANSACTION_TYPE, expense.transactionType)  // Store income/expense type
            put(COLUMN_TITLE, expense.title)  // Store transaction title
        }
        return db.insert(TABLE_EXPENSES, null, values).also { db.close() }
    }

    fun getAllExpenses(): List<Expense> {
        val expenseList = mutableListOf<Expense>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_EXPENSES", null)

        while (cursor.moveToNext()) {
            val id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID))
            val category = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY))
            val date = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE))
            val amount = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_AMOUNT))
            val note = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NOTE))
            val transactionType = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TRANSACTION_TYPE))
            val title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE))

            expenseList.add(Expense(id, category, date, amount, transactionType, title, note))
        }

        cursor.close()
        db.close()
        return expenseList
    }

    fun deleteExpense(id: Int?): Boolean {
        if (id == null) return false  // Ensure ID is not null
        val db = this.writableDatabase
        val result = db.delete(TABLE_EXPENSES, "$COLUMN_ID=?", arrayOf(id.toString()))
        db.close()
        return result > 0
    }

    fun getTotalIncomeAndExpenseForMonth(month: Int): Pair<Double, Double> {
        val db = readableDatabase
        val datePattern = "%-${String.format("%02d", month)}-%" // Matches 'YYYY-MM-%'

        val incomeQuery = "SELECT SUM(amount) FROM $TABLE_EXPENSES WHERE $COLUMN_TRANSACTION_TYPE = ? AND $COLUMN_DATE LIKE ?"
        val expenseQuery = "SELECT SUM(amount) FROM $TABLE_EXPENSES WHERE $COLUMN_TRANSACTION_TYPE = ? AND $COLUMN_DATE LIKE ?"

        val incomeCursor = db.rawQuery(incomeQuery, arrayOf(Constants.TYPE_INCOME, datePattern))
        val expenseCursor = db.rawQuery(expenseQuery, arrayOf(Constants.TYPE_EXPENSE, datePattern))

        var totalIncome = 0.0
        var totalExpense = 0.0

        if (incomeCursor.moveToFirst()) {
            totalIncome = incomeCursor.getDouble(0)
        }
        if (expenseCursor.moveToFirst()) {
            totalExpense = expenseCursor.getDouble(0)
        }

        incomeCursor.close()
        expenseCursor.close()
        db.close()

        return Pair(totalIncome, totalExpense)
    }
}
