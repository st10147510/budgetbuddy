import SwiftUI

struct SignUpView: View {
    @EnvironmentObject var authVM: AuthViewModel

    @State private var name     = ""
    @State private var email    = ""
    @State private var password = ""
    @State private var confirm  = ""

    private var passwordMismatch: Bool { !confirm.isEmpty && password != confirm }

    var body: some View {
        ScrollView {
            VStack(spacing: 24) {
                Text("Create account")
                    .font(.title.bold())
                    .frame(maxWidth: .infinity, alignment: .leading)

                VStack(spacing: 12) {
                    BBTextField("Full name", text: $name)
                    BBTextField("Email", text: $email, keyboard: .emailAddress)
                    BBSecureField("Password", text: $password)
                    BBSecureField("Confirm password", text: $confirm)
                    if passwordMismatch {
                        Text("Passwords don't match.")
                            .font(.caption).foregroundColor(.red)
                            .frame(maxWidth: .infinity, alignment: .leading)
                    }
                }

                if let err = authVM.errorMessage {
                    Text(err).font(.caption).foregroundColor(.red)
                        .frame(maxWidth: .infinity, alignment: .leading)
                }

                Button(authVM.isLoading ? "Creating account…" : "Create Account") {
                    authVM.signUp(email: email, password: password, displayName: name)
                }
                .tealButton()
                .disabled(authVM.isLoading || name.isEmpty || email.isEmpty
                          || password.count < 6 || passwordMismatch)

                Text("By creating an account you agree to our Terms & Conditions and Privacy Policy.")
                    .font(.caption2)
                    .foregroundStyle(.secondary)
                    .multilineTextAlignment(.center)
            }
            .padding(24)
        }
        .navigationTitle("Sign Up")
        .navigationBarTitleDisplayMode(.inline)
    }
}
