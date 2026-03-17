package com.budgetbuddy.ui.category

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.budgetbuddy.data.local.entities.CategoryEntity
import com.budgetbuddy.data.repository.CategoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CategoryViewModel @Inject constructor(
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    val categories: StateFlow<List<CategoryEntity>> = categoryRepository.getAllCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addCategory(name: String, icon: String, colorHex: String = "#607D8B") {
        viewModelScope.launch {
            categoryRepository.insertCategory(
                CategoryEntity(name = name, icon = icon, colorHex = colorHex, isDefault = false)
            )
        }
    }

    fun deleteCategory(category: CategoryEntity) {
        viewModelScope.launch { categoryRepository.deleteCategory(category) }
    }
}
