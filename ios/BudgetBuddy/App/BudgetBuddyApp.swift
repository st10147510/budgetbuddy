import SwiftUI
import SwiftData
import FirebaseCore

@main
struct BudgetBuddyApp: App {
    @UIApplicationDelegateAdaptor(AppDelegate.self) var delegate

    var body: some Scene {
        WindowGroup {
            RootView()
                .modelContainer(AppContainer.shared.modelContainer)
                .environmentObject(AppContainer.shared.authViewModel)
                .environmentObject(AppContainer.shared)
        }
    }
}

// MARK: - App-level DI container

final class AppContainer: ObservableObject {
    static let shared = AppContainer()

    let modelContainer: ModelContainer

    // Repos — shared singletons
    let firestoreRepo:   FirestoreRepository
    let storageRepo:     StorageRepository
    let transactionRepo: TransactionRepository
    let budgetRepo:      BudgetRepository
    let goalRepo:        GoalRepository
    let debtRepo:        DebtRepository
    let categoryRepo:    CategoryRepository
    let badgeRepo:       BadgeRepository
    let syncRepo:        SyncRepository
    let policyRepo:      PolicyRepository

    // ViewModels
    let authViewModel: AuthViewModel

    private init() {
        let schema = Schema([
            BBTransaction.self,
            BBBudget.self,
            BBGoal.self,
            BBDebt.self,
            BBCategory.self,
            BBBadge.self,
        ])
        let config = ModelConfiguration(schema: schema, isStoredInMemoryOnly: false)
        do {
            modelContainer = try ModelContainer(for: schema, configurations: config)
        } catch {
            fatalError("Failed to create ModelContainer: \(error)")
        }

        let ctx = modelContainer.mainContext
        DefaultCategorySeeder.seedIfNeeded(context: ctx)

        firestoreRepo   = FirestoreRepository()
        storageRepo     = StorageRepository()
        transactionRepo = TransactionRepository(context: ctx, firestore: firestoreRepo)
        budgetRepo      = BudgetRepository(context: ctx, firestore: firestoreRepo)
        goalRepo        = GoalRepository(context: ctx, firestore: firestoreRepo)
        debtRepo        = DebtRepository(context: ctx, firestore: firestoreRepo)
        categoryRepo    = CategoryRepository(context: ctx)
        policyRepo      = PolicyRepository()
        badgeRepo       = BadgeRepository(context: ctx, firestore: firestoreRepo,
                                          transactionRepo: transactionRepo,
                                          budgetRepo: budgetRepo,
                                          goalRepo: goalRepo,
                                          debtRepo: debtRepo)
        syncRepo        = SyncRepository(context: ctx, firestore: firestoreRepo,
                                          transactionRepo: transactionRepo,
                                          budgetRepo: budgetRepo,
                                          goalRepo: goalRepo,
                                          debtRepo: debtRepo,
                                          categoryRepo: categoryRepo,
                                          badgeRepo: badgeRepo)
        let authRepo    = AuthRepository(session: SessionManager.shared,
                                         firestore: firestoreRepo,
                                         sync: syncRepo)
        authViewModel   = AuthViewModel(authRepo: authRepo,
                                        policyRepo: policyRepo,
                                        session: SessionManager.shared)
    }

    // Convenience ViewModel factories

    func makeHomeVM()     -> HomeViewModel {
        HomeViewModel(transactionRepo: transactionRepo, budgetRepo: budgetRepo,
                      goalRepo: goalRepo, categoryRepo: categoryRepo)
    }
    func makeExpenseVM()  -> ExpenseViewModel {
        ExpenseViewModel(transactionRepo: transactionRepo, categoryRepo: categoryRepo,
                         badgeRepo: badgeRepo, storageRepo: storageRepo)
    }
    func makeBudgetVM()   -> BudgetViewModel {
        BudgetViewModel(budgetRepo: budgetRepo, categoryRepo: categoryRepo,
                        transactionRepo: transactionRepo)
    }
    func makeGoalsVM()    -> GoalsViewModel  { GoalsViewModel(goalRepo: goalRepo) }
    func makeDebtVM()     -> DebtViewModel   { DebtViewModel(debtRepo: debtRepo) }
    func makeReportsVM()  -> ReportsViewModel {
        ReportsViewModel(transactionRepo: transactionRepo, budgetRepo: budgetRepo,
                         categoryRepo: categoryRepo)
    }
    func makeProfileVM()  -> ProfileViewModel {
        let authRepo = AuthRepository(session: SessionManager.shared,
                                      firestore: firestoreRepo, sync: syncRepo)
        return ProfileViewModel(authRepo: authRepo, storageRepo: storageRepo,
                                syncRepo: syncRepo, session: SessionManager.shared)
    }
}
