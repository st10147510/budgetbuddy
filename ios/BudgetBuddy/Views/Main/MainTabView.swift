import SwiftUI

struct MainTabView: View {
    let userId: String
    @EnvironmentObject var container: AppContainer

    @StateObject private var homeVM: HomeViewModel
    @StateObject private var expenseVM: ExpenseViewModel
    @StateObject private var budgetVM: BudgetViewModel
    @StateObject private var goalsVM: GoalsViewModel
    @StateObject private var debtVM: DebtViewModel
    @StateObject private var reportsVM: ReportsViewModel
    @StateObject private var profileVM: ProfileViewModel

    init(userId: String) {
        self.userId = userId
        let c = AppContainer.shared
        _homeVM    = StateObject(wrappedValue: c.makeHomeVM())
        _expenseVM = StateObject(wrappedValue: c.makeExpenseVM())
        _budgetVM  = StateObject(wrappedValue: c.makeBudgetVM())
        _goalsVM   = StateObject(wrappedValue: c.makeGoalsVM())
        _debtVM    = StateObject(wrappedValue: c.makeDebtVM())
        _reportsVM = StateObject(wrappedValue: c.makeReportsVM())
        _profileVM = StateObject(wrappedValue: c.makeProfileVM())
    }

    var body: some View {
        TabView {
            HomeView(userId: userId, vm: homeVM)
                .tabItem { Label("Home",    systemImage: "house.fill") }

            TransactionListView(userId: userId, vm: expenseVM)
                .tabItem { Label("Expenses", systemImage: "creditcard.fill") }

            BudgetView(userId: userId, vm: budgetVM)
                .tabItem { Label("Budget",  systemImage: "chart.bar.fill") }

            GoalsView(userId: userId, vm: goalsVM)
                .tabItem { Label("Goals",   systemImage: "star.fill") }

            ProfileView(userId: userId, vm: profileVM)
                .tabItem { Label("Profile", systemImage: "person.fill") }
        }
        .tint(.teal)
        .onAppear { loadAll() }
    }

    private func loadAll() {
        homeVM.load(userId: userId)
        expenseVM.loadAll(userId: userId)
        budgetVM.load(userId: userId)
        goalsVM.load(userId: userId)
        debtVM.load(userId: userId)
        reportsVM.load(userId: userId)
    }
}
