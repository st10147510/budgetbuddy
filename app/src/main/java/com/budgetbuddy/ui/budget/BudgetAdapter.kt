package com.budgetbuddy.ui.budget

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.budgetbuddy.R
import com.budgetbuddy.databinding.ItemBudgetBinding

class BudgetAdapter(
    private val onDelete: (BudgetWithSpend) -> Unit
) : ListAdapter<BudgetWithSpend, BudgetAdapter.ViewHolder>(DIFF) {

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<BudgetWithSpend>() {
            override fun areItemsTheSame(a: BudgetWithSpend, b: BudgetWithSpend) = a.budget.id == b.budget.id
            override fun areContentsTheSame(a: BudgetWithSpend, b: BudgetWithSpend) = a == b
        }
    }

    inner class ViewHolder(private val binding: ItemBudgetBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: BudgetWithSpend) {
            binding.tvCategoryIcon.text = item.category.icon
            binding.tvCategoryName.text = item.category.name
            binding.tvSpent.text = "R %.2f".format(item.spent)
            binding.tvLimit.text = "R %.2f".format(item.budget.limitAmount)
            binding.tvPercent.text = "${item.progressPercent}%"
            binding.progressBudget.progress = item.progressPercent.coerceIn(0, 100)

            val color = when (item.status) {
                BudgetStatus.OK -> itemView.context.getColor(R.color.green_ok)
                BudgetStatus.WARNING -> itemView.context.getColor(R.color.amber_warning)
                BudgetStatus.EXCEEDED -> itemView.context.getColor(R.color.red_danger)
            }
            binding.tvPercent.setTextColor(color)
            binding.progressBudget.progressTintList = android.content.res.ColorStateList.valueOf(color)

            binding.btnDelete.setOnClickListener { onDelete(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemBudgetBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(getItem(position))
}
