import SwiftUI

struct WelcomeView: View {
    @State private var showSignIn = false
    @State private var showSignUp = false

    var body: some View {
        NavigationStack {
            VStack(spacing: 0) {
                Spacer()

                // Logo & tagline
                VStack(spacing: 16) {
                    ZStack {
                        Circle()
                            .fill(Color.teal.opacity(0.15))
                            .frame(width: 100, height: 100)
                        Text("💰")
                            .font(.system(size: 52))
                    }
                    Text("BudgetBuddy")
                        .font(.largeTitle.bold())
                    Text("Take control of your finances")
                        .font(.subheadline)
                        .foregroundStyle(.secondary)
                }

                Spacer()

                // Actions
                VStack(spacing: 12) {
                    Button("Get Started") { showSignUp = true }
                        .tealButton()

                    Button("Sign In") { showSignIn = true }
                        .font(.subheadline.weight(.semibold))
                        .foregroundColor(.teal)
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 14)
                        .overlay(RoundedRectangle(cornerRadius: 14).stroke(Color.teal, lineWidth: 1.5))
                }
                .padding(.horizontal, 24)
                .padding(.bottom, 48)
            }
            .navigationDestination(isPresented: $showSignIn)  { SignInView() }
            .navigationDestination(isPresented: $showSignUp)  { SignUpView() }
        }
    }
}
