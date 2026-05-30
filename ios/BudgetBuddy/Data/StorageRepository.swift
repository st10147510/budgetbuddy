import Foundation
import FirebaseStorage
import UIKit

final class StorageRepository {
    private let storage = Storage.storage()

    func uploadProfilePhoto(userId: String, image: UIImage) async -> Result<String, Error> {
        guard let data = image.jpegData(compressionQuality: 0.8) else {
            return .failure(AppError.invalidData)
        }
        let ref = storage.reference().child("profile_photos/\(userId).jpg")
        let meta = StorageMetadata()
        meta.contentType = "image/jpeg"
        do {
            _ = try await ref.putDataAsync(data, metadata: meta)
            let url = try await ref.downloadURL()
            return .success(url.absoluteString)
        } catch {
            return .failure(error)
        }
    }

    func uploadReceipt(userId: String, image: UIImage) async -> Result<String, Error> {
        guard let data = image.jpegData(compressionQuality: 0.85) else {
            return .failure(AppError.invalidData)
        }
        let filename = "\(UUID().uuidString).jpg"
        let ref = storage.reference().child("receipts/\(userId)/\(filename)")
        let meta = StorageMetadata()
        meta.contentType = "image/jpeg"
        do {
            _ = try await ref.putDataAsync(data, metadata: meta)
            let url = try await ref.downloadURL()
            return .success(url.absoluteString)
        } catch {
            return .failure(error)
        }
    }

    func uploadStatement(userId: String, data: Data, filename: String) async -> Result<String, Error> {
        let ref = storage.reference().child("bank_statements/\(userId)/\(filename)")
        let meta = StorageMetadata()
        meta.contentType = "application/pdf"
        do {
            _ = try await ref.putDataAsync(data, metadata: meta)
            let url = try await ref.downloadURL()
            return .success(url.absoluteString)
        } catch {
            return .failure(error)
        }
    }
}

enum AppError: LocalizedError {
    case invalidData
    case notAuthenticated
    case unknown(String)

    var errorDescription: String? {
        switch self {
        case .invalidData:       return "Invalid data."
        case .notAuthenticated:  return "You must be signed in."
        case .unknown(let msg):  return msg
        }
    }
}
