import Foundation
import FirebaseAuth

// Mirrors Android's BudgetBuddyApiService (Retrofit interface)
final class ApiService {
    static let shared = ApiService()

    private let base = URL(string: "https://thebudgetbuddy.co.za/api/v1")!
    private let session = URLSession.shared

    private func authorizedRequest(path: String, method: String = "GET") async throws -> URLRequest {
        guard let token = try await Auth.auth().currentUser?.getIDToken() else {
            throw AppError.notAuthenticated
        }
        var req = URLRequest(url: base.appendingPathComponent(path))
        req.httpMethod = method
        req.setValue("Bearer \(token)", forHTTPHeaderField: "Authorization")
        req.setValue("application/json", forHTTPHeaderField: "Accept")
        return req
    }

    // MARK: - Statements

    func listStatements() async throws -> [StatementJobDTO] {
        let req = try await authorizedRequest(path: "statements")
        let (data, _) = try await session.data(for: req)
        return try JSONDecoder().decode(StatementListResponse.self, from: data).data
    }

    func uploadStatement(pdfData: Data, filename: String, defaultCategoryId: String? = nil) async throws -> StatementUploadDTO {
        var req = try await authorizedRequest(path: "statements", method: "POST")
        let boundary = UUID().uuidString
        req.setValue("multipart/form-data; boundary=\(boundary)", forHTTPHeaderField: "Content-Type")
        var body = Data()
        body.append("--\(boundary)\r\nContent-Disposition: form-data; name=\"file\"; filename=\"\(filename)\"\r\nContent-Type: application/pdf\r\n\r\n".data(using: .utf8)!)
        body.append(pdfData)
        body.append("\r\n--\(boundary)--\r\n".data(using: .utf8)!)
        req.httpBody = body
        let (data, _) = try await session.data(for: req)
        let resp = try JSONDecoder().decode(StatementUploadResponse.self, from: data)
        return resp.data
    }

    func getStatement(id: Int) async throws -> StatementJobDTO {
        let req = try await authorizedRequest(path: "statements/\(id)")
        let (data, _) = try await session.data(for: req)
        return try JSONDecoder().decode(StatementJobResponse.self, from: data).data
    }

    // MARK: - Policies

    func getPolicyVersions() async throws -> PolicyVersionsDTO {
        var req = URLRequest(url: base.appendingPathComponent("policies/current"))
        req.setValue("application/json", forHTTPHeaderField: "Accept")
        let (data, _) = try await session.data(for: req)
        return try JSONDecoder().decode(PolicyVersionsResponse.self, from: data).data
    }

    func acceptPolicy(type: String) async throws {
        var req = try await authorizedRequest(path: "policies/accept", method: "POST")
        req.setValue("application/json", forHTTPHeaderField: "Content-Type")
        req.httpBody = try JSONEncoder().encode(["type": type])
        _ = try await session.data(for: req)
    }
}

// MARK: - DTOs

struct StatementJobDTO: Codable, Identifiable {
    let id: Int
    let filename: String
    let status: String
    let rowsImported: Int?
    let error: String?
    let createdAt: String

    enum CodingKeys: String, CodingKey {
        case id, filename, status, error
        case rowsImported = "rows_imported"
        case createdAt    = "created_at"
    }
}

struct StatementUploadDTO: Codable {
    let id: Int
    let filename: String
    let status: String
}

struct StatementListResponse: Codable    { let data: [StatementJobDTO] }
struct StatementUploadResponse: Codable  { let message: String; let data: StatementUploadDTO }
struct StatementJobResponse: Codable     { let data: StatementJobDTO }

struct PolicyVersionsDTO: Codable {
    let terms: String
    let privacy: String
}

struct PolicyVersionsResponse: Codable { let data: PolicyVersionsDTO }
