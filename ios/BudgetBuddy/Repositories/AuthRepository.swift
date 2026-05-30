import Foundation
import FirebaseAuth

enum AuthResult {
    case success(userId: String, displayName: String, email: String)
    case failure(String)
}

final class AuthRepository {
    private let auth    = Auth.auth()
    private let session: SessionManager
    private let firestore: FirestoreRepository
    private let sync: SyncRepository

    init(session: SessionManager, firestore: FirestoreRepository, sync: SyncRepository) {
        self.session   = session
        self.firestore = firestore
        self.sync      = sync
    }

    var isLoggedIn: Bool { auth.currentUser != nil }
    var currentUserId: String? { auth.currentUser?.uid }

    func signIn(email: String, password: String) async -> AuthResult {
        do {
            let result = try await auth.signIn(withEmail: email.trimmingCharacters(in: .whitespaces),
                                               password: password)
            let user = result.user
            let name = user.displayName?.nilIfEmpty
                ?? session.displayName
                ?? email.components(separatedBy: "@").first
                ?? "User"
            session.userId      = user.uid
            session.displayName = name
            session.email       = user.email ?? email
            Task {
                await sync.syncFromFirestore(userId: user.uid)
                if let url = await firestore.getUserPhotoUrl(uid: user.uid) {
                    session.photoUrl = url
                }
            }
            return .success(userId: user.uid, displayName: name, email: user.email ?? email)
        } catch {
            return .failure(friendlyError(error))
        }
    }

    func signUp(email: String, password: String, displayName: String) async -> AuthResult {
        do {
            let result = try await auth.createUser(withEmail: email.trimmingCharacters(in: .whitespaces),
                                                    password: password)
            let user = result.user
            let req = user.createProfileChangeRequest()
            req.displayName = displayName.trimmingCharacters(in: .whitespaces)
            try await req.commitChanges()
            session.userId      = user.uid
            session.displayName = displayName.trimmingCharacters(in: .whitespaces)
            session.email       = user.email ?? email
            Task {
                try? await firestore.saveUserProfile(uid: user.uid,
                                                     displayName: session.displayName ?? "",
                                                     email: user.email ?? email)
            }
            return .success(userId: user.uid, displayName: session.displayName ?? "", email: user.email ?? email)
        } catch {
            return .failure(friendlyError(error))
        }
    }

    func sendPasswordReset(email: String) async -> Result<Void, Error> {
        do {
            try await auth.sendPasswordReset(withEmail: email.trimmingCharacters(in: .whitespaces))
            return .success(())
        } catch {
            return .failure(error)
        }
    }

    func updatePhotoUrl(userId: String, url: String) async {
        let req = auth.currentUser?.createProfileChangeRequest()
        req?.photoURL = URL(string: url)
        try? await req?.commitChanges()
        try? await firestore.saveUserProfile(uid: userId,
                                             displayName: session.displayName ?? "",
                                             email: session.email ?? "",
                                             photoUrl: url)
        session.photoUrl = url
    }

    func signOut() {
        try? auth.signOut()
        session.clear()
    }

    private func friendlyError(_ error: Error) -> String {
        let code = (error as NSError).code
        let msg  = error.localizedDescription
        if msg.contains("user-not-found") || msg.contains("no user record") {
            return "No account found with this email address."
        }
        if msg.contains("wrong-password") || msg.contains("invalid-credential") || code == 17004 {
            return "Incorrect email or password."
        }
        if msg.contains("email-already-in-use") { return "An account with this email already exists." }
        if msg.contains("weak-password")        { return "Password is too weak." }
        if msg.contains("network")              { return "Network error. Check your connection." }
        if msg.contains("too-many-requests")    { return "Too many attempts. Try again later." }
        return msg
    }
}

private extension String {
    var nilIfEmpty: String? { isEmpty ? nil : self }
}
