import SwiftUI

struct SignInView: View {
    @EnvironmentObject var authVM: AuthViewModel
    @Environment(\.dismiss) var dismiss

    @State private var email    = ""
    @State private var password = ""
    @State private var showReset = false

    var body: some View {
        ScrollView {
            VStack(spacing: 24) {
                Text("Welcome back")
                    .font(.title.bold())
                    .frame(maxWidth: .infinity, alignment: .leading)

                VStack(spacing: 12) {
                    BBTextField("Email", text: $email, keyboard: .emailAddress)
                    BBSecureField("Password", text: $password)
                }

                if let err = authVM.errorMessage {
                    Text(err)
                        .font(.caption)
                        .foregroundColor(.red)
                        .frame(maxWidth: .infinity, alignment: .leading)
                }

                Button(authVM.isLoading ? "Signing in…" : "Sign In") {
                    authVM.signIn(email: email, password: password)
                }
                .tealButton()
                .disabled(authVM.isLoading || email.isEmpty || password.isEmpty)

                Button("Forgot password?") { showReset = true }
                    .font(.subheadline)
                    .foregroundColor(.teal)
            }
            .padding(24)
        }
        .navigationTitle("Sign In")
        .navigationBarTitleDisplayMode(.inline)
        .sheet(isPresented: $showReset) { ForgotPasswordView() }
    }
}
