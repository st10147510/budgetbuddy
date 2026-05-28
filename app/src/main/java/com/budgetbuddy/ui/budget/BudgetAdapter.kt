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
import com.budgetbuddy.util.CurrencyFormatter

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
            val ctx = binding.root.context
            binding.tvSpent.text = CurrencyFormatter.format(ctx, item.spent)
            binding.tvLimit.text = CurrencyFormatter.format(ctx, item.budget.limitAmount)
            binding.tvPercent.text = "${item.progressPercent}%"
            binding.progressBudget.progress = item.progressPercent.coerceIn(0, 100)

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
                binding.tvMinGoal.text = ctx.getString(com.budgetbuddy.R.string.min_goal_label, CurrencyFormatter.format(ctx, item.budget.minAmount))
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
