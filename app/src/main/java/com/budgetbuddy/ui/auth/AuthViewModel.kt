package com.budgetbuddy.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.budgetbuddy.data.repository.AuthRepository
import com.budgetbuddy.data.repository.AuthResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    object Success : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    val isLoggedIn: Boolean get() = authRepository.isLoggedIn
    val currentUserId: String get() = authRepository.currentUser?.uid ?: ""

    fun signIn(email: String, password: String) {
        if (!validateSignIn(email, password)) return
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            _uiState.value = when (val result = authRepository.signIn(email, password)) {
                is AuthResult.Success -> AuthUiState.Success
                is AuthResult.Error -> AuthUiState.Error(result.message)
            }
        }
    }

    fun signUp(email: String, password: String, displayName: String) {
        if (!validateSignUp(email, password, displayName)) return
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            _uiState.value = when (val result = authRepository.signUp(email, password, displayName)) {
                is AuthResult.Success -> AuthUiState.Success
                is AuthResult.Error -> AuthUiState.Error(result.message)
            }
        }
    }

    fun sendPasswordReset(email: String) {
        if (email.isBlank()) {
            _uiState.value = AuthUiState.Error("Please enter your email address")
            return
        }
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            authRepository.sendPasswordReset(email).fold(
                onSuccess = { _uiState.value = AuthUiState.Success },
                onFailure = { _uiState.value = AuthUiState.Error(it.message ?: "Reset failed") }
            )
        }
    }

    fun signOut() = authRepository.signOut()

    fun resetState() { _uiState.value = AuthUiState.Idle }

    private fun validateSignIn(email: String, password: String): Boolean {
        return when {
            email.isBlank() -> { _uiState.value = AuthUiState.Error("Please enter your email address"); false }
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> { _uiState.value = AuthUiState.Error("Please enter a valid email address"); false }
            password.isBlank() -> { _uiState.value = AuthUiState.Error("Please enter your password"); false }
            else -> true
        }
    }

    private fun validateSignUp(email: String, password: String, name: String): Boolean {
        return when {
            name.isBlank() -> { _uiState.value = AuthUiState.Error("Please enter your full name"); false }
            email.isBlank() -> { _uiState.value = AuthUiState.Error("Please enter your email address"); false }
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> { _uiState.value = AuthUiState.Error("Please enter a valid email address"); false }
            password.length < 8 -> { _uiState.value = AuthUiState.Error("Password must be at least 8 characters"); false }
            else -> true
        }
    }
}
