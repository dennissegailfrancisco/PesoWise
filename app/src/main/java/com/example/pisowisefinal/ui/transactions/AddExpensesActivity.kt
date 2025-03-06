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

    // Category lists
    private val incomeCategories = arrayOf("Salary", "Freelancing", "Investments", "Gifts", "Other Income")
    private val expenseCategories = arrayOf("Food", "Shopping", "Rent", "Transport", "Bills", "Entertainment", "Other Expense")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_addexpenses)

        dbHelper = DatabaseHelper(this)

        // Initialize views
        spinnerTransactionType = findViewById(R.id.spinnerTransactionType)
        spinnerCategory = findViewById(R.id.spinnerCategory)
        editDate = findViewById(R.id.editDate)
        editAmount = findViewById(R.id.editAmount)
        editTitle = findViewById(R.id.editTitle)
        editMessage = findViewById(R.id.editMessage)
        btnSaveTransaction = findViewById(R.id.btnSaveTransaction)
        backButton = findViewById(R.id.backButton)

        // Setup Transaction Type Spinner
        ArrayAdapter.createFromResource(
            this,
            R.array.transaction_types_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerTransactionType.adapter = adapter
        }

        // Setup Category Spinner with default values
        updateCategorySpinner("Income")

        // Listen for Transaction Type selection change
        spinnerTransactionType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedType = parent?.getItemAtPosition(position).toString()
                updateCategorySpinner(selectedType)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }


        // Date Picker - Prevents selecting future dates
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        editDate.apply {
            isFocusable = false
            isClickable = true
            setOnClickListener {
                DatePickerDialog(
                    this@AddExpensesActivity,
                    { _, year, month, day ->
                        val selectedCalendar = Calendar.getInstance()
                        selectedCalendar.set(year, month, day)

                        // Prevent future dates
                        if (selectedCalendar.after(calendar)) {
                            Toast.makeText(this@AddExpensesActivity, "You can't select future dates!", Toast.LENGTH_SHORT).show()
                        } else {
                            setText(dateFormat.format(selectedCalendar.time))
                        }
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                ).show()
            }
        }

        // Save Transaction
        btnSaveTransaction.setOnClickListener {
            val transactionType = spinnerTransactionType.selectedItem.toString()
            val category = spinnerCategory.selectedItem?.toString() ?: ""
            val amountText = editAmount.text.toString()
            val date = editDate.text.toString()
            val title = editTitle.text.toString()
            val message = editMessage.text.toString()

            // Validate fields
            if (category.isEmpty() || date.isEmpty() || title.isEmpty() || amountText.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Validate amount (only numbers & decimals allowed)
            val amount = amountText.toDoubleOrNull()
            if (amount == null || amount <= 0) {
                Toast.makeText(this, "Enter a valid amount", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Save to database
            val expense = Expense(0, category, date, amount, transactionType, title, message)
            dbHelper.addExpense(expense)
            Toast.makeText(this, "Transaction Added!", Toast.LENGTH_SHORT).show()

            // Navigate back to MainActivity
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            finish()
        }

        // Handle Back Button Click
        backButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP // Ensures no duplicate activities
            startActivity(intent)
            finish() // Close current activity
        }
    }

    // Function to update category spinner based on transaction type
    private fun updateCategorySpinner(transactionType: String) {
        val categories = if (transactionType == "Income") incomeCategories else expenseCategories
        val categoryAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategory.adapter = categoryAdapter
    }
}
