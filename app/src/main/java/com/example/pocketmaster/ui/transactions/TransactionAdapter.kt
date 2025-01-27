package com.example.pocketmaster.ui.transactions

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.pocketmaster.data.model.Transaction
import com.example.pocketmaster.data.model.TransactionType
import com.example.pocketmaster.databinding.ItemTransactionBinding
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TransactionAdapter(
    private val onItemClick: (Transaction) -> Unit,
    private val onDeleteClick: (Transaction) -> Unit
) : ListAdapter<Transaction, TransactionAdapter.TransactionViewHolder>(TransactionDiffCallback()) {
//ListAdapter automates diffing between old and new lists using DiffUtil and helps refresh only the necessary items.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val binding = ItemTransactionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TransactionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class TransactionViewHolder(
        private val binding: ItemTransactionBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        private val dateFormatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        private val currencyFormatter = NumberFormat.getCurrencyInstance()

        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(getItem(position))
                }
            }


        }

        fun bind(transaction: Transaction) {
            binding.apply {
                tvDescription.text = transaction.description
                tvCategory.text = transaction.category
                tvDate.text = dateFormatter.format(Date(transaction.date))

                // Format amount with currency symbol and color based on transaction type
                val amount = currencyFormatter.format(transaction.amount)
                tvAmount.text = when (transaction.type) {
                    TransactionType.INCOME -> "+$amount"
                    TransactionType.EXPENSE -> "-$amount"
                }

                // Set text color based on transaction type
                tvAmount.setTextColor(
                    tvAmount.context.getColor(
                        when (transaction.type) {
                            TransactionType.INCOME -> android.R.color.holo_green_dark
                            TransactionType.EXPENSE -> android.R.color.holo_red_dark
                        }
                    )
                )
            }
        }
    }
}

private class TransactionDiffCallback : DiffUtil.ItemCallback<Transaction>() {
    override fun areItemsTheSame(oldItem: Transaction, newItem: Transaction): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Transaction, newItem: Transaction): Boolean {
        return oldItem == newItem
    }
}