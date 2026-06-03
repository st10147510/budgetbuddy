import SwiftUI

// MARK: - Shared text fields

struct BBTextField: View {
    let placeholder: String
    @Binding var text: String
    var keyboard: UIKeyboardType = .default

    init(_ placeholder: String, text: Binding<String>, keyboard: UIKeyboardType = .default) {
        self.placeholder = placeholder
        _text = text
        self.keyboard = keyboard
    }

    var body: some View {
        TextField(placeholder, text: $text)
            .keyboardType(keyboard)
            .autocapitalization(keyboard == .emailAddress ? .none : .sentences)
            .autocorrectionDisabled(keyboard == .emailAddress)
            .padding(14)
            .background(Color(.secondarySystemBackground))
            .cornerRadius(12)
    }
}

struct BBSecureField: View {
    let placeholder: String
    @Binding var text: String

    init(_ placeholder: String, text: Binding<String>) {
        self.placeholder = placeholder
        _text = text
    }

    var body: some View {
        SecureField(placeholder, text: $text)
            .padding(14)
            .background(Color(.secondarySystemBackground))
            .cornerRadius(12)
    }
}

// MARK: - Empty state

struct EmptyStateView: View {
    let icon: String
    let message: String

    var body: some View {
        VStack(spacing: 12) {
            Image(systemName: icon)
                .font(.system(size: 40))
                .foregroundColor(.teal.opacity(0.5))
            Text(message)
                .font(.subheadline)
                .foregroundStyle(.secondary)
                .multilineTextAlignment(.center)
        }
        .frame(maxWidth: .infinity)
        .padding(32)
    }
}

// MARK: - Amount input field

struct AmountField: View {
    let label: String
    @Binding var value: String

    var body: some View {
        VStack(alignment: .leading, spacing: 6) {
            Text(label).font(.caption).foregroundStyle(.secondary)
            HStack {
                Text("R").foregroundStyle(.secondary)
                TextField("0.00", text: $value)
                    .keyboardType(.decimalPad)
            }
            .padding(14)
            .background(Color(.secondarySystemBackground))
            .cornerRadius(12)
        }
    }
}
