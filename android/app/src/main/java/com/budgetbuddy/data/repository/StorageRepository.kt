package com.budgetbuddy.data.repository

import android.net.Uri
import android.util.Log
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "StorageRepository"

@Singleton
class StorageRepository @Inject constructor(
    private val storage: FirebaseStorage
) {
    suspend fun uploadProfilePhoto(userId: String, imageUri: Uri): Result<String> {
        return try {
            val ref = storage.reference.child("profile_photos/$userId.jpg")
            ref.putFile(imageUri).await()
            val downloadUrl = ref.downloadUrl.await().toString()
            Result.success(downloadUrl)
        } catch (e: Exception) {
            Log.w(TAG, "uploadProfilePhoto failed: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun uploadReceiptPhoto(userId: String, imageUri: Uri): Result<String> {
        return try {
            val filename = "receipts/$userId/${System.currentTimeMillis()}.jpg"
            val ref = storage.reference.child(filename)
            ref.putFile(imageUri).await()
            val downloadUrl = ref.downloadUrl.await().toString()
            Result.success(downloadUrl)
        } catch (e: Exception) {
            Log.w(TAG, "uploadReceiptPhoto failed: ${e.message}")
            Result.failure(e)
        }
    }
}
