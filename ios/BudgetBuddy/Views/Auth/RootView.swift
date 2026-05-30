import SwiftUI

struct RootView: View {
    @EnvironmentObject var authVM: AuthViewModel

    var body: some View {
        Group {
            switch authVM.state {
            case .unauthenticated:
                WelcomeView()
            case .checkingPolicy:
                ProgressView("Checking account…")
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
                    .background(Color(.systemBackground))
            case .needsPolicy(let terms, let privacy):
                PolicyAcceptanceView(termsVersion: terms, privacyVersion: privacy)
            case .authenticated(let userId):
                MainTabView(userId: userId)
            }
        }
        .animation(.easeInOut, value: authVM.state.isAuthenticated)
    }
}

private extension AuthState {
    var isAuthenticated: Bool {
        if case .authenticated = self { return true }
        return false
    }
}
