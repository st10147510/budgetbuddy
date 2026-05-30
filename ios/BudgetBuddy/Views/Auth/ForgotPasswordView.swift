import SwiftUI

struct ForgotPasswordView: View {
    @EnvironmentObject var authVM: AuthViewModel
    @Environment(\.dismiss) var dismiss

    @State private var email   = ""
    @State private var sent    = false
    @State private var errMsg: String?

    var body: some View {
        NavigationStack {
            VStack(spacing: 24) {
                Text("Enter your email address and we'll send you a link to reset your password.")
                    .font(.subheadline)
                    .foregroundStyle(.secondary)
                    .multilineTextAlignment(.center)

                BBTextField("Email", text: $email, keyboard: .emailAddress)

                if let err = errMsg {
                    Text(err).font(.caption).foregroundColor(.red)
                }

                if sent {
                    Label("Reset email sent! Check your inbox.", systemImage: "checkmark.circle.fill")
                        .foregroundColor(.green)
                        .font(.subheadline)
                }

                Button("Send Reset Link") {
                    authVM.sendReset(email: email) { ok, err in
                        sent   = ok
                        errMsg = err
                    }
                }
                .tealButton()
                .disabled(email.isEmpty)

                Spacer()
            }
            .padding(24)
            .navigationTitle("Reset Password")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Close") { dismiss() }
                }
            }
        }
    }
}
