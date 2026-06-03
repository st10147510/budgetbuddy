package com.budgetbuddy.ui.gamification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.budgetbuddy.data.local.entities.BadgeEntity
import com.budgetbuddy.data.repository.BadgeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BadgesViewModel @Inject constructor(
    private val badgeRepository: BadgeRepository
) : ViewModel() {

    private val _badges = MutableStateFlow<List<BadgeEntity>>(emptyList())
    val badges: StateFlow<List<BadgeEntity>> = _badges.asStateFlow()

    fun loadBadges(userId: String) {
        viewModelScope.launch {
            badgeRepository.getBadges(userId).collect { _badges.value = it }
        }
    }
}
