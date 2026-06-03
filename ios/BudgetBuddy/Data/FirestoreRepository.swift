import Foundation
import FirebaseFirestore

final class FirestoreRepository {
    private let db = Firestore.firestore()

    // MARK: - User

    func saveUserProfile(uid: String, displayName: String, email: String, photoUrl: String? = nil) async throws {
        var data: [String: Any] = ["displayName": displayName, "email": email, "updatedAt": Timestamp()]
        if let url = photoUrl { data["photoUrl"] = url }
        try await db.collection("users").document(uid).setData(data, merge: true)
    }

    func getUserPhotoUrl(uid: String) async -> String? {
        let snap = try? await db.collection("users").document(uid).getDocument()
        return snap?.data()?["photoUrl"] as? String
    }

    // MARK: - Transactions

    func saveTransaction(userId: String, tx: BBTransaction) async {
        let data: [String: Any] = [
            "id": tx.id.uuidString,
            "amount": tx.amount,
            "categoryId": tx.categoryId?.uuidString ?? "",
            "date": Timestamp(date: tx.date),
            "notes": tx.notes ?? "",
            "receiptImagePath": tx.receiptImagePath ?? "",
            "type": tx.type.rawValue,
            "createdAt": Timestamp(date: tx.createdAt),
        ]
        try? await db.collection("users").document(userId)
            .collection("transactions").document(tx.id.uuidString)
            .setData(data, merge: true)
    }

    func deleteTransaction(userId: String, id: UUID) async {
        try? await db.collection("users").document(userId)
            .collection("transactions").document(id.uuidString).delete()
    }

    // MARK: - Budgets

    func saveBudget(userId: String, budget: BBBudget) async {
        let data: [String: Any] = [
            "id": budget.id.uuidString,
            "categoryId": budget.categoryId.uuidString,
            "minAmount": budget.minAmount,
            "limitAmount": budget.limitAmount,
            "month": budget.month,
            "year": budget.year,
            "createdAt": Timestamp(date: budget.createdAt),
        ]
        try? await db.collection("users").document(userId)
            .collection("budgets").document(budget.id.uuidString)
            .setData(data, merge: true)
    }

    func deleteBudget(userId: String, id: UUID) async {
        try? await db.collection("users").document(userId)
            .collection("budgets").document(id.uuidString).delete()
    }

    // MARK: - Goals

    func saveGoal(userId: String, goal: BBGoal) async {
        var data: [String: Any] = [
            "id": goal.id.uuidString,
            "name": goal.name,
            "targetAmount": goal.targetAmount,
            "savedAmount": goal.savedAmount,
            "isCompleted": goal.isCompleted,
            "createdAt": Timestamp(date: goal.createdAt),
        ]
        if let td = goal.targetDate { data["targetDate"] = Timestamp(date: td) }
        try? await db.collection("users").document(userId)
            .collection("goals").document(goal.id.uuidString)
            .setData(data, merge: true)
    }

    func deleteGoal(userId: String, id: UUID) async {
        try? await db.collection("users").document(userId)
            .collection("goals").document(id.uuidString).delete()
    }

    // MARK: - Debts

    func saveDebt(userId: String, debt: BBDebt) async {
        let data: [String: Any] = [
            "id": debt.id.uuidString,
            "name": debt.name,
            "originalBalance": debt.originalBalance,
            "balance": debt.balance,
            "interestRate": debt.interestRate,
            "minimumPayment": debt.minimumPayment,
            "isPaidOff": debt.isPaidOff,
            "createdAt": Timestamp(date: debt.createdAt),
        ]
        try? await db.collection("users").document(userId)
            .collection("debts").document(debt.id.uuidString)
            .setData(data, merge: true)
    }

    func deleteDebt(userId: String, id: UUID) async {
        try? await db.collection("users").document(userId)
            .collection("debts").document(id.uuidString).delete()
    }

    // MARK: - Badges

    func saveBadge(userId: String, badge: BBBadge) async {
        let data: [String: Any] = [
            "id": badge.id.uuidString,
            "badgeType": badge.badgeType.rawValue,
            "earnedAt": Timestamp(date: badge.earnedAt),
        ]
        try? await db.collection("users").document(userId)
            .collection("badges").document(badge.badgeType.rawValue)
            .setData(data, merge: true)
    }

    // MARK: - Policies

    func getPolicyVersions() async throws -> (termsVersion: String, privacyVersion: String) {
        let snap = try await db.collection("app_config").document("policies").getDocument()
        let terms   = snap.data()?["terms_version"]   as? String ?? "1.0"
        let privacy = snap.data()?["privacy_version"] as? String ?? "1.0"
        return (terms, privacy)
    }

    func hasPolicyAcceptance(uid: String, type: String, version: String) async -> Bool {
        guard let snap = try? await db.collection("users").document(uid)
            .collection("policy_acceptances").document(type).getDocument(),
              let accepted = snap.data()?["version"] as? String else { return false }
        return accepted == version
    }

    func recordPolicyAcceptance(uid: String, type: String, version: String) async throws {
        let data: [String: Any] = [
            "version": version,
            "acceptedAt": Timestamp(),
            "platform": "ios",
        ]
        try await db.collection("users").document(uid)
            .collection("policy_acceptances").document(type)
            .setData(data, merge: true)
    }

    // MARK: - Sync: fetch all remote data for a user

    func fetchAllTransactions(userId: String) async -> [[String: Any]] {
        let snaps = try? await db.collection("users").document(userId)
            .collection("transactions").getDocuments()
        return snaps?.documents.compactMap { $0.data() } ?? []
    }

    func fetchAllBudgets(userId: String) async -> [[String: Any]] {
        let snaps = try? await db.collection("users").document(userId)
            .collection("budgets").getDocuments()
        return snaps?.documents.compactMap { $0.data() } ?? []
    }

    func fetchAllGoals(userId: String) async -> [[String: Any]] {
        let snaps = try? await db.collection("users").document(userId)
            .collection("goals").getDocuments()
        return snaps?.documents.compactMap { $0.data() } ?? []
    }

    func fetchAllDebts(userId: String) async -> [[String: Any]] {
        let snaps = try? await db.collection("users").document(userId)
            .collection("debts").getDocuments()
        return snaps?.documents.compactMap { $0.data() } ?? []
    }

    func fetchAllBadges(userId: String) async -> [[String: Any]] {
        let snaps = try? await db.collection("users").document(userId)
            .collection("badges").getDocuments()
        return snaps?.documents.compactMap { $0.data() } ?? []
    }
}
