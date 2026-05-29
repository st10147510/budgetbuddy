package com.budgetbuddy.ui.statement

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.budgetbuddy.data.remote.dto.StatementJobDto
import com.budgetbuddy.data.repository.StatementUploadRepository
import com.budgetbuddy.data.repository.UploadResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

sealed class UploadUiState {
    object Idle : UploadUiState()
    object Uploading : UploadUiState()
    data class Queued(val jobId: Int, val filename: String) : UploadUiState()
    data class Processing(val jobId: Int) : UploadUiState()
    data class Done(val rowsImported: Int, val filename: String) : UploadUiState()
    data class Failed(val error: String) : UploadUiState()
}

@HiltViewModel
class UploadStatementViewModel @Inject constructor(
    private val repository: StatementUploadRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<UploadUiState>(UploadUiState.Idle)
    val uiState: StateFlow<UploadUiState> = _uiState

    private val _history = MutableStateFlow<List<StatementJobDto>>(emptyList())
    val history: StateFlow<List<StatementJobDto>> = _history

    init {
        loadHistory()
    }

    fun loadHistory() {
        viewModelScope.launch {
            _history.value = repository.listStatements()
        }
    }

    fun upload(file: File) {
        viewModelScope.launch {
            _uiState.value = UploadUiState.Uploading
            when (val result = repository.uploadStatement(file)) {
                is UploadResult.Success -> {
                    _uiState.value = UploadUiState.Queued(result.jobId, result.filename)
                    loadHistory()
                    pollStatus(result.jobId)
                }
                is UploadResult.Error -> {
                    _uiState.value = UploadUiState.Failed(result.message)
                }
            }
        }
    }

    private fun pollStatus(jobId: Int) {
        viewModelScope.launch {
            repeat(20) {
                delay(3_000)
                val job = repository.getStatementStatus(jobId) ?: return@launch
                when (job.status) {
                    "done" -> {
                        _uiState.value = UploadUiState.Done(job.rowsImported ?: 0, job.filename)
                        loadHistory()
                        return@launch
                    }
                    "failed" -> {
                        _uiState.value = UploadUiState.Failed(job.error ?: "Processing failed.")
                        return@launch
                    }
                    else -> {
                        _uiState.value = UploadUiState.Processing(jobId)
                    }
                }
            }
        }
    }

    fun reset() {
        _uiState.value = UploadUiState.Idle
    }
}
