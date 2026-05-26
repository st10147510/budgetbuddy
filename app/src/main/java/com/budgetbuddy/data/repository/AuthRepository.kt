package com.budgetbuddy.data.repository

import com.budgetbuddy.data.local.SessionManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

sealed class AuthResult {
    data class Success(val userId: String, val displayName: String, val email: String) : AuthResult()
    data class Error(val message: String) : AuthResult()
}

@Singleton
class AuthRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val sessionManager: SessionManager
) {
    val isLoggedIn: Boolean get() = firebaseAuth.currentUser != null
    val currentUserId: String? get() = firebaseAuth.currentUser?.uid
    val currentDisplayName: String? get() = sessionManager.displayName ?: firebaseAuth.currentUser?.displayName
    val currentEmail: String? get() = firebaseAuth.currentUser?.email

    suspend fun signIn(email: String, password: String): AuthResult {
        return try {
            val result = firebaseAuth.signInWithEmailAndPassword(email.trim(), password).await()
            val user = result.user ?: return AuthResult.Error("Sign in failed. Please try again.")
            val displayName = user.displayName?.takeIf { it.isNotBlank() }
                ?: sessionManager.displayName
                ?: email.substringBefore("@")
            sessionManager.userId = user.uid
            sessionManager.displayName = displayName
            sessionManager.email = user.email ?: email
            AuthResult.Success(user.uid, displayName, user.email ?: email)
        } catch (e: Exception) {
            AuthResult.Error(friendlyError(e.message))
        }
    }

    suspend fun signUp(email: String, password: String, displayName: String): AuthResult {
        return try {
            val result = firebaseAuth.createUserWithEmailAndPassword(email.trim(), password).await()
            val user = result.user ?: return AuthResult.Error("Registration failed. Please try again.")
            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(displayName.trim())
                .build()
            user.updateProfile(profileUpdates).await()
            sessionManager.userId = user.uid
            sessionManager.displayName = displayName.trim()
            sessionManager.email = user.email ?: email
            AuthResult.Success(user.uid, displayName.trim(), user.email ?: email)
        } catch (e: Exception) {
            AuthResult.Error(friendlyError(e.message))
        }
    }

    suspend fun sendPasswordReset(email: String): Result<Unit> {
        return try {
            firebaseAuth.sendPasswordResetEmail(email.trim()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception(friendlyError(e.message)))
        }
    }

    fun signOut() {
        firebaseAuth.signOut()
        sessionManager.clear()
    }

    private fun friendlyError(message: String?): String = when {
        message == null -> "An error occurred. Please try again."
        message.contains("no user record") || message.contains("user-not-found") ->
            "No account found with this email address."
        message.contains("wrong-password") || message.contains("invalid-credential") ->
            "Incorrect email or password."
        message.contains("email-already-in-use") ->
            "An account with this email already exists."
        message.contains("weak-password") ->
            "Password is too weak. Please choose a stronger password."
        message.contains("network") || message.contains("Network") ->
            "Network error. Please check your connection."
        message.contains("too-many-requests") ->
            "Too many attempts. Please try again later."
        else -> message
    }
}
