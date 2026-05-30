import SwiftUI

struct BadgesView: View {
    let userId: String
    @State private var earned: [BBBadge] = []
    private let badgeRepo = AppContainer.shared.badgeRepo

    private var earnedTypes: Set<BadgeType> { Set(earned.map(\.badgeType)) }

    var body: some View {
        NavigationStack {
            ScrollView {
                LazyVGrid(columns: [GridItem(.adaptive(minimum: 140))], spacing: 16) {
                    ForEach(BadgeType.allCases, id: \.self) { type in
                        BadgeCard(type: type, isEarned: earnedTypes.contains(type),
                                  earnedAt: earned.first(where: { $0.badgeType == type })?.earnedAt)
                    }
                }
                .padding(16)
            }
            .navigationTitle("Achievements")
            .task {
                earned = (try? badgeRepo.all(userId: userId)) ?? []
            }
        }
    }
}

private struct BadgeCard: View {
    let type: BadgeType
    let isEarned: Bool
    let earnedAt: Date?

    var body: some View {
        VStack(spacing: 10) {
            Text(type.icon)
                .font(.system(size: 40))
                .opacity(isEarned ? 1 : 0.3)
                .grayscale(isEarned ? 0 : 1)

            Text(type.displayName)
                .font(.caption.bold())
                .multilineTextAlignment(.center)

            Text(type.description)
                .font(.caption2)
                .foregroundStyle(.secondary)
                .multilineTextAlignment(.center)

            if let date = earnedAt {
                Text(date.formatted(style: .short))
                    .font(.caption2)
                    .foregroundColor(.teal)
            } else {
                Text("Locked")
                    .font(.caption2)
                    .foregroundStyle(.tertiary)
            }
        }
        .padding(14)
        .frame(maxWidth: .infinity)
        .cardStyle()
        .overlay(
            RoundedRectangle(cornerRadius: 16)
                .stroke(isEarned ? Color.teal.opacity(0.4) : Color.clear, lineWidth: 1.5)
        )
    }
}
