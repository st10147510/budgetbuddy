package com.budgetbuddy.data.remote.dto

import com.google.gson.annotations.SerializedName

data class StatementUploadResponse(
    val message: String,
    val data: StatementJobDto,
)

data class StatementListResponse(
    val data: List<StatementJobDto>,
)

data class StatementJobDto(
    val id: Int,
    val filename: String,
    val status: String,
    @SerializedName("rows_imported") val rowsImported: Int?,
    @SerializedName("storage_url") val storageUrl: String?,
    val error: String?,
    @SerializedName("created_at") val createdAt: String?,
)

data class PolicyVersionsResponse(
    val data: PolicyVersionsDto,
)

data class PolicyVersionsDto(
    @SerializedName("terms_version") val termsVersion: String,
    @SerializedName("privacy_version") val privacyVersion: String,
)

data class PolicyAcceptRequest(
    val type: String,
)

data class ApiResponse(
    val message: String,
)
