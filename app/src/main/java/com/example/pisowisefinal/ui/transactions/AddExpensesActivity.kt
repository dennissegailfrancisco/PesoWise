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

    private lateinit var incomeCategories: Array<String>
    private lateinit var expenseCategories: Array<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_addexpenses)

        initUI()
        setupSpinners()
        setupDatePicker()
        setupSaveButton()
        setupBackButton()
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

        incomeCategories = resources.getStringArray(R.array.income_categories)
        expenseCategories = resources.getStringArray(R.array.expense_categories)
    }

    private fun setupSpinners() {
        val transactionTypes = resources.getStringArray(R.array.transaction_types_array)

        ArrayAdapter(this, android.R.layout.simple_spinner_item, transactionTypes).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerTransactionType.adapter = this
        }

        updateCategorySpinner("Income")

        spinnerTransactionType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedType = parent?.getItemAtPosition(position).toString()
                updateCategorySpinner(selectedType)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupDatePicker() {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        editDate.apply {
            isFocusable = false
            isClickable = true
            setOnClickListener {
                val todayCalendar = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }

                DatePickerDialog(
                    this@AddExpensesActivity,
                    { _, year, month, day ->
                        val selectedCalendar = Calendar.getInstance().apply {
                            set(year, month, day)
                            set(Calendar.HOUR_OF_DAY, 0)
                            set(Calendar.MINUTE, 0)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }

                        if (selectedCalendar.after(todayCalendar)) {
                            Toast.makeText(this@AddExpensesActivity, "You can't select future dates!", Toast.LENGTH_SHORT).show()
                        } else {
                            setText(dateFormat.format(selectedCalendar.time))
                        }
                    },
                    todayCalendar.get(Calendar.YEAR),
                    todayCalendar.get(Calendar.MONTH),
                    todayCalendar.get(Calendar.DAY_OF_MONTH)
                ).show()
            }
        }
    }

    private fun setupSaveButton() {
        btnSaveTransaction.setOnClickListener {
            val transactionType = spinnerTransactionType.selectedItem.toString()
            val category = spinnerCategory.selectedItem?.toString() ?: ""
            val amountText = editAmount.text.toString()
            val date = editDate.text.toString()
            val title = editTitle.text.toString()
            val message = editMessage.text.toString()

            if (category.isEmpty() || date.isEmpty() || title.isEmpty() || amountText.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val amount = amountText.toDoubleOrNull()
            if (amount == null || amount <= 0) {
                Toast.makeText(this, "Enter a valid amount", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val expense = Expense(0, category, date, amount, transactionType, title, message)
            dbHelper.addExpense(expense)

            Toast.makeText(this, "Transaction Added!", Toast.LENGTH_SHORT).show()

            val intent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            startActivity(intent)
            finish()
        }
    }

    private fun setupBackButton() {
        backButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP // Ensures no duplicate activities
            }
            startActivity(intent)
            finish()
        }
    }

    private fun updateCategorySpinner(transactionType: String) {
        val categories = if (transactionType == "Income") incomeCategories else expenseCategories
        val categoryAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        spinnerCategory.adapter = categoryAdapter
    }
}
