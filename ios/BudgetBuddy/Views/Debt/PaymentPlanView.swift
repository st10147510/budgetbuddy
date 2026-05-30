import SwiftUI
import Charts

struct PaymentPlanView: View {
    @ObservedObject var vm: DebtViewModel
    @Environment(\.dismiss) var dismiss

    @State private var strategy: PayoffStrategy = .avalanche
    @State private var extraStr = ""

    private var schedule: [DebtPayoffMonth] { vm.payoffSchedule }

    private var monthsToPayoff: Int {
        schedule.map(\.month).max() ?? 0
    }

    private var totalInterest: Double {
        schedule.reduce(0) { $0 + $1.payment } - vm.totalOwed
    }

    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(alignment: .leading, spacing: 20) {

                    // Strategy picker
                    VStack(alignment: .leading, spacing: 8) {
                        Text("Payoff Strategy").font(.headline)
                        Picker("Strategy", selection: $strategy) {
                            ForEach(PayoffStrategy.allCases, id: \.self) { s in
                                Text(s == .snowball ? "Snowball" : "Avalanche").tag(s)
                            }
                        }
                        .pickerStyle(.segmented)
                        Text(strategy == .snowball
                             ? "Pay smallest balance first — quick wins build momentum."
                             : "Pay highest interest first — minimises total interest paid.")
                            .font(.caption)
                            .foregroundStyle(.secondary)
                    }
                    .padding()
                    .cardStyle()

                    // Extra payment
                    VStack(alignment: .leading, spacing: 8) {
                        Text("Extra Monthly Payment").font(.headline)
                        HStack {
                            Text("R").foregroundStyle(.secondary)
                            TextField("0.00", text: $extraStr).keyboardType(.decimalPad)
                        }
                        .padding(12)
                        .background(Color(.secondarySystemBackground))
                        .cornerRadius(10)
                    }
                    .padding()
                    .cardStyle()

                    // Summary
                    if !schedule.isEmpty {
                        VStack(alignment: .leading, spacing: 8) {
                            Text("Summary").font(.headline)
                            HStack(spacing: 0) {
                                SummaryItem(label: "Months", value: "\(monthsToPayoff)")
                                Divider().frame(height: 36)
                                SummaryItem(label: "Total Interest", value: max(0, totalInterest).currencyFormatted)
                            }
                        }
                        .padding()
                        .cardStyle()

                        // Balance chart per debt
                        let debtNames = Array(Set(schedule.map(\.debtName))).sorted()
                        VStack(alignment: .leading, spacing: 8) {
                            Text("Balance Over Time").font(.headline)
                            Chart {
                                ForEach(debtNames, id: \.self) { name in
                                    let series = schedule.filter { $0.debtName == name }
                                    ForEach(series) { point in
                                        LineMark(
                                            x: .value("Month", point.month),
                                            y: .value("Balance", point.remainingBalance)
                                        )
                                        .foregroundStyle(by: .value("Debt", name))
                                    }
                                }
                            }
                            .frame(height: 200)
                            .chartXAxisLabel("Month")
                            .chartYAxisLabel("Balance (R)")
                        }
                        .padding()
                        .cardStyle()

                        // Schedule table
                        VStack(alignment: .leading, spacing: 8) {
                            Text("Schedule").font(.headline)
                            ForEach(schedule.prefix(24)) { row in
                                HStack {
                                    Text("Mo \(row.month)").font(.caption).frame(width: 50, alignment: .leading)
                                    Text(row.debtName).font(.caption).lineLimit(1).frame(maxWidth: .infinity, alignment: .leading)
                                    Text(row.payment.currencyFormatted).font(.caption.bold()).frame(width: 80, alignment: .trailing)
                                    Text(row.remainingBalance.currencyFormatted).font(.caption).foregroundStyle(.secondary).frame(width: 80, alignment: .trailing)
                                }
                                Divider()
                            }
                            if schedule.count > 24 {
                                Text("… and \(schedule.count - 24) more months").font(.caption).foregroundStyle(.secondary)
                            }
                        }
                        .padding()
                        .cardStyle()
                    }
                }
                .padding(16)
            }
            .navigationTitle("Payment Plan")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .confirmationAction) { Button("Done") { dismiss() } }
            }
            .onChange(of: strategy)  { _, _ in recalculate() }
            .onChange(of: extraStr)  { _, _ in recalculate() }
            .onAppear { recalculate() }
        }
    }

    private func recalculate() {
        vm.computePayoffSchedule(strategy: strategy, extraPayment: Double(extraStr) ?? 0)
    }
}

private struct SummaryItem: View {
    let label: String
    let value: String
    var body: some View {
        VStack(spacing: 4) {
            Text(label).font(.caption).foregroundStyle(.secondary)
            Text(value).font(.headline)
        }
        .frame(maxWidth: .infinity)
    }
}
