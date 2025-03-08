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
        private const val DATABASE_VERSION = 3

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
        return writableDatabase.use { db ->
            val values = ContentValues().apply {
                put(COLUMN_CATEGORY, expense.category)
                put(COLUMN_DATE, expense.date)
                put(COLUMN_AMOUNT, expense.amount)
                put(COLUMN_NOTE, expense.message)
                put(COLUMN_TRANSACTION_TYPE, expense.transactionType)
                put(COLUMN_TITLE, expense.title)
            }
            db.insert(TABLE_EXPENSES, null, values)
        }
    }

    fun getAllExpenses(): List<Expense> {
        val expenseList = mutableListOf<Expense>()
        readableDatabase.use { db ->
            db.rawQuery("SELECT * FROM $TABLE_EXPENSES", null).use { cursor ->
                while (cursor.moveToNext()) {
                    expenseList.add(
                        Expense(
                            id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                            category = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY)),
                            date = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE)),
                            amount = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_AMOUNT)),
                            transactionType = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TRANSACTION_TYPE)),
                            title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE)),
                            message = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NOTE))
                        )
                    )
                }
            }
        }
        return expenseList
    }

    fun deleteExpense(id: Int?): Boolean {
        return id?.let {
            writableDatabase.use { db ->
                db.delete(TABLE_EXPENSES, "$COLUMN_ID=?", arrayOf(it.toString())) > 0
            }
        } ?: false
    }

    fun getTotalIncomeAndExpenseForMonth(month: Int): Pair<Double, Double> {
        val datePattern = "%-${String.format("%02d", month)}-%"
        return readableDatabase.use { db ->
            val income = db.rawQuery("SELECT SUM(amount) FROM $TABLE_EXPENSES WHERE $COLUMN_TRANSACTION_TYPE = ? AND $COLUMN_DATE LIKE ?",
                arrayOf(Constants.TYPE_INCOME, datePattern)).use { cursor ->
                if (cursor.moveToFirst()) cursor.getDouble(0) else 0.0
            }

            val expense = db.rawQuery("SELECT SUM(amount) FROM $TABLE_EXPENSES WHERE $COLUMN_TRANSACTION_TYPE = ? AND $COLUMN_DATE LIKE ?",
                arrayOf(Constants.TYPE_EXPENSE, datePattern)).use { cursor ->
                if (cursor.moveToFirst()) cursor.getDouble(0) else 0.0
            }

            Pair(income, expense)
        }
    }
}
