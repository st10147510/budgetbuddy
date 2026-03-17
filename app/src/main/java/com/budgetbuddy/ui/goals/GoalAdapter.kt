package com.budgetbuddy.ui.goals

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.budgetbuddy.data.local.entities.GoalEntity
import com.budgetbuddy.databinding.ItemGoalBinding

class GoalAdapter(
    private val onAddSavings: (GoalEntity) -> Unit,
    private val onDelete: (GoalEntity) -> Unit
) : ListAdapter<GoalEntity, GoalAdapter.ViewHolder>(DIFF) {

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<GoalEntity>() {
            override fun areItemsTheSame(a: GoalEntity, b: GoalEntity) = a.id == b.id
            override fun areContentsTheSame(a: GoalEntity, b: GoalEntity) = a == b
        }
    }

    inner class ViewHolder(private val binding: ItemGoalBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: GoalEntity) {
            binding.tvGoalName.text = item.name
            val pct = item.progressPercent
            binding.tvPercent.text = "$pct%"
            binding.progressGoal.progress = pct.coerceIn(0, 100)
            binding.tvSaved.text = "Saved: R %.2f".format(item.savedAmount)
            binding.tvTarget.text = "Target: R %.2f".format(item.targetAmount)
            binding.root.setOnClickListener { onAddSavings(item) }
            binding.btnDelete.setOnClickListener { onDelete(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemGoalBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(getItem(position))
}
