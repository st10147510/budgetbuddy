package com.budgetbuddy.ui.auth

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import app.cash.turbine.test
import com.budgetbuddy.data.repository.AuthRepository
import com.budgetbuddy.data.repository.AuthResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.*

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    @get:Rule
    val instantTaskRule = InstantTaskExecutorRule()

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var authRepository: AuthRepository
    private lateinit var viewModel: AuthViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        authRepository = mock()
        viewModel = AuthViewModel(authRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is Idle`() {
        assertEquals(AuthUiState.Idle, viewModel.uiState.value)
    }

    @Test
    fun `signIn with empty email emits Error immediately without calling repository`() = runTest {
        viewModel.uiState.test {
            skipItems(1) // skip initial Idle
            viewModel.signIn("", "password123")
            val state = awaitItem()
            assertTrue(state is AuthUiState.Error)
            assertEquals("Please enter your email address", (state as AuthUiState.Error).message)
        }
        verifyNoInteractions(authRepository)
    }

    @Test
    fun `signIn with empty password emits Error without calling repository`() = runTest {
        viewModel.uiState.test {
            skipItems(1)
            viewModel.signIn("user@example.com", "")
            val state = awaitItem()
            assertTrue(state is AuthUiState.Error)
            assertEquals("Please enter your password", (state as AuthUiState.Error).message)
        }
        verifyNoInteractions(authRepository)
    }

    @Test
    fun `signIn success emits Success state`() = runTest {
        whenever(authRepository.signIn("user@example.com", "password123"))
            .thenReturn(AuthResult.Success("uid-1", "Test User", "user@example.com"))

        viewModel.signIn("user@example.com", "password123")
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value is AuthUiState.Success)
    }

    @Test
    fun `signIn failure emits Error state`() = runTest {
        whenever(authRepository.signIn("user@example.com", "wrongpass"))
            .thenReturn(AuthResult.Error("Invalid credentials"))

        viewModel.signIn("user@example.com", "wrongpass")
        advanceUntilIdle()
        val state = viewModel.uiState.value
        assertTrue(state is AuthUiState.Error)
        assertEquals("Invalid credentials", (state as AuthUiState.Error).message)
    }

    @Test
    fun `signUp with empty name emits Error without calling repository`() = runTest {
        viewModel.uiState.test {
            skipItems(1)
            viewModel.signUp("user@example.com", "password123", "")
            val state = awaitItem()
            assertTrue(state is AuthUiState.Error)
            assertEquals("Please enter your full name", (state as AuthUiState.Error).message)
        }
        verifyNoInteractions(authRepository)
    }

    @Test
    fun `signUp with short password emits Error without calling repository`() = runTest {
        viewModel.uiState.test {
            skipItems(1)
            viewModel.signUp("user@example.com", "short", "Test User")
            val state = awaitItem()
            assertTrue(state is AuthUiState.Error)
            assertEquals("Password must be at least 8 characters", (state as AuthUiState.Error).message)
        }
        verifyNoInteractions(authRepository)
    }

    @Test
    fun `signUp success emits Success state`() = runTest {
        whenever(authRepository.signUp("user@example.com", "password123", "Test User"))
            .thenReturn(AuthResult.Success("uid-1", "Test User", "user@example.com"))

        viewModel.signUp("user@example.com", "password123", "Test User")
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value is AuthUiState.Success)
    }

    @Test
    fun `resetState sets state back to Idle`() = runTest {
        whenever(authRepository.signIn("user@example.com", "password123"))
            .thenReturn(AuthResult.Success("uid-1", "Test User", "user@example.com"))

        viewModel.signIn("user@example.com", "password123")
        advanceUntilIdle()

        viewModel.resetState()
        assertEquals(AuthUiState.Idle, viewModel.uiState.value)
    }

    @Test
    fun `sendPasswordReset with empty email emits Error`() = runTest {
        viewModel.uiState.test {
            skipItems(1)
            viewModel.sendPasswordReset("")
            val state = awaitItem()
            assertTrue(state is AuthUiState.Error)
        }
        verifyNoInteractions(authRepository)
    }

    @Test
    fun `signOut calls authRepository signOut`() {
        viewModel.signOut()
        verify(authRepository).signOut()
    }
}
