package com.budgetbuddy.ui.profile

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.budgetbuddy.data.local.SessionManager
import com.budgetbuddy.data.repository.AuthRepository
import com.budgetbuddy.data.repository.StorageRepository
import com.budgetbuddy.data.repository.SyncRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ProfileUiState {
    object Idle : ProfileUiState()
    object Loading : ProfileUiState()
    object Syncing : ProfileUiState()
    object SyncSuccess : ProfileUiState()
    data class PhotoUpdated(val url: String) : ProfileUiState()
    data class Error(val message: String) : ProfileUiState()
}

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val storageRepository: StorageRepository,
    private val authRepository: AuthRepository,
    private val syncRepository: SyncRepository,
    val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Idle)
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    fun uploadProfilePhoto(uri: Uri) {
        val userId = sessionManager.userId ?: return
        viewModelScope.launch {
            _uiState.value = ProfileUiState.Loading
            storageRepository.uploadProfilePhoto(userId, uri).fold(
                onSuccess = { url ->
                    authRepository.updatePhotoUrl(userId, url)
                    _uiState.value = ProfileUiState.PhotoUpdated(url)
                },
                onFailure = {
                    _uiState.value = ProfileUiState.Error(it.message ?: "Photo upload failed")
                }
            )
        }
    }

    fun syncToCloud() {
        val userId = sessionManager.userId ?: return
        viewModelScope.launch {
            _uiState.value = ProfileUiState.Syncing
            try {
                syncRepository.syncToFirestore(userId)
                _uiState.value = ProfileUiState.SyncSuccess
            } catch (e: Exception) {
                _uiState.value = ProfileUiState.Error(e.message ?: "Sync failed")
            }
        }
    }

    fun resetState() { _uiState.value = ProfileUiState.Idle }
}
