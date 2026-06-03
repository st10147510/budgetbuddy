package com.budgetbuddy.ui.profile

import android.net.Uri
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.budgetbuddy.data.local.SessionManager
import com.budgetbuddy.data.repository.AuthRepository
import com.budgetbuddy.data.repository.StorageRepository
import com.budgetbuddy.data.repository.SyncRepository
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
class ProfileViewModelTest {

    @get:Rule
    val instantTaskRule = InstantTaskExecutorRule()

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var storageRepository: StorageRepository
    private lateinit var authRepository: AuthRepository
    private lateinit var syncRepository: SyncRepository
    private lateinit var sessionManager: SessionManager
    private lateinit var viewModel: ProfileViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        storageRepository = mock()
        authRepository = mock()
        syncRepository = mock()
        sessionManager = mock()
        whenever(sessionManager.userId).thenReturn("user1")
        viewModel = ProfileViewModel(storageRepository, authRepository, syncRepository, sessionManager)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is Idle`() {
        assertEquals(ProfileUiState.Idle, viewModel.uiState.value)
    }

    @Test
    fun `uploadProfilePhoto emits PhotoUpdated on success and updates auth`() = runTest {
        val uri: Uri = mock()
        val url = "https://storage.googleapis.com/profile.jpg"
        whenever(storageRepository.uploadProfilePhoto("user1", uri)).thenReturn(Result.success(url))

        viewModel.uploadProfilePhoto(uri)
        advanceUntilIdle()

        assertEquals(ProfileUiState.PhotoUpdated(url), viewModel.uiState.value)
        verify(authRepository).updatePhotoUrl("user1", url)
    }

    @Test
    fun `uploadProfilePhoto emits Error on storage failure`() = runTest {
        val uri: Uri = mock()
        whenever(storageRepository.uploadProfilePhoto("user1", uri))
            .thenReturn(Result.failure(Exception("Upload failed")))

        viewModel.uploadProfilePhoto(uri)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is ProfileUiState.Error)
        assertEquals("Upload failed", (state as ProfileUiState.Error).message)
    }

    @Test
    fun `uploadProfilePhoto does nothing when user is not logged in`() = runTest {
        whenever(sessionManager.userId).thenReturn(null)
        val uri: Uri = mock()

        viewModel.uploadProfilePhoto(uri)
        advanceUntilIdle()

        verify(storageRepository, never()).uploadProfilePhoto(any(), any())
        assertEquals(ProfileUiState.Idle, viewModel.uiState.value)
    }

    @Test
    fun `syncToCloud emits SyncSuccess when sync completes`() = runTest {
        viewModel.syncToCloud()
        advanceUntilIdle()

        assertEquals(ProfileUiState.SyncSuccess, viewModel.uiState.value)
        verify(syncRepository).syncToFirestore("user1")
        verify(syncRepository).syncFromFirestore("user1")
    }

    @Test
    fun `syncToCloud emits Error when push throws`() = runTest {
        whenever(syncRepository.syncToFirestore("user1")).thenThrow(RuntimeException("network error"))

        viewModel.syncToCloud()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is ProfileUiState.Error)
        assertEquals("network error", (state as ProfileUiState.Error).message)
        verify(syncRepository, never()).syncFromFirestore(any())
    }

    @Test
    fun `syncToCloud emits Error when pull throws`() = runTest {
        whenever(syncRepository.syncFromFirestore("user1")).thenThrow(RuntimeException("pull failed"))

        viewModel.syncToCloud()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is ProfileUiState.Error)
        assertEquals("pull failed", (state as ProfileUiState.Error).message)
    }

    @Test
    fun `syncToCloud does nothing when user is not logged in`() = runTest {
        whenever(sessionManager.userId).thenReturn(null)

        viewModel.syncToCloud()
        advanceUntilIdle()

        verify(syncRepository, never()).syncToFirestore(any())
        verify(syncRepository, never()).syncFromFirestore(any())
        assertEquals(ProfileUiState.Idle, viewModel.uiState.value)
    }

    @Test
    fun `resetState sets state back to Idle`() = runTest {
        val uri: Uri = mock()
        whenever(storageRepository.uploadProfilePhoto(any(), any()))
            .thenReturn(Result.failure(Exception("err")))
        viewModel.uploadProfilePhoto(uri)
        advanceUntilIdle()

        viewModel.resetState()
        assertEquals(ProfileUiState.Idle, viewModel.uiState.value)
    }
}
