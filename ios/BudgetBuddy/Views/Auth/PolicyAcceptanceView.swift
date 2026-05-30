import SwiftUI

struct PolicyAcceptanceView: View {
    @EnvironmentObject var authVM: AuthViewModel
    let termsVersion: String
    let privacyVersion: String

    @State private var termsChecked   = false
    @State private var privacyChecked = false

    var body: some View {
        VStack(spacing: 0) {
            ScrollView {
                VStack(alignment: .leading, spacing: 24) {
                    VStack(alignment: .leading, spacing: 8) {
                        Text("Before you continue")
                            .font(.title.bold())
                        Text("Please read and accept our policies to use BudgetBuddy.")
                            .font(.subheadline)
                            .foregroundStyle(.secondary)
                    }

                    // Terms
                    PolicyCard(
                        icon: "doc.text",
                        title: "Terms & Conditions",
                        version: termsVersion,
                        url: URL(string: "https://thebudgetbuddy.co.za/terms")!,
                        isChecked: $termsChecked
                    )

                    // Privacy
                    PolicyCard(
                        icon: "lock.shield",
                        title: "Privacy Policy",
                        version: privacyVersion,
                        url: URL(string: "https://thebudgetbuddy.co.za/privacy")!,
                        isChecked: $privacyChecked
                    )
                }
                .padding(24)
            }

            Divider()

            VStack(spacing: 12) {
                Button(authVM.isLoading ? "Accepting…" : "Accept & Continue") {
                    authVM.acceptPolicies()
                }
                .tealButton()
                .disabled(!termsChecked || !privacyChecked || authVM.isLoading)
                .padding(.horizontal, 24)

                Button("Sign Out") { authVM.signOut() }
                    .font(.subheadline)
                    .foregroundColor(.secondary)
            }
            .padding(.vertical, 16)
        }
    }
}

private struct PolicyCard: View {
    let icon: String
    let title: String
    let version: String
    let url: URL
    @Binding var isChecked: Bool

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack(spacing: 12) {
                Image(systemName: icon)
                    .font(.title3)
                    .foregroundColor(.teal)
                VStack(alignment: .leading, spacing: 2) {
                    Text(title).font(.headline)
                    Text("Version \(version)").font(.caption).foregroundStyle(.secondary)
                }
                Spacer()
                Link(destination: url) {
                    Image(systemName: "arrow.up.right.square")
                        .foregroundColor(.teal)
                }
            }

            Toggle(isOn: $isChecked) {
                Text("I have read and agree to the \(title)")
                    .font(.subheadline)
            }
            .tint(.teal)
        }
        .padding()
        .cardStyle()
    }
}
