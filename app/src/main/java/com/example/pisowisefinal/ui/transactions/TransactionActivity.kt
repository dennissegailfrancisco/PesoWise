package com.example.pisowisefinal.ui.transactions

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pisowisefinal.R
import com.example.pisowisefinal.adapters.TransactionAdapter
import com.example.pisowisefinal.database.DatabaseHelper
import com.example.pisowisefinal.ui.dashboards.MainActivity
import java.text.SimpleDateFormat
import java.util.*

class TransactionActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var transactionAdapter: TransactionAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var spinnerMonth: Spinner
    private lateinit var backButton: ImageButton

    private lateinit var txtTotalBalance: TextView
    private lateinit var txtTotalIncome: TextView
    private lateinit var txtTotalExpense: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transaction)

        dbHelper = DatabaseHelper(this)
        recyclerView = findViewById(R.id.recyclerView)
        spinnerMonth = findViewById(R.id.spinnerMonthFilter)
        backButton = findViewById(R.id.backButton2)

        txtTotalBalance = findViewById(R.id.txtTotalBalance)
        txtTotalIncome = findViewById(R.id.txtTotalIncome)
        txtTotalExpense = findViewById(R.id.txtTotalExpense)

        recyclerView.layoutManager = LinearLayoutManager(this)

        val allTransactions = dbHelper.getAllExpenses().toMutableList()
        Log.d("TransactionActivity", "Transactions from DB: $allTransactions")

        transactionAdapter = TransactionAdapter(allTransactions)
        recyclerView.adapter = transactionAdapter

        // Setup month filter
        val months = listOf(
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
        )
        val monthAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, months)
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerMonth.adapter = monthAdapter

        spinnerMonth.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedMonth = position + 1
                filterTransactionsByMonth(selectedMonth)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Back Button Functionality
        backButton.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    // ✅ FIXED: Only One `filterTransactionsByMonth()` Function
    private fun filterTransactionsByMonth(month: Int) {
        val allTransactions = dbHelper.getAllExpenses()
        val filteredTransactions = allTransactions.filter {
            try {
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val date = dateFormat.parse(it.date)
                val calendar = Calendar.getInstance()
                calendar.time = date ?: return@filter false
                calendar.get(Calendar.MONTH) + 1 == month
            } catch (e: Exception) {
                Log.e("TransactionActivity", "Date parsing error: ${e.message}")
                false
            }
        }

        Log.d("TransactionActivity", "Filtered Transactions for month $month: $filteredTransactions")
        transactionAdapter.updateList(filteredTransactions)

        // Fetch total income & expense for selected month
        val (totalIncome, totalExpense) = dbHelper.getTotalIncomeAndExpenseForMonth(month)
        val totalBalance = totalIncome - totalExpense

        // Update UI
        txtTotalIncome.text = "₱${String.format("%.2f", totalIncome)}"
        txtTotalExpense.text = "-₱${String.format("%.2f", totalExpense)}"
        txtTotalBalance.text = "₱${String.format("%.2f", totalBalance)}"
    }

    override fun onResume() {
        super.onResume()
        updateTransactionList()
    }

    private fun updateTransactionList() {
        val updatedTransactions = dbHelper.getAllExpenses()
        Log.d("TransactionActivity", "updateTransactionList() - Reloaded transactions: $updatedTransactions")
        transactionAdapter.updateList(updatedTransactions)
    }
}
