package com.budgetbuddy.ui.category

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.budgetbuddy.data.local.entities.CategoryEntity
import com.budgetbuddy.databinding.ItemCategoryBinding

class CategoryAdapter(
    private val onDelete: (CategoryEntity) -> Unit
) : ListAdapter<CategoryEntity, CategoryAdapter.ViewHolder>(DIFF) {

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<CategoryEntity>() {
            override fun areItemsTheSame(a: CategoryEntity, b: CategoryEntity) = a.id == b.id
            override fun areContentsTheSame(a: CategoryEntity, b: CategoryEntity) = a == b
        }
    }

    inner class ViewHolder(private val binding: ItemCategoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: CategoryEntity) {
            binding.tvIcon.text = item.icon
            binding.tvName.text = item.name
            binding.btnDelete.visibility = if (item.isDefault) View.GONE else View.VISIBLE
            binding.btnDelete.setOnClickListener { onDelete(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemCategoryBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(getItem(position))
}
