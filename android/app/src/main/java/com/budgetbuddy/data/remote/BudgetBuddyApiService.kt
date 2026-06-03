package com.budgetbuddy.data.remote

import com.budgetbuddy.data.remote.dto.ApiResponse
import com.budgetbuddy.data.remote.dto.PolicyAcceptRequest
import com.budgetbuddy.data.remote.dto.PolicyVersionsResponse
import com.budgetbuddy.data.remote.dto.StatementListResponse
import com.budgetbuddy.data.remote.dto.StatementJobDto
import com.budgetbuddy.data.remote.dto.StatementUploadResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface BudgetBuddyApiService {

    @GET("v1/statements")
    suspend fun listStatements(): StatementListResponse

    @GET("v1/statements/{id}")
    suspend fun getStatement(@Path("id") id: Int): StatementJobDto

    @Multipart
    @POST("v1/statements")
    suspend fun uploadStatement(
        @Part file: MultipartBody.Part,
        @Part("default_category") defaultCategory: RequestBody? = null,
    ): StatementUploadResponse

    @GET("v1/policies/current")
    suspend fun getPolicyVersions(): PolicyVersionsResponse

    @POST("v1/policies/accept")
    suspend fun acceptPolicy(@Body body: PolicyAcceptRequest): ApiResponse
}
