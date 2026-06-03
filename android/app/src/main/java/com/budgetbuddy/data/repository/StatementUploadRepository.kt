package com.budgetbuddy.data.repository

import com.budgetbuddy.data.remote.BudgetBuddyApiService
import com.budgetbuddy.data.remote.dto.StatementJobDto
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

sealed class UploadResult {
    data class Success(val jobId: Int, val filename: String) : UploadResult()
    data class Error(val message: String) : UploadResult()
}

@Singleton
class StatementUploadRepository @Inject constructor(
    private val api: BudgetBuddyApiService,
) {
    suspend fun uploadStatement(file: File): UploadResult {
        return try {
            val requestFile = file.asRequestBody("application/pdf".toMediaTypeOrNull())
            val part = MultipartBody.Part.createFormData("file", file.name, requestFile)
            val response = api.uploadStatement(part)
            UploadResult.Success(response.data.id, response.data.filename)
        } catch (e: Exception) {
            UploadResult.Error(e.message ?: "Upload failed. Please try again.")
        }
    }

    suspend fun listStatements(): List<StatementJobDto> {
        return try {
            api.listStatements().data
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getStatementStatus(id: Int): StatementJobDto? {
        return try {
            api.getStatement(id)
        } catch (e: Exception) {
            null
        }
    }
}
