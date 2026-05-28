package com.budgetbuddy.ui.gamification

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.budgetbuddy.databinding.ItemBadgeBinding

data class BadgeItem(
    val icon: String,
    val name: String,
    val description: String,   // how to earn it
    val earnedOn: String?,     // null = not yet earned (locked)
) {
    val isEarned: Boolean get() = earnedOn != null
}

class BadgeAdapter(private val items: List<BadgeItem>) :
    RecyclerView.Adapter<BadgeAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: ItemBadgeBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: BadgeItem) {
            binding.tvBadgeIcon.text = item.icon
            binding.tvBadgeName.text = item.name
            binding.tvBadgeDescription.text = item.description
            if (item.isEarned) {
                binding.tvBadgeDate.text = item.earnedOn
                binding.tvBadgeDate.setTextColor(
                    androidx.core.content.ContextCompat.getColor(
                        binding.root.context, com.budgetbuddy.R.color.income_green
                    )
                )
                binding.tvBadgeIcon.alpha = 1f
                binding.tvBadgeName.alpha = 1f
            } else {
                binding.tvBadgeDate.text = "Locked"
                binding.tvBadgeDate.setTextColor(
                    androidx.core.content.ContextCompat.getColor(
                        binding.root.context, com.budgetbuddy.R.color.text_secondary_dark
                    )
                )
                // Dim locked badges so earned ones stand out
                binding.tvBadgeIcon.alpha = 0.35f
                binding.tvBadgeName.alpha = 0.5f
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemBadgeBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(items[position])
    override fun getItemCount() = items.size
}
