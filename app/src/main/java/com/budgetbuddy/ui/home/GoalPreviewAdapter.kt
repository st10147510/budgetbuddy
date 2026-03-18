package com.budgetbuddy.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.budgetbuddy.data.local.entities.GoalEntity
import com.budgetbuddy.databinding.ItemGoalPreviewBinding

class GoalPreviewAdapter : ListAdapter<GoalEntity, GoalPreviewAdapter.VH>(DIFF) {

    inner class VH(private val b: ItemGoalPreviewBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(goal: GoalEntity) {
            b.tvGoalName.text = goal.name
            b.tvGoalAmount.text = "R %.2f".format(goal.savedAmount)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = VH(
        ItemGoalPreviewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(getItem(position))

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<GoalEntity>() {
            override fun areItemsTheSame(a: GoalEntity, b: GoalEntity) = a.id == b.id
            override fun areContentsTheSame(a: GoalEntity, b: GoalEntity) = a == b
        }
    }
}
