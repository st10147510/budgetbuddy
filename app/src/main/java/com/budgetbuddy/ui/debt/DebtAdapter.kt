package com.budgetbuddy.ui.debt

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.budgetbuddy.data.local.entities.DebtEntity
import com.budgetbuddy.databinding.ItemDebtBinding

class DebtAdapter(
    private val onItemClick: (DebtEntity) -> Unit,
    private val onDelete: (DebtEntity) -> Unit,
    private val onPayment: (DebtEntity) -> Unit
) : ListAdapter<DebtEntity, DebtAdapter.ViewHolder>(DIFF) {

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<DebtEntity>() {
            override fun areItemsTheSame(a: DebtEntity, b: DebtEntity) = a.id == b.id
            override fun areContentsTheSame(a: DebtEntity, b: DebtEntity) = a == b
        }
    }

    inner class ViewHolder(private val binding: ItemDebtBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: DebtEntity) {
            binding.tvDebtName.text = item.name
            binding.tvInterestRate.text = "%.1f%% interest".format(item.interestRate)
            binding.tvBalance.text = "R %.2f".format(item.balance)
            binding.tvMinPayment.text = "Min: R %.2f/mo".format(item.minimumPayment)

            // Payoff progress: (originalBalance - currentBalance) / originalBalance * 100
            val progressPct = if (item.originalBalance > 0) {
                ((item.originalBalance - item.balance) / item.originalBalance * 100)
                    .coerceIn(0.0, 100.0).toInt()
            } else 0
            binding.progressDebt.progress = progressPct
            binding.tvProgress.text = "$progressPct%"

            binding.root.setOnClickListener { onItemClick(item) }
            binding.btnDelete.setOnClickListener { onDelete(item) }
            binding.btnMakePayment.setOnClickListener { onPayment(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemDebtBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(getItem(position))
}
