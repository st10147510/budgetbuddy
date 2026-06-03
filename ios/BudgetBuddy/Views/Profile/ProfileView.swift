import SwiftUI
import PhotosUI

struct ProfileView: View {
    let userId: String
    @ObservedObject var vm: ProfileViewModel
    @EnvironmentObject var authVM: AuthViewModel

    @State private var photoItem: PhotosPickerItem?
    @State private var showStatement = false
    @State private var showBadges    = false
    @State private var showReports   = false
    @State private var showDebt      = false

    var body: some View {
        NavigationStack {
            List {
                // ── Profile header ─────────────────────────────────────
                Section {
                    HStack(spacing: 16) {
                        PhotosPicker(selection: $photoItem, matching: .images) {
                            Group {
                                if let urlStr = vm.photoUrl, let url = URL(string: urlStr) {
                                    AsyncImage(url: url) { img in
                                        img.resizable().scaledToFill()
                                    } placeholder: { Color.teal.opacity(0.2) }
                                    .frame(width: 64, height: 64)
                                    .clipShape(Circle())
                                } else {
                                    Image(systemName: "person.circle.fill")
                                        .font(.system(size: 64))
                                        .foregroundColor(.teal)
                                }
                            }
                        }
                        VStack(alignment: .leading, spacing: 4) {
                            Text(vm.displayName).font(.headline)
                            Text(vm.email).font(.caption).foregroundStyle(.secondary)
                        }
                    }
                    .padding(.vertical, 4)
                }

                // ── Status ─────────────────────────────────────────────
                if case .loading  = vm.state { Section { ProgressView("Uploading…") } }
                if case .syncing  = vm.state { Section { ProgressView("Syncing…") } }
                if case .success(let msg) = vm.state {
                    Section { Label(msg, systemImage: "checkmark.circle.fill").foregroundColor(.green) }
                }
                if case .error(let msg) = vm.state {
                    Section { Text(msg).foregroundColor(.red).font(.caption) }
                }

                // ── Navigation links ───────────────────────────────────
                Section("Tools") {
                    Button { showReports   = true } label: {
                        Label("Reports", systemImage: "chart.xyaxis.line")
                    }
                    Button { showDebt      = true } label: {
                        Label("Debt Manager", systemImage: "creditcard.trianglebadge.exclamationmark")
                    }
                    Button { showBadges    = true } label: {
                        Label("Achievements", systemImage: "star.circle")
                    }
                    Button { showStatement = true } label: {
                        Label("Upload Statement", systemImage: "doc.badge.plus")
                    }
                }

                // ── Account actions ────────────────────────────────────
                Section("Account") {
                    Button { vm.syncToCloud(userId: userId) } label: {
                        Label("Sync to Cloud", systemImage: "icloud.and.arrow.up")
                    }
                    Button(role: .destructive) {
                        vm.signOut()
                        authVM.signOut()
                    } label: {
                        Label("Sign Out", systemImage: "rectangle.portrait.and.arrow.right")
                    }
                }
            }
            .navigationTitle("Profile")
            .onChange(of: photoItem) { _, item in
                Task {
                    if let data = try? await item?.loadTransferable(type: Data.self),
                       let img  = UIImage(data: data) {
                        vm.uploadPhoto(img, userId: userId)
                    }
                }
            }
            .sheet(isPresented: $showStatement) { UploadStatementView(userId: userId) }
            .sheet(isPresented: $showBadges)    { BadgesView(userId: userId) }
            .sheet(isPresented: $showReports) {
                let vm2 = AppContainer.shared.makeReportsVM()
                let _ = { vm2.load(userId: userId) }()
                ReportsView(userId: userId, vm: vm2)
            }
            .sheet(isPresented: $showDebt) {
                let vm2 = AppContainer.shared.makeDebtVM()
                let _ = { vm2.load(userId: userId) }()
                DebtView(userId: userId, vm: vm2)
            }
        }
    }
}
