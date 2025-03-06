package com.example.pisowisefinal.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.pisowisefinal.R
import com.example.pisowisefinal.models.Expense

class TransactionAdapter(private var expenses: MutableList<Expense>)
    : RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {


    class TransactionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val icon: ImageView = view.findViewById(R.id.icon)
        val category: TextView = view.findViewById(R.id.textCategory)
        val date: TextView = view.findViewById(R.id.textDate)
        val type: TextView = view.findViewById(R.id.textType)
        val amount: TextView = view.findViewById(R.id.textAmount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_transaction, parent, false)
        return TransactionViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val expense = expenses[position]

        holder.category.text = expense.category
        holder.date.text = expense.date
        holder.type.text = expense.transactionType

        val formattedAmount = String.format("%.2f", Math.abs(expense.amount))
        holder.amount.text = if (expense.transactionType == "Expense") "- ₱$formattedAmount"
        else "+ ₱$formattedAmount"
    }

    override fun getItemCount(): Int = expenses.size

    fun updateList(newList: List<Expense>) {
        expenses.clear()
        expenses.addAll(newList.toMutableList())
        notifyDataSetChanged()
    }

}
