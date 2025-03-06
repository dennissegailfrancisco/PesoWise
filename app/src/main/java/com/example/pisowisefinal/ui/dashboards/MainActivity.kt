package com.example.pisowisefinal.ui.dashboards

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pisowisefinal.R
import com.example.pisowisefinal.adapters.TransactionAdapter
import com.example.pisowisefinal.database.DatabaseHelper
import com.example.pisowisefinal.models.Expense
import com.example.pisowisefinal.ui.transactions.AddExpensesActivity
import com.example.pisowisefinal.ui.transactions.TransactionActivity
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var transactionAdapter: TransactionAdapter
    private lateinit var rvRecentTransactions: RecyclerView
    private lateinit var tvTotalBalance: TextView
    private lateinit var tvTotalExpenses: TextView
    private lateinit var tvSavingsLastWeek: TextView
    private lateinit var tvFoodExpenseLastWeek: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        dbHelper = DatabaseHelper(this)

        // Find Views
        val btnAddTransaction = findViewById<Button>(R.id.btnAddTransaction)
        val btnViewTransactions = findViewById<Button>(R.id.btnViewTransactions)

        tvTotalBalance = findViewById(R.id.tvTotalBalance)
        tvTotalExpenses = findViewById(R.id.tvTotalExpenses)
        tvSavingsLastWeek = findViewById(R.id.tvSavingsLastWeek)
        tvFoodExpenseLastWeek = findViewById(R.id.tvFoodExpenseLastWeek)
        rvRecentTransactions = findViewById(R.id.rvRecentTransactions)

        rvRecentTransactions.layoutManager = LinearLayoutManager(this)

        btnAddTransaction.setOnClickListener {
            startActivity(Intent(this, AddExpensesActivity::class.java))
        }

        btnViewTransactions.setOnClickListener {
            startActivity(Intent(this, TransactionActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        updateDashboard()
    }

    private fun updateDashboard() {
        val allTransactions = dbHelper.getAllExpenses()
        val totalIncome =
            allTransactions.filter { it.transactionType == "Income" }.sumOf { it.amount }
        val totalExpense =
            allTransactions.filter { it.transactionType == "Expense" }.sumOf { it.amount }
        val totalBalance = totalIncome - totalExpense

        tvTotalBalance.text = "₱${String.format("%.2f", totalBalance)}"
        tvTotalExpenses.text = "-₱${String.format("%.2f", totalExpense)}"

        calculateLastWeekData()
        displayRecentTransactions(allTransactions)
    }

    private fun calculateLastWeekData() {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.WEEK_OF_YEAR, -1)
        val lastWeekDate = calendar.time
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        val lastWeekTransactions = dbHelper.getAllExpenses().filter {
            try {
                val transactionDate = dateFormat.parse(it.date)
                transactionDate?.after(lastWeekDate) == true
            } catch (e: Exception) {
                false
            }
        }

        val totalLastWeekIncome =
            lastWeekTransactions.filter { it.transactionType == "Income" }.sumOf { it.amount }
        val totalLastWeekExpense =
            lastWeekTransactions.filter { it.transactionType == "Expense" }.sumOf { it.amount }
        val savingsLastWeek = totalLastWeekIncome - totalLastWeekExpense

        val foodExpensesLastWeek = lastWeekTransactions
            .filter { it.category.equals("Food", ignoreCase = true) }
            .sumOf { it.amount }

        tvSavingsLastWeek.text = "₱${String.format("%.2f", savingsLastWeek)}"
        tvFoodExpenseLastWeek.text = "-₱${String.format("%.2f", foodExpensesLastWeek)}"
    }

    private fun displayRecentTransactions(transactions: List<Expense>) {
        val recentTransactions = transactions.sortedByDescending { it.date }.take(20)

        if (!::transactionAdapter.isInitialized) {
            transactionAdapter =
                TransactionAdapter(recentTransactions.toMutableList()) // Convert to mutable list
            rvRecentTransactions.adapter = transactionAdapter
        } else {
            transactionAdapter.updateList(recentTransactions)
        }
    }
}
