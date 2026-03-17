package com.budgetbuddy.ui.expense

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.budgetbuddy.data.local.entities.TransactionEntity
import com.budgetbuddy.data.local.entities.TransactionType
import com.budgetbuddy.databinding.ItemTransactionBinding
import com.budgetbuddy.util.DateUtils

data class TransactionWithCategory(
    val transaction: TransactionEntity,
    val categoryName: String,
    val categoryIcon: String
)

class TransactionAdapter(
    private val onItemClick: ((TransactionWithCategory) -> Unit)? = null
) : ListAdapter<TransactionWithCategory, TransactionAdapter.ViewHolder>(DIFF) {

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<TransactionWithCategory>() {
            override fun areItemsTheSame(a: TransactionWithCategory, b: TransactionWithCategory) =
                a.transaction.id == b.transaction.id
            override fun areContentsTheSame(a: TransactionWithCategory, b: TransactionWithCategory) = a == b
        }
    }

    inner class ViewHolder(private val binding: ItemTransactionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: TransactionWithCategory) {
            binding.tvCategoryIcon.text = item.categoryIcon
            binding.tvCategoryName.text = item.categoryName
            binding.tvNotes.text = item.transaction.notes ?: ""
            binding.tvDate.text = DateUtils.formatDate(item.transaction.date)
            val sign = if (item.transaction.type == TransactionType.INCOME) "+" else "-"
            binding.tvAmount.text = "$sign R %.2f".format(item.transaction.amount)
            binding.tvAmount.setTextColor(
                if (item.transaction.type == TransactionType.INCOME)
                    itemView.context.getColor(com.budgetbuddy.R.color.income_green)
                else
                    itemView.context.getColor(com.budgetbuddy.R.color.expense_red)
            )
            binding.root.setOnClickListener { onItemClick?.invoke(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemTransactionBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(getItem(position))
}
