import Foundation

@MainActor
final class GoalsViewModel: ObservableObject {
    @Published var goals: [BBGoal]  = []
    @Published var isLoading = false
    @Published var error: String?

    private let goalRepo: GoalRepository

    init(goalRepo: GoalRepository) { self.goalRepo = goalRepo }

    func load(userId: String) {
        isLoading = true
        Task {
            do   { goals = try goalRepo.all(userId: userId) }
            catch { self.error = error.localizedDescription }
            isLoading = false
        }
    }

    func create(userId: String, name: String, targetAmount: Double,
                targetDate: Date?, completion: (() -> Void)? = nil) {
        let goal = BBGoal(userId: userId, name: name, targetAmount: targetAmount, targetDate: targetDate)
        Task {
            do {
                try await goalRepo.insert(goal)
                load(userId: userId)
                completion?()
            } catch { self.error = error.localizedDescription }
        }
    }

    func addSavings(goal: BBGoal, amount: Double) {
        goal.savedAmount += amount
        if goal.savedAmount >= goal.targetAmount { goal.isCompleted = true }
        Task { try? await goalRepo.update(goal) }
    }

    func delete(_ goal: BBGoal) {
        Task {
            try? await goalRepo.delete(goal)
            goals.removeAll { $0.id == goal.id }
        }
    }
}
