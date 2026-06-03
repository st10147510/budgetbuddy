import SwiftUI

struct GoalsView: View {
    let userId: String
    @ObservedObject var vm: GoalsViewModel
    @State private var showAdd = false

    var body: some View {
        NavigationStack {
            Group {
                if vm.goals.isEmpty && !vm.isLoading {
                    EmptyStateView(icon: "star", message: "No savings goals yet.\nTap + to create one.")
                } else {
                    List {
                        let active    = vm.goals.filter { !$0.isCompleted }
                        let completed = vm.goals.filter {  $0.isCompleted }
                        if !active.isEmpty {
                            Section("Active") {
                                ForEach(active) { goal in
                                    NavigationLink {
                                        GoalDetailView(goal: goal, vm: vm, userId: userId)
                                    } label: {
                                        GoalRowView(goal: goal)
                                    }
                                }
                                .onDelete { idxs in idxs.forEach { vm.delete(active[$0]) } }
                            }
                        }
                        if !completed.isEmpty {
                            Section("Completed") {
                                ForEach(completed) { goal in
                                    GoalRowView(goal: goal)
                                }
                            }
                        }
                    }
                }
            }
            .navigationTitle("Goals")
            .refreshable { vm.load(userId: userId) }
            .toolbar {
                ToolbarItem(placement: .primaryAction) {
                    Button { showAdd = true } label: { Image(systemName: "plus") }
                }
            }
            .sheet(isPresented: $showAdd, onDismiss: { vm.load(userId: userId) }) {
                AddGoalView(userId: userId, vm: vm)
            }
        }
    }
}

private struct GoalRowView: View {
    let goal: BBGoal

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            HStack {
                Text(goal.name).font(.subheadline.bold())
                Spacer()
                if goal.isCompleted {
                    Label("Done", systemImage: "checkmark.circle.fill").font(.caption).foregroundColor(.green)
                }
            }
            ProgressView(value: Double(goal.progressPercent) / 100).tint(.teal)
            HStack {
                Text("\(goal.savedAmount.currencyFormatted) / \(goal.targetAmount.currencyFormatted)")
                    .font(.caption).foregroundStyle(.secondary)
                Spacer()
                Text("\(goal.progressPercent)%").font(.caption.bold()).foregroundColor(.teal)
            }
            if let date = goal.targetDate {
                Text("Target: \(date.formatted(style: .medium))").font(.caption2).foregroundStyle(.tertiary)
            }
        }
        .padding(.vertical, 4)
    }
}
