package com.example.pisowisefinal.adapters

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.pisowisefinal.R
import com.example.pisowisefinal.database.DatabaseHelper
import com.example.pisowisefinal.models.Expense
import com.example.pisowisefinal.utils.Constants
import kotlin.math.abs

class TransactionAdapter(
    private var expenses: MutableList<Expense>,
    private val dbHelper: DatabaseHelper,
    private val onItemClick: (Expense) -> Unit
) : RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {


    private val categoryIcons = mapOf(
        "Food" to R.drawable.foodicon,
        "Shopping" to R.drawable.shopping,
        "Grocery" to R.drawable.grocery,
        "Rent" to R.drawable.rent,
        "Transport" to R.drawable.transpo,
        "Bills" to R.drawable.bills,
        "Entertainment" to R.drawable.entertainment,
        "Other Expense" to R.drawable.other_exp,

        "Salary" to R.drawable.salary,
        "Freelancing" to R.drawable.freelancing,
        "Investments" to R.drawable.investment,
        "Gifts" to R.drawable.gift,
        "Other income" to R.drawable.other_income,


        )

    class TransactionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val icon: ImageView = view.findViewById(R.id.icon)
        val category: TextView = view.findViewById(R.id.textCategory)
        val date: TextView = view.findViewById(R.id.textDate)
        val type: TextView = view.findViewById(R.id.textType)
        val amount: TextView = view.findViewById(R.id.textAmount)
        val deleteButton: Button = view.findViewById(R.id.btnDeleteTransaction)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_transaction, parent, false)
        return TransactionViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val expense = expenses[position]

        // Set transaction data
        holder.category.text = expense.category
        holder.date.text = expense.date
        holder.type.text = expense.transactionType

        // Format amount with sign for expense or income
        val formattedAmount = String.format("%.2f", abs(expense.amount))
        holder.amount.text = if (expense.transactionType == Constants.TYPE_EXPENSE) {
            "- ₱$formattedAmount"
        } else {
            "+ ₱$formattedAmount"
        }

        // Set the category icon dynamically
        val iconResource = categoryIcons[expense.category] ?: R.drawable.ic_placeholder
        holder.icon.setImageResource(iconResource)

        // Handle item click
        holder.itemView.setOnClickListener { onItemClick(expense) }

        // Handle delete button click
        holder.deleteButton.setOnClickListener {
            showDeleteDialog(holder.itemView.context, position)
        }
    }

    override fun getItemCount(): Int = expenses.size

    private fun showDeleteDialog(context: Context, position: Int) {
        AlertDialog.Builder(context).apply {
            setTitle(Constants.ALERT_DIALOG_TITLE)
            setMessage(Constants.ALERT_DIALOG_MESSAGE)
            setPositiveButton(Constants.ALERT_DIALOG_POSITIVE) { _, _ ->
                deleteItem(position)
            }
            setNegativeButton(Constants.ALERT_DIALOG_NEGATIVE, null)
            show()
        }
    }

    private fun deleteItem(position: Int) {
        val expense = expenses[position]
        if (expense.id > 0 && dbHelper.deleteExpense(expense.id)) {
            expenses.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, expenses.size)
        }
    }

    fun updateList(newList: List<Expense>) {
        expenses.clear()
        expenses.addAll(newList)
        notifyDataSetChanged()
    }
}
