package com.example.pisowisefinal.ui.transactions

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pisowisefinal.R
import com.example.pisowisefinal.models.Expense
import com.example.pisowisefinal.adapters.TransactionAdapter
import com.example.pisowisefinal.database.DatabaseHelper
import com.example.pisowisefinal.ui.dashboards.MainActivity
import com.example.pisowisefinal.utils.Constants
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

        initUI()
        setupRecyclerView()
        setupMonthSpinner()
        setupBackButton()

        val currentMonthIndex = Calendar.getInstance().get(Calendar.MONTH)
        filterTransactionsByMonth(currentMonthIndex + 1)
    }

    private fun initUI() {
        dbHelper = DatabaseHelper(this)
        recyclerView = findViewById(R.id.recyclerView)
        spinnerMonth = findViewById(R.id.spinnerMonthFilter)
        backButton = findViewById(R.id.backButton2)
        txtTotalBalance = findViewById(R.id.txtTotalBalance)
        txtTotalIncome = findViewById(R.id.txtTotalIncome)
        txtTotalExpense = findViewById(R.id.txtTotalExpense)
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        transactionAdapter = TransactionAdapter(dbHelper.getAllExpenses().toMutableList(), dbHelper) { expense ->
            showTransactionPopup(expense)
        }
        recyclerView.adapter = transactionAdapter
    }

    private fun setupMonthSpinner() {
        val months = listOf(
            Constants.MONTH_JANUARY, Constants.MONTH_FEBRUARY, Constants.MONTH_MARCH,
            Constants.MONTH_APRIL, Constants.MONTH_MAY, Constants.MONTH_JUNE,
            Constants.MONTH_JULY, Constants.MONTH_AUGUST, Constants.MONTH_SEPTEMBER,
            Constants.MONTH_OCTOBER, Constants.MONTH_NOVEMBER, Constants.MONTH_DECEMBER
        )
        val monthAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, months)
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerMonth.adapter = monthAdapter

        val currentMonthIndex = Calendar.getInstance().get(Calendar.MONTH)
        spinnerMonth.setSelection(currentMonthIndex)

        spinnerMonth.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedMonth = position + 1
                filterTransactionsByMonth(selectedMonth)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupBackButton() {
        backButton.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    private fun showTransactionPopup(expense: Expense) {
        val view = LayoutInflater.from(this).inflate(R.layout.card_transaction, null)
        val dialog = AlertDialog.Builder(this).setView(view).create()

        val tvTitle = view.findViewById<TextView>(R.id.tvTransactionTitle)
        val tvAmount = view.findViewById<TextView>(R.id.tvTransactionAmount)
        val tvCategory = view.findViewById<TextView>(R.id.tvTransactionCategory)
        val tvDate = view.findViewById<TextView>(R.id.tvTransactionDate)
        val tvMessage = view.findViewById<TextView>(R.id.tvTransactionMessage)
        val tvType = view.findViewById<TextView>(R.id.tvTransactionType)
        val btnDelete = view.findViewById<Button>(R.id.btnDeleteTransaction)
        val btnClose = view.findViewById<ImageButton>(R.id.backButton4)

        tvTitle.text = expense.title
        tvAmount.text = getString(R.string.transaction_amount, String.format("%.2f", expense.amount))
        tvCategory.text = getString(R.string.transaction_category, expense.category)
        tvDate.text = getString(R.string.transaction_date, expense.date)
        tvMessage.text = getString(R.string.transaction_message, expense.message)
        tvType.text = getString(R.string.transaction_type, expense.transactionType)



        btnClose.setOnClickListener { dialog.dismiss() }

        btnDelete.setOnClickListener {
            dbHelper.deleteExpense(expense.id)
            updateTransactionList()
            filterTransactionsByMonth(spinnerMonth.selectedItemPosition + 1)
            dialog.dismiss()
            Toast.makeText(this, "Transaction Deleted", Toast.LENGTH_SHORT).show()
        }

        dialog.show()
    }

    private fun filterTransactionsByMonth(month: Int) {
        val allTransactions = dbHelper.getAllExpenses()
        val filteredTransactions = allTransactions.filter {
            try {
                val dateFormat = SimpleDateFormat(Constants.DATE_FORMAT, Locale.getDefault())
                val date = dateFormat.parse(it.date)
                val calendar = Calendar.getInstance().apply { time = date ?: return@filter false }
                calendar.get(Calendar.MONTH) + 1 == month
            } catch (e: Exception) {
                false
            }
        }

        transactionAdapter.updateList(filteredTransactions)

        val (totalIncome, totalExpense) = dbHelper.getTotalIncomeAndExpenseForMonth(month)
        val totalBalance = totalIncome - totalExpense

        txtTotalIncome.text = getString(R.string.total_income, String.format("%.2f", totalIncome))
        txtTotalExpense.text = getString(R.string.total_expense, String.format("%.2f", totalExpense))
        txtTotalBalance.text = getString(R.string.total_balance, String.format("%.2f", totalBalance))

    }

    private fun updateTransactionList() {
        val updatedTransactions = dbHelper.getAllExpenses()
        transactionAdapter.updateList(updatedTransactions)
    }
}
