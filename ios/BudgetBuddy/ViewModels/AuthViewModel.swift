import Foundation
import Combine

enum AuthState {
    case unauthenticated
    case checkingPolicy
    case needsPolicy(termsVersion: String, privacyVersion: String)
    case authenticated(userId: String)
}

@MainActor
final class AuthViewModel: ObservableObject {
    @Published var state: AuthState = .unauthenticated
    @Published var isLoading = false
    @Published var errorMessage: String?

    private let authRepo: AuthRepository
    private let policyRepo: PolicyRepository
    private let session: SessionManager

    init(authRepo: AuthRepository, policyRepo: PolicyRepository, session: SessionManager) {
        self.authRepo   = authRepo
        self.policyRepo = policyRepo
        self.session    = session

        if authRepo.isLoggedIn, let uid = authRepo.currentUserId {
            checkPolicies(uid: uid)
        }
    }

    func signIn(email: String, password: String) {
        isLoading    = true
        errorMessage = nil
        Task {
            let result = await authRepo.signIn(email: email, password: password)
            isLoading  = false
            switch result {
            case .success(let userId, _, _): checkPolicies(uid: userId)
            case .failure(let msg):          errorMessage = msg
            }
        }
    }

    func signUp(email: String, password: String, displayName: String) {
        isLoading    = true
        errorMessage = nil
        Task {
            let result = await authRepo.signUp(email: email, password: password, displayName: displayName)
            isLoading  = false
            switch result {
            case .success(let userId, _, _): checkPolicies(uid: userId)
            case .failure(let msg):          errorMessage = msg
            }
        }
    }

    func sendReset(email: String, completion: @escaping (Bool, String?) -> Void) {
        Task {
            let result = await authRepo.sendPasswordReset(email: email)
            switch result {
            case .success:         completion(true, nil)
            case .failure(let e):  completion(false, e.localizedDescription)
            }
        }
    }

    func acceptPolicies() {
        guard case .needsPolicy = state, let uid = authRepo.currentUserId else { return }
        isLoading = true
        Task {
            try? await policyRepo.recordAllAcceptances(uid: uid)
            isLoading = false
            state     = .authenticated(userId: uid)
        }
    }

    func signOut() {
        authRepo.signOut()
        state = .unauthenticated
    }

    private func checkPolicies(uid: String) {
        state = .checkingPolicy
        Task {
            let accepted = await policyRepo.hasUserAcceptedAll(uid: uid)
            if accepted {
                state = .authenticated(userId: uid)
            } else {
                let versions = try? await policyRepo.getCurrentVersions()
                state = .needsPolicy(termsVersion:   versions?.termsVersion   ?? "1.0",
                                     privacyVersion: versions?.privacyVersion ?? "1.0")
            }
        }
    }
}
