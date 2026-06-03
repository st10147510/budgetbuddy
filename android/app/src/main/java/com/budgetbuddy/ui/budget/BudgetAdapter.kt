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

            val (statusColorRes, chipColorRes, chipLabel) = when (item.status) {
                BudgetStatus.OK        -> Triple(R.color.green_ok,      R.color.chip_ok,      "On Track")
                BudgetStatus.WARNING   -> Triple(R.color.amber_warning, R.color.chip_warning, "Near Limit")
                BudgetStatus.EXCEEDED  -> Triple(R.color.red_danger,    R.color.chip_danger,  "Over Limit")
                BudgetStatus.UNDER_MIN -> Triple(R.color.blue_info,     R.color.chip_info,    "Below Goal")
            }

            val statusColor = ContextCompat.getColor(ctx, statusColorRes)
            binding.tvPercent.setTextColor(statusColor)
            binding.progressBudget.progressTintList = android.content.res.ColorStateList.valueOf(statusColor)

            binding.tvComplianceChip.text = chipLabel
            binding.tvComplianceChip.background.setTint(ContextCompat.getColor(ctx, chipColorRes))

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
