package com.example.pisowisefinal.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class ExpenseDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        val createTableQuery = """
        CREATE TABLE $TABLE_EXPENSES (
            $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
            $COLUMN_AMOUNT REAL NOT NULL,
            $COLUMN_CATEGORY TEXT NOT NULL,
            $COLUMN_DATE TEXT NOT NULL,
            $COLUMN_TRANSACTION_TYPE TEXT NOT NULL,
            $COLUMN_TITLE TEXT NOT NULL,
            $COLUMN_MESSAGE TEXT NOT NULL,
            $COLUMN_NOTE TEXT
        )
    """.trimIndent()
        db.execSQL(createTableQuery)
    }


    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE $TABLE_EXPENSES ADD COLUMN $COLUMN_TRANSACTION_TYPE TEXT DEFAULT ''")
            db.execSQL("ALTER TABLE $TABLE_EXPENSES ADD COLUMN $COLUMN_TITLE TEXT DEFAULT ''")
            db.execSQL("ALTER TABLE $TABLE_EXPENSES ADD COLUMN $COLUMN_MESSAGE TEXT DEFAULT ''")
        }
    }


    companion object {
        private const val DATABASE_NAME = "expenses.db"
        private const val DATABASE_VERSION = 2

        const val TABLE_EXPENSES = "expenses"
        const val COLUMN_ID = "id"
        const val COLUMN_AMOUNT = "amount"
        const val COLUMN_CATEGORY = "category"
        const val COLUMN_DATE = "date"
        const val COLUMN_TRANSACTION_TYPE = "transaction_type"
        const val COLUMN_TITLE = "title"
        const val COLUMN_MESSAGE = "message"
        const val COLUMN_NOTE = "note"
    }

}
