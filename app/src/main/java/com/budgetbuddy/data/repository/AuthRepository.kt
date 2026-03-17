package com.budgetbuddy.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

sealed class AuthResult {
    data class Success(val user: FirebaseUser) : AuthResult()
    data class Error(val message: String) : AuthResult()
}

@Singleton
class AuthRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) {
    val currentUser: FirebaseUser? get() = firebaseAuth.currentUser
    val isLoggedIn: Boolean get() = currentUser != null

    suspend fun signIn(email: String, password: String): AuthResult = try {
        val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
        result.user?.let { AuthResult.Success(it) } ?: AuthResult.Error("Sign in failed")
    } catch (e: Exception) {
        AuthResult.Error(e.message ?: "Sign in failed")
    }

    suspend fun signUp(email: String, password: String, displayName: String): AuthResult = try {
        val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
        result.user?.let { user ->
            val profileUpdate = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                .setDisplayName(displayName).build()
            user.updateProfile(profileUpdate).await()
            AuthResult.Success(user)
        } ?: AuthResult.Error("Registration failed")
    } catch (e: Exception) {
        AuthResult.Error(e.message ?: "Registration failed")
    }

    suspend fun sendPasswordReset(email: String): Result<Unit> = try {
        firebaseAuth.sendPasswordResetEmail(email).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    fun signOut() = firebaseAuth.signOut()
}
