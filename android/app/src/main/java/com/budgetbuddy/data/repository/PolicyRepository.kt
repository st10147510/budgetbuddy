package com.budgetbuddy.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

data class PolicyVersions(
    val termsVersion: String = "1.0",
    val privacyVersion: String = "1.0",
)

@Singleton
class PolicyRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
) {
    private val configRef get() = firestore.collection("app_config").document("policies")

    suspend fun getCurrentVersions(): PolicyVersions {
        return try {
            val snap = configRef.get().await()
            PolicyVersions(
                termsVersion   = snap.getString("terms_version")   ?: "1.0",
                privacyVersion = snap.getString("privacy_version") ?: "1.0",
            )
        } catch (e: Exception) {
            PolicyVersions()
        }
    }

    suspend fun hasUserAcceptedAll(uid: String): Boolean {
        val versions = getCurrentVersions()
        return hasAccepted(uid, "terms", versions.termsVersion)
            && hasAccepted(uid, "privacy", versions.privacyVersion)
    }

    private suspend fun hasAccepted(uid: String, type: String, requiredVersion: String): Boolean {
        return try {
            val snap = firestore.collection("users").document(uid)
                .collection("policy_acceptances").document(type)
                .get().await()
            snap.getString("version") == requiredVersion
        } catch (e: Exception) {
            false
        }
    }

    suspend fun recordAcceptance(uid: String, type: String) {
        val versions = getCurrentVersions()
        val version = if (type == "terms") versions.termsVersion else versions.privacyVersion
        val data = mapOf(
            "version"    to version,
            "acceptedAt" to com.google.firebase.Timestamp.now(),
            "platform"   to "android",
        )
        firestore.collection("users").document(uid)
            .collection("policy_acceptances").document(type)
            .set(data, SetOptions.merge()).await()
    }

    suspend fun recordAllAcceptances(uid: String) {
        recordAcceptance(uid, "terms")
        recordAcceptance(uid, "privacy")
    }
}
