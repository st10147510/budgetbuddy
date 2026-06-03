package com.budgetbuddy.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.budgetbuddy.data.local.SessionManager
import com.budgetbuddy.data.repository.PolicyRepository
import com.budgetbuddy.data.repository.PolicyVersions
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class PolicyUiState {
    object Loading : PolicyUiState()
    data class NeedsAcceptance(val versions: PolicyVersions) : PolicyUiState()
    object AlreadyAccepted : PolicyUiState()
    object Accepted : PolicyUiState()
    data class Error(val message: String) : PolicyUiState()
}

@HiltViewModel
class PolicyAcceptanceViewModel @Inject constructor(
    private val policyRepository: PolicyRepository,
    private val sessionManager: SessionManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow<PolicyUiState>(PolicyUiState.Loading)
    val uiState: StateFlow<PolicyUiState> = _uiState

    init {
        checkPolicies()
    }

    private fun checkPolicies() {
        val uid = sessionManager.userId ?: return
        viewModelScope.launch {
            _uiState.value = PolicyUiState.Loading
            try {
                if (policyRepository.hasUserAcceptedAll(uid)) {
                    _uiState.value = PolicyUiState.AlreadyAccepted
                } else {
                    val versions = policyRepository.getCurrentVersions()
                    _uiState.value = PolicyUiState.NeedsAcceptance(versions)
                }
            } catch (e: Exception) {
                _uiState.value = PolicyUiState.Error(e.message ?: "Failed to load policies")
            }
        }
    }

    fun acceptPolicies() {
        val uid = sessionManager.userId ?: return
        viewModelScope.launch {
            _uiState.value = PolicyUiState.Loading
            try {
                policyRepository.recordAllAcceptances(uid)
                _uiState.value = PolicyUiState.Accepted
            } catch (e: Exception) {
                _uiState.value = PolicyUiState.Error("Failed to record acceptance. Please try again.")
            }
        }
    }
}
