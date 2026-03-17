package com.budgetbuddy.data.repository

import com.budgetbuddy.data.local.SessionManager
import com.budgetbuddy.data.local.dao.UserDao
import com.budgetbuddy.data.local.entities.UserEntity
import com.budgetbuddy.util.PasswordUtils
import javax.inject.Inject
import javax.inject.Singleton

sealed class AuthResult {
    data class Success(val userId: String, val displayName: String, val email: String) : AuthResult()
    data class Error(val message: String) : AuthResult()
}

@Singleton
class AuthRepository @Inject constructor(
    private val userDao: UserDao,
    private val sessionManager: SessionManager
) {
    val isLoggedIn: Boolean get() = sessionManager.isLoggedIn
    val currentUserId: String? get() = sessionManager.userId
    val currentDisplayName: String? get() = sessionManager.displayName
    val currentEmail: String? get() = sessionManager.email

    suspend fun signIn(email: String, password: String): AuthResult {
        val user = userDao.findByEmail(email.trim().lowercase())
            ?: return AuthResult.Error("No account found with this email address")
        if (!PasswordUtils.verify(password, user.passwordHash))
            return AuthResult.Error("Incorrect password")
        saveSession(user)
        return AuthResult.Success(user.id.toString(), user.displayName, user.email)
    }

    suspend fun signUp(email: String, password: String, displayName: String): AuthResult {
        val normalizedEmail = email.trim().lowercase()
        if (userDao.findByEmail(normalizedEmail) != null)
            return AuthResult.Error("An account with this email already exists")
        val user = UserEntity(
            email = normalizedEmail,
            passwordHash = PasswordUtils.hash(password),
            displayName = displayName.trim()
        )
        val id = userDao.insertUser(user)
        val created = user.copy(id = id)
        saveSession(created)
        return AuthResult.Success(id.toString(), created.displayName, created.email)
    }

    suspend fun sendPasswordReset(email: String): Result<Unit> {
        // Local implementation: just verify the account exists
        return if (userDao.findByEmail(email.trim().lowercase()) != null)
            Result.success(Unit)
        else
            Result.failure(Exception("No account found with this email address"))
    }

    fun signOut() = sessionManager.clear()

    private fun saveSession(user: UserEntity) {
        sessionManager.userId = user.id.toString()
        sessionManager.displayName = user.displayName
        sessionManager.email = user.email
    }
}
