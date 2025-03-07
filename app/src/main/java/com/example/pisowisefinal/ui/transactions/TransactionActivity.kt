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
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions


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
        val months = Constants.MONTHS
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
        val view = layoutInflater.inflate(R.layout.card_transaction, null)
        val dialog = AlertDialog.Builder(this).setView(view).create()

        view.findViewById<TextView>(R.id.tvTransactionTitle).text = expense.title
        view.findViewById<TextView>(R.id.tvTransactionAmount).text =
            getString(R.string.transaction_amount, String.format("%.2f", expense.amount))
        view.findViewById<TextView>(R.id.tvTransactionCategory).text = expense.category
        view.findViewById<TextView>(R.id.tvTransactionDate).text = expense.date
        view.findViewById<TextView>(R.id.tvTransactionMessage).text = expense.message
        view.findViewById<TextView>(R.id.tvTransactionType).text = expense.transactionType


        val iconCard = view.findViewById<ImageView>(R.id.iconCard)
        val iconResId = Constants.categoryIcons[expense.category] ?: R.drawable.ic_placeholder

        Glide.with(this)
            .load(iconResId)
            .transform(RoundedCorners(30))
            .into(iconCard)

        view.findViewById<ImageButton>(R.id.backButton4).setOnClickListener { dialog.dismiss() }

        view.findViewById<Button>(R.id.btnDeleteTransaction).setOnClickListener {
            dbHelper.deleteExpense(expense.id)
            updateTransactionList()
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
