package com.budgetbuddy.ui.goals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.budgetbuddy.data.local.entities.GoalEntity
import com.budgetbuddy.data.repository.GoalRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GoalsViewModel @Inject constructor(
    private val goalRepository: GoalRepository
) : ViewModel() {

    private val _goals = MutableStateFlow<List<GoalEntity>>(emptyList())
    val goals: StateFlow<List<GoalEntity>> = _goals.asStateFlow()

    fun loadGoals(userId: String) {
        viewModelScope.launch {
            goalRepository.getActiveGoals(userId).collect { _goals.value = it }
        }
    }

    fun saveGoal(userId: String, name: String, targetAmount: Double, savedAmount: Double = 0.0) {
        viewModelScope.launch {
            goalRepository.insertGoal(
                GoalEntity(
                    userId = userId,
                    name = name,
                    targetAmount = targetAmount,
                    savedAmount = savedAmount
                )
            )
        }
    }

    fun updateSaved(goal: GoalEntity, additionalAmount: Double) {
        viewModelScope.launch {
            val updated = goal.copy(
                savedAmount = (goal.savedAmount + additionalAmount).coerceAtMost(goal.targetAmount),
                isCompleted = (goal.savedAmount + additionalAmount) >= goal.targetAmount
            )
            goalRepository.updateGoal(updated)
        }
    }

    fun deleteGoal(goal: GoalEntity) {
        viewModelScope.launch { goalRepository.deleteGoal(goal) }
    }
}
