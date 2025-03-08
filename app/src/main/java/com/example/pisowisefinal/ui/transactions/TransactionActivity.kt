package com.example.pisowisefinal.ui.transactions

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.pisowisefinal.R
import com.example.pisowisefinal.adapters.TransactionAdapter
import com.example.pisowisefinal.database.DatabaseHelper
import com.example.pisowisefinal.models.Expense
import com.example.pisowisefinal.ui.dashboards.MainActivity
import com.example.pisowisefinal.utils.Constants
import com.example.pisowisefinal.utils.PdfGenerator
import java.text.SimpleDateFormat
import java.util.*

class TransactionActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var transactionAdapter: TransactionAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var spinnerMonth: Spinner
    private lateinit var txtTotalBalance: TextView
    private lateinit var txtTotalIncome: TextView
    private lateinit var txtTotalExpense: TextView

    private var allTransactions: List<Expense> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transaction)

        initUI()
        setupRecyclerView()
        setupMonthSpinner()
        setupButtons()

        loadTransactions() // Load once to avoid repeated DB calls
    }

    private fun initUI() {
        dbHelper = DatabaseHelper(this)
        recyclerView = findViewById(R.id.recyclerView)
        spinnerMonth = findViewById(R.id.spinnerMonthFilter)
        txtTotalBalance = findViewById(R.id.txtTotalBalance)
        txtTotalIncome = findViewById(R.id.txtTotalIncome)
        txtTotalExpense = findViewById(R.id.txtTotalExpense)
    }

    private fun setupRecyclerView() {
        transactionAdapter = TransactionAdapter(mutableListOf(), dbHelper, ::showTransactionPopup)
        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@TransactionActivity)
            adapter = transactionAdapter
        }
    }

    private fun setupMonthSpinner() {
        val months = Constants.MONTHS
        spinnerMonth.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, months).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        spinnerMonth.setSelection(Calendar.getInstance().get(Calendar.MONTH))

        spinnerMonth.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                filterTransactionsByMonth(position + 1)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupButtons() {
        findViewById<ImageButton>(R.id.backButton2).setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        findViewById<ImageButton>(R.id.pdfBtn).setOnClickListener { generatePdf() }

        findViewById<ImageButton>(R.id.addExpensesBtn).setOnClickListener {
            startActivity(Intent(this, AddExpensesActivity::class.java))
        }
    }

    private fun loadTransactions() {
        allTransactions = dbHelper.getAllExpenses() // Fetch all transactions once
        val currentMonthIndex = Calendar.getInstance().get(Calendar.MONTH) + 1
        filterTransactionsByMonth(currentMonthIndex)
    }

    private fun filterTransactionsByMonth(month: Int) {
        val transactions = allTransactions.filter { expense ->
            parseDate(expense.date)?.let { date ->
                val calendar = Calendar.getInstance().apply { time = date }
                val expenseMonth = calendar.get(Calendar.MONTH) + 1
                expenseMonth == month
            } ?: false
        }

        transactionAdapter.updateList(transactions)

        val selectedMonthName = Constants.MONTHS[month - 1] // Get the month name from the array
        if (transactions.isEmpty()) {
            showToast("No transactions found for $selectedMonthName.")
        }

        updateSummary(transactions, month)
    }





    private fun updateSummary(transactions: List<Expense>, month: Int) {
        val (totalIncome, totalExpense) = dbHelper.getTotalIncomeAndExpenseForMonth(month)

        txtTotalIncome.text = getString(R.string.total_income, String.format("%.2f", totalIncome))
        txtTotalExpense.text = getString(R.string.total_expense, String.format("%.2f", totalExpense))
        txtTotalBalance.text = getString(R.string.total_balance, String.format("%.2f", totalIncome - totalExpense))
    }

    private fun generatePdf() {
        val selectedMonth = spinnerMonth.selectedItemPosition + 1
        val monthName = Constants.MONTHS[selectedMonth - 1]
        val fileName = "transactions_$monthName.pdf"
        val transactions = allTransactions.filter { parseDate(it.date)?.let { date ->
            Calendar.getInstance().apply { time = date }.get(Calendar.MONTH) + 1 == selectedMonth } ?: false }

        if (transactions.isNotEmpty()) {
            PdfGenerator.generateTransactionPdf(this, transactions, fileName)
            showToast("PDF '$fileName' generated successfully!")
        } else {
            showToast("No transactions to export for $monthName")
        }
    }

    private fun showTransactionPopup(expense: Expense) {
        val view = layoutInflater.inflate(R.layout.card_transaction, null)
        val dialog = AlertDialog.Builder(this).setView(view).create()

        with(view) {
            findViewById<TextView>(R.id.tvTransactionTitle).text = expense.title
            findViewById<TextView>(R.id.tvTransactionAmount).text = getString(R.string.transaction_amount, String.format("%.2f", expense.amount))
            findViewById<TextView>(R.id.tvTransactionCategory).text = expense.category
            findViewById<TextView>(R.id.tvTransactionDate).text = expense.date
            findViewById<TextView>(R.id.tvTransactionMessage).text = expense.message
            findViewById<TextView>(R.id.tvTransactionType).text = expense.transactionType

            Glide.with(this@TransactionActivity)
                .load(Constants.categoryIcons[expense.category] ?: R.drawable.ic_placeholder)
                .transform(RoundedCorners(30))
                .into(findViewById(R.id.iconCard))

            findViewById<ImageButton>(R.id.backButton4).setOnClickListener { dialog.dismiss() }
            findViewById<Button>(R.id.btnDeleteTransaction).setOnClickListener {
                dbHelper.deleteExpense(expense.id)
                loadTransactions()
                dialog.dismiss()
                showToast("Transaction Deleted")
            }
        }

        dialog.show()
    }

    private fun parseDate(dateString: String): Date? {
        return try { SimpleDateFormat(Constants.DATE_FORMAT, Locale.getDefault()).parse(dateString) }
        catch (e: Exception) { e.printStackTrace(); null }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
