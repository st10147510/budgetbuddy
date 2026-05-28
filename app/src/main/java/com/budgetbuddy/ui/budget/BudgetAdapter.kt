package com.budgetbuddy.ui.budget

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.budgetbuddy.R
import com.budgetbuddy.databinding.ItemBudgetBinding

class BudgetAdapter(
    private val onItemClick: (BudgetWithSpend) -> Unit,
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

            val ctx = itemView.context
            val (statusColor, chipLabel) = when (item.status) {
                BudgetStatus.OK       -> ContextCompat.getColor(ctx, R.color.green_ok)   to "On Track"
                BudgetStatus.WARNING  -> ContextCompat.getColor(ctx, R.color.amber_warning) to "Near Limit"
                BudgetStatus.EXCEEDED -> ContextCompat.getColor(ctx, R.color.red_danger)  to "Over Limit"
                BudgetStatus.UNDER_MIN -> ContextCompat.getColor(ctx, R.color.blue_info)  to "Below Goal"
            }

            binding.tvPercent.setTextColor(statusColor)
            binding.progressBudget.progressTintList = android.content.res.ColorStateList.valueOf(statusColor)

            binding.tvComplianceChip.text = chipLabel
            binding.tvComplianceChip.background.setTint(statusColor)

            if (item.budget.minAmount > 0) {
                binding.tvMinGoal.visibility = View.VISIBLE
                binding.tvMinGoal.text = "Min goal: R %.2f".format(item.budget.minAmount)
            } else {
                binding.tvMinGoal.visibility = View.GONE
            }

            binding.root.setOnClickListener { onItemClick(item) }
            binding.btnDelete.setOnClickListener { onDelete(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemBudgetBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(getItem(position))
}
