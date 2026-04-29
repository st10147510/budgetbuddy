package com.budgetbuddy.ui.auth

import android.util.Log
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

    // 0 = no input, 1 = weak, 2 = medium, 3 = strong
    private val _passwordStrength = MutableStateFlow(0)
    val passwordStrength: StateFlow<Int> = _passwordStrength.asStateFlow()

    val isLoggedIn: Boolean get() = authRepository.isLoggedIn
    val currentUserId: String get() = authRepository.currentUserId ?: ""

    fun signIn(email: String, password: String) {
        if (!validateSignIn(email, password)) return
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            _uiState.value = when (val result = authRepository.signIn(email, password)) {
                is AuthResult.Success -> AuthUiState.Success
                is AuthResult.Error   -> AuthUiState.Error(result.message)
            }
        }
    }

    /**
     * Evaluates password strength in real time and updates [passwordStrength]:
     * 0 = empty, 1 = weak, 2 = medium, 3 = strong.
     * Called on every keystroke from the Fragment's TextWatcher.
     */
    fun evaluatePasswordStrength(password: String) {
        if (password.isEmpty()) { _passwordStrength.value = 0; return }
        var score = 0
        if (password.length >= 8) score++                              // length criteria
        if (password.any { it.isUpperCase() } && password.any { it.isLowerCase() } && password.any { it.isDigit() }) score++ // complexity
        if (password.any { !it.isLetterOrDigit() }) score++            // special char
        _passwordStrength.value = score
        Log.d("AuthViewModel", "Password strength score: $score")
    }

    fun signUp(email: String, password: String, confirmPassword: String, displayName: String) {
        if (!validateSignUp(email, password, confirmPassword, displayName)) return
        Log.d("AuthViewModel", "signUp: attempting registration for $email")
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            _uiState.value = when (val result = authRepository.signUp(email, password, displayName)) {
                is AuthResult.Success -> { Log.i("AuthViewModel", "signUp: success"); AuthUiState.Success }
                is AuthResult.Error   -> { Log.w("AuthViewModel", "signUp: error - ${result.message}"); AuthUiState.Error(result.message) }
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
            email.isBlank()    -> { _uiState.value = AuthUiState.Error("Please enter your email address"); false }
            !isValidEmail(email) -> { _uiState.value = AuthUiState.Error("Please enter a valid email address"); false }
            password.isBlank() -> { _uiState.value = AuthUiState.Error("Please enter your password"); false }
            else -> true
        }
    }

    /**
     * Validates all sign-up fields including:
     * - Non-blank name and valid email
     * - Password strength: min 8 chars, upper + lower + digit + special character
     * - Confirm password must match password exactly
     */
    private fun validateSignUp(email: String, password: String, confirmPassword: String, name: String): Boolean {
        val hasUpper   = password.any { it.isUpperCase() }
        val hasLower   = password.any { it.isLowerCase() }
        val hasDigit   = password.any { it.isDigit() }
        val hasSpecial = password.any { !it.isLetterOrDigit() }
        return when {
            name.isBlank()        -> { _uiState.value = AuthUiState.Error("Please enter your full name"); false }
            email.isBlank()       -> { _uiState.value = AuthUiState.Error("Please enter your email address"); false }
            !isValidEmail(email)  -> { _uiState.value = AuthUiState.Error("Please enter a valid email address"); false }
            password.length < 8   -> { _uiState.value = AuthUiState.Error("Password must be at least 8 characters"); false }
            !hasUpper             -> { _uiState.value = AuthUiState.Error("Password must contain at least one uppercase letter"); false }
            !hasLower             -> { _uiState.value = AuthUiState.Error("Password must contain at least one lowercase letter"); false }
            !hasDigit             -> { _uiState.value = AuthUiState.Error("Password must contain at least one number"); false }
            !hasSpecial           -> { _uiState.value = AuthUiState.Error("Password must contain at least one special character (e.g. @#\$%)"); false }
            password != confirmPassword -> { _uiState.value = AuthUiState.Error("Passwords do not match"); false }
            else -> true
        }
    }

    private fun isValidEmail(email: String): Boolean =
        Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$").matches(email)
}
