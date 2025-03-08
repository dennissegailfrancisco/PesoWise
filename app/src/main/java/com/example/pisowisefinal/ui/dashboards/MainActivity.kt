package com.example.pisowisefinal.ui.dashboards

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pisowisefinal.R
import com.example.pisowisefinal.adapters.TransactionAdapter
import com.example.pisowisefinal.database.DatabaseHelper
import com.example.pisowisefinal.models.Expense
import com.example.pisowisefinal.ui.transactions.AddExpensesActivity
import com.example.pisowisefinal.ui.transactions.TransactionActivity
import com.example.pisowisefinal.utils.Constants
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
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        dbHelper = DatabaseHelper(this)
        initViews()
        setListeners()
    }

    private fun initViews() {
        tvTotalBalance = findViewById(R.id.tvTotalBalance)
        tvTotalExpenses = findViewById(R.id.tvTotalExpenses)
        tvSavingsLastWeek = findViewById(R.id.tvSavingsLastWeek)
        tvFoodExpenseLastWeek = findViewById(R.id.tvFoodExpenseLastWeek)
        rvRecentTransactions = findViewById(R.id.rvRecentTransactions)
        progressBar = findViewById(R.id.progressBar2)

        rvRecentTransactions.layoutManager = LinearLayoutManager(this)
    }

    private fun setListeners() {
        findViewById<ImageButton>(R.id.btnAddTransaction).setOnClickListener {
            startActivity(Intent(this, AddExpensesActivity::class.java))
        }

        findViewById<ImageButton>(R.id.btnViewTransactions).setOnClickListener {
            startActivity(Intent(this, TransactionActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        updateDashboard()
    }

    private fun updateDashboard() {
        val transactions = dbHelper.getAllExpenses()
        val totalIncome =
            transactions.sumOf { if (it.transactionType == Constants.TYPE_INCOME) it.amount else 0.0 }
        val totalExpense =
            transactions.sumOf { if (it.transactionType == Constants.TYPE_EXPENSE) it.amount else 0.0 }
        val totalBalance = totalIncome - totalExpense

        tvTotalBalance.text = getString(R.string.total_balance, String.format("%.2f", totalBalance))
        tvTotalExpenses.text = getString(R.string.total_expense, String.format("%.2f", totalExpense))

        updateProgressBar(totalBalance, totalExpense)
        calculateLastWeekData()
        displayRecentTransactions(transactions)
    }

    private fun updateProgressBar(totalBalance: Double, totalExpense: Double) {
        val total = totalBalance + totalExpense
        if (total > 0) {
            progressBar.progress = ((totalBalance / total) * 100).toInt()
            progressBar.secondaryProgress = ((totalExpense / total) * 100).toInt()
        } else {
            progressBar.progress = 0
            progressBar.secondaryProgress = 0
        }
    }

    private fun calculateLastWeekData() {
        val calendar = Calendar.getInstance().apply { add(Calendar.WEEK_OF_YEAR, -1) }
        val lastWeekDate = calendar.time
        val dateFormat = SimpleDateFormat(Constants.DATE_FORMAT, Locale.getDefault())

        val lastWeekTransactions = dbHelper.getAllExpenses().filter {
            runCatching { dateFormat.parse(it.date)?.after(lastWeekDate) ?: false }.getOrDefault(false)
        }

        val totalLastWeekIncome =
            lastWeekTransactions.sumOf { if (it.transactionType == Constants.TYPE_INCOME) it.amount else 0.0 }
        val totalLastWeekExpense =
            lastWeekTransactions.sumOf { if (it.transactionType == Constants.TYPE_EXPENSE) it.amount else 0.0 }
        val savingsLastWeek = totalLastWeekIncome - totalLastWeekExpense

        val foodExpensesLastWeek = lastWeekTransactions
            .filter { it.category.equals("Food", ignoreCase = true) }
            .sumOf { it.amount }

        tvSavingsLastWeek.text = getString(R.string.savings_last_week, String.format("%.2f", savingsLastWeek))
        tvFoodExpenseLastWeek.text = getString(R.string.food_expense_last_week, String.format("%.2f", foodExpensesLastWeek))
    }

    private fun displayRecentTransactions(transactions: List<Expense>) {
        val recentTransactions = transactions.sortedByDescending { it.date }.take(Constants.RECENT_TRANSACTIONS_LIMIT)

        if (!::transactionAdapter.isInitialized) {
            transactionAdapter = TransactionAdapter(recentTransactions.toMutableList(), dbHelper) { showTransactionPopup(it) }
            rvRecentTransactions.adapter = transactionAdapter
        } else {
            transactionAdapter.updateList(recentTransactions)
        }
    }

    private fun showTransactionPopup(expense: Expense) {
        val view = layoutInflater.inflate(R.layout.card_transaction, null)
        val dialog = AlertDialog.Builder(this).setView(view).create()

        view.findViewById<TextView>(R.id.tvTransactionTitle).text = expense.title
        view.findViewById<TextView>(R.id.tvTransactionAmount).text =
            getString(R.string.transaction_amount, String.format("%.2f", expense.amount))
        view.findViewById<TextView>(R.id.tvTransactionCategory).text = expense.category
        view.findViewById<TextView>(R.id.tvTransactionDate).text = expense.date
        view.findViewById<TextView>(R.id.tvTransactionMessage).text = expense.message
        view.findViewById<TextView>(R.id.tvTransactionType).text = expense.transactionType

        val iconResId = Constants.categoryIcons[expense.category] ?: R.drawable.ic_placeholder
        view.findViewById<ImageView>(R.id.iconCard).setImageResource(iconResId)

        view.findViewById<ImageButton>(R.id.backButton4).setOnClickListener { dialog.dismiss() }

        view.findViewById<Button>(R.id.btnDeleteTransaction).setOnClickListener {
            dbHelper.deleteExpense(expense.id)
            updateTransactionList()
            updateDashboard()
            dialog.dismiss()
            Toast.makeText(this, "Transaction Deleted", Toast.LENGTH_SHORT).show()
        }

        dialog.show()
    }

    private fun updateTransactionList() {
        displayRecentTransactions(dbHelper.getAllExpenses())
    }
}
