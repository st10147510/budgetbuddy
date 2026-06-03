import SwiftUI
import UniformTypeIdentifiers

struct UploadStatementView: View {
    let userId: String
    @Environment(\.dismiss) var dismiss

    @State private var showFilePicker  = false
    @State private var jobs: [StatementJobDTO] = []
    @State private var isUploading = false
    @State private var errorMsg: String?
    @State private var successMsg: String?

    var body: some View {
        NavigationStack {
            List {
                Section {
                    Button {
                        showFilePicker = true
                    } label: {
                        Label("Upload PDF Statement", systemImage: "doc.badge.plus")
                            .foregroundColor(.teal)
                    }
                    .disabled(isUploading)
                }

                if let err = errorMsg {
                    Section { Text(err).foregroundColor(.red).font(.caption) }
                }
                if let ok = successMsg {
                    Section { Label(ok, systemImage: "checkmark.circle.fill").foregroundColor(.green).font(.caption) }
                }

                if !jobs.isEmpty {
                    Section("Recent Uploads") {
                        ForEach(jobs) { job in
                            VStack(alignment: .leading, spacing: 4) {
                                Text(job.filename).font(.subheadline.bold())
                                HStack {
                                    StatusBadge(status: job.status)
                                    Spacer()
                                    if let rows = job.rowsImported {
                                        Text("\(rows) transactions imported").font(.caption).foregroundStyle(.secondary)
                                    }
                                }
                                if let err = job.error {
                                    Text(err).font(.caption).foregroundColor(.red)
                                }
                            }
                            .padding(.vertical, 2)
                        }
                    }
                }
            }
            .navigationTitle("Bank Statements")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) { Button("Close") { dismiss() } }
                ToolbarItem(placement: .primaryAction) {
                    Button { loadJobs() } label: { Image(systemName: "arrow.clockwise") }
                }
            }
            .fileImporter(isPresented: $showFilePicker, allowedContentTypes: [.pdf]) { result in
                handleFile(result)
            }
            .task { loadJobs() }
            .overlay { if isUploading { ProgressView("Uploading…") } }
        }
    }

    private func loadJobs() {
        Task {
            jobs = (try? await ApiService.shared.listStatements()) ?? []
        }
    }

    private func handleFile(_ result: Result<URL, Error>) {
        switch result {
        case .failure(let e): errorMsg = e.localizedDescription
        case .success(let url):
            guard url.startAccessingSecurityScopedResource() else {
                errorMsg = "Cannot access file."
                return
            }
            defer { url.stopAccessingSecurityScopedResource() }
            guard let data = try? Data(contentsOf: url) else {
                errorMsg = "Could not read file."
                return
            }
            isUploading = true
            errorMsg    = nil
            successMsg  = nil
            Task {
                do {
                    let dto = try await ApiService.shared.uploadStatement(pdfData: data, filename: url.lastPathComponent)
                    successMsg  = "Statement #\(dto.id) queued for processing."
                    isUploading = false
                    loadJobs()
                } catch {
                    errorMsg    = error.localizedDescription
                    isUploading = false
                }
            }
        }
    }
}

private struct StatusBadge: View {
    let status: String
    var color: Color {
        switch status {
        case "done":       return .green
        case "failed":     return .red
        case "processing": return .orange
        default:           return .secondary
        }
    }
    var body: some View {
        Text(status.capitalized)
            .font(.caption2.bold())
            .padding(.horizontal, 8).padding(.vertical, 3)
            .background(color.opacity(0.15))
            .foregroundColor(color)
            .cornerRadius(6)
    }
}
