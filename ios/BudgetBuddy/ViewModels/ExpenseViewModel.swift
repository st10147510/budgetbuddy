import Foundation

@MainActor
final class ExpenseViewModel: ObservableObject {
    @Published var transactions: [BBTransaction] = []
    @Published var categories: [BBCategory]      = []
    @Published var isLoading = false
    @Published var error: String?
    @Published var saveSuccess = false

    private let transactionRepo: TransactionRepository
    private let categoryRepo: CategoryRepository
    private let badgeRepo: BadgeRepository
    private let storageRepo: StorageRepository

    init(transactionRepo: TransactionRepository, categoryRepo: CategoryRepository,
         badgeRepo: BadgeRepository, storageRepo: StorageRepository) {
        self.transactionRepo = transactionRepo
        self.categoryRepo    = categoryRepo
        self.badgeRepo       = badgeRepo
        self.storageRepo     = storageRepo
    }

    func loadAll(userId: String) {
        isLoading = true
        Task {
            do {
                transactions = try transactionRepo.all(userId: userId)
                categories   = try categoryRepo.all()
            } catch { self.error = error.localizedDescription }
            isLoading = false
        }
    }

    func loadForDateRange(userId: String, start: Date, end: Date) {
        Task {
            do {
                transactions = try transactionRepo.forDateRange(userId: userId, start: start, end: end)
            } catch { self.error = error.localizedDescription }
        }
    }

    func save(userId: String, amount: Double, categoryId: UUID?, date: Date,
              notes: String?, type: TransactionType, receiptImage: UIImage? = nil) {
        isLoading = true
        Task {
            var imagePath: String? = nil
            if let img = receiptImage {
                let result = await storageRepo.uploadReceipt(userId: userId, image: img)
                if case .success(let url) = result { imagePath = url }
            }
            let tx = BBTransaction(userId: userId, amount: amount, categoryId: categoryId,
                                   date: date, notes: notes.flatMap { $0.isEmpty ? nil : $0 },
                                   receiptImagePath: imagePath, type: type)
            do {
                try await transactionRepo.insert(tx)
                await badgeRepo.checkAndAwardBadges(userId: userId)
                saveSuccess  = true
                isLoading    = false
                loadAll(userId: userId)
            } catch {
                self.error = error.localizedDescription
                isLoading  = false
            }
        }
    }

    func delete(_ tx: BBTransaction) {
        Task {
            try? await transactionRepo.delete(tx)
            transactions.removeAll { $0.id == tx.id }
        }
    }

    func category(for tx: BBTransaction) -> BBCategory? {
        guard let cid = tx.categoryId else { return nil }
        return categories.first { $0.id == cid }
    }
}

// Avoid UIKit import in model-only files; forward-declare here
import UIKit
