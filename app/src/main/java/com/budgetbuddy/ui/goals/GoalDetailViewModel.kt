package com.budgetbuddy.ui.goals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.budgetbuddy.data.local.entities.GoalEntity
import com.budgetbuddy.data.repository.GoalRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GoalDetailViewModel @Inject constructor(
    private val goalRepository: GoalRepository
) : ViewModel() {

    private val _goal = MutableStateFlow<GoalEntity?>(null)
    val goal: StateFlow<GoalEntity?> = _goal.asStateFlow()

    private val _finished = MutableStateFlow(false)
    val finished: StateFlow<Boolean> = _finished.asStateFlow()

    fun load(id: Long) {
        viewModelScope.launch { _goal.value = goalRepository.getGoalById(id) }
    }

    fun update(name: String, targetAmount: Double, targetDate: Long?) {
        val current = _goal.value ?: return
        viewModelScope.launch {
            val updated = current.copy(
                name = name,
                targetAmount = targetAmount,
                targetDate = targetDate,
                isCompleted = current.savedAmount >= targetAmount
            )
            goalRepository.updateGoal(updated)
            _goal.value = updated
        }
    }

    fun addSavings(amount: Double) {
        val current = _goal.value ?: return
        viewModelScope.launch {
            val newSaved = (current.savedAmount + amount).coerceAtMost(current.targetAmount)
            val updated = current.copy(savedAmount = newSaved, isCompleted = newSaved >= current.targetAmount)
            goalRepository.updateGoal(updated)
            _goal.value = updated
        }
    }

    fun delete() {
        val current = _goal.value ?: return
        viewModelScope.launch {
            goalRepository.deleteGoal(current)
            _finished.value = true
        }
    }
}
