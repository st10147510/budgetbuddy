package com.budgetbuddy.ui.gamification

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.budgetbuddy.databinding.ItemBadgeBinding

data class BadgeItem(val icon: String, val name: String, val earnedOn: String)

class BadgeAdapter(private val items: List<BadgeItem>) :
    RecyclerView.Adapter<BadgeAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: ItemBadgeBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: BadgeItem) {
            binding.tvBadgeIcon.text = item.icon
            binding.tvBadgeName.text = item.name
            binding.tvBadgeDate.text = item.earnedOn
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemBadgeBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(items[position])
    override fun getItemCount() = items.size
}
