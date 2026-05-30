import Foundation
import FirebaseFirestore

struct PolicyVersions {
    let termsVersion: String
    let privacyVersion: String
}

final class PolicyRepository {
    private let db = Firestore.firestore()

    func getCurrentVersions() async throws -> PolicyVersions {
        let snap = try await db.collection("app_config").document("policies").getDocument()
        return PolicyVersions(
            termsVersion:   snap.data()?["terms_version"]   as? String ?? "1.0",
            privacyVersion: snap.data()?["privacy_version"] as? String ?? "1.0"
        )
    }

    func hasUserAcceptedAll(uid: String) async -> Bool {
        do {
            let versions = try await getCurrentVersions()
            return await hasAccepted(uid: uid, type: "terms",   version: versions.termsVersion)
                && await hasAccepted(uid: uid, type: "privacy", version: versions.privacyVersion)
        } catch { return false }
    }

    private func hasAccepted(uid: String, type: String, version: String) async -> Bool {
        guard let snap = try? await db.collection("users").document(uid)
            .collection("policy_acceptances").document(type).getDocument(),
              let accepted = snap.data()?["version"] as? String else { return false }
        return accepted == version
    }

    func recordAllAcceptances(uid: String) async throws {
        let versions = try await getCurrentVersions()
        try await record(uid: uid, type: "terms",   version: versions.termsVersion)
        try await record(uid: uid, type: "privacy", version: versions.privacyVersion)
    }

    private func record(uid: String, type: String, version: String) async throws {
        let data: [String: Any] = [
            "version":    version,
            "acceptedAt": Timestamp(),
            "platform":   "ios",
        ]
        try await db.collection("users").document(uid)
            .collection("policy_acceptances").document(type)
            .setData(data, merge: true)
    }
}
