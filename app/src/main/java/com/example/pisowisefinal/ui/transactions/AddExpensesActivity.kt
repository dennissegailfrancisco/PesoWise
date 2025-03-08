package com.example.pisowisefinal.ui.transactions

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.pisowisefinal.R
import com.example.pisowisefinal.database.DatabaseHelper
import com.example.pisowisefinal.models.Expense
import com.example.pisowisefinal.ui.dashboards.MainActivity
import com.example.pisowisefinal.utils.Constants
import java.text.SimpleDateFormat
import java.util.*

class AddExpensesActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var spinnerTransactionType: Spinner
    private lateinit var spinnerCategory: Spinner
    private lateinit var editDate: EditText
    private lateinit var editAmount: EditText
    private lateinit var editTitle: EditText
    private lateinit var editMessage: EditText
    private lateinit var btnSaveTransaction: Button
    private lateinit var backButton: ImageButton


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_addexpenses)

        initUI()
        setupSpinners()
        setupDatePicker()
        setupButtons()
    }

    private fun initUI() {
        dbHelper = DatabaseHelper(this)

        spinnerTransactionType = findViewById(R.id.spinnerTransactionType)
        spinnerCategory = findViewById(R.id.spinnerCategory)
        editDate = findViewById(R.id.editDate)
        editAmount = findViewById(R.id.editAmount)
        editTitle = findViewById(R.id.editTitle)
        editMessage = findViewById(R.id.editMessage)
        btnSaveTransaction = findViewById(R.id.btnSaveTransaction)
        backButton = findViewById(R.id.backButton)
    }

    private fun setupSpinners() {
        val transactionTypes = resources.getStringArray(R.array.transaction_types_array)

        spinnerTransactionType.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, transactionTypes).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        updateCategorySpinner(Constants.TYPE_EXPENSE)

        spinnerTransactionType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                updateCategorySpinner(parent?.getItemAtPosition(position).toString())
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupDatePicker() {
        val dateFormat = SimpleDateFormat(Constants.DATE_FORMAT, Locale.getDefault())

        editDate.apply {
            isFocusable = false
            isClickable = true
            setOnClickListener {
                val today = Calendar.getInstance().apply { resetTime() }

                DatePickerDialog(
                    this@AddExpensesActivity,
                    { _, year, month, day ->
                        val selectedDate = Calendar.getInstance().apply {
                            set(year, month, day)
                            resetTime()
                        }

                        if (selectedDate.after(today)) {
                            showToast("You can't select future dates!")
                        } else {
                            setText(dateFormat.format(selectedDate.time))
                        }
                    },
                    today.get(Calendar.YEAR),
                    today.get(Calendar.MONTH),
                    today.get(Calendar.DAY_OF_MONTH)
                ).show()
            }
        }
    }

    private fun setupButtons() {
        btnSaveTransaction.setOnClickListener { saveTransaction() }
        backButton.setOnClickListener { navigateToMain() }
    }

    private fun saveTransaction() {
        val transactionType = spinnerTransactionType.selectedItem.toString()
        val category = spinnerCategory.selectedItem?.toString().orEmpty()
        val amountText = editAmount.text.toString()
        val date = editDate.text.toString()
        val title = editTitle.text.toString()
        val message = editMessage.text.toString()

        if (category.isEmpty() || date.isEmpty() || title.isEmpty() || amountText.isEmpty()) {
            showToast("Please fill in all fields")
            return
        }

        val amount = amountText.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            showToast("Enter a valid amount")
            return
        }

        val expense = Expense(0, category, date, amount, transactionType, title, message)
        dbHelper.addExpense(expense)

        showToast("Transaction Added!")
        navigateToMain()
    }

    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        })
        finish()
    }

    private fun updateCategorySpinner(transactionType: String) {
        val categories = if (transactionType == Constants.TYPE_INCOME) {
            resources.getStringArray(R.array.income_categories)
        } else {
            resources.getStringArray(R.array.expense_categories)
        }

        spinnerCategory.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
    }

    private fun Calendar.resetTime() {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
