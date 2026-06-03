import Foundation
import UIKit

enum ProfileState { case idle, loading, syncing, success(String), error(String) }

@MainActor
final class ProfileViewModel: ObservableObject {
    @Published var state: ProfileState = .idle
    @Published var displayName: String = ""
    @Published var email: String       = ""
    @Published var photoUrl: String?   = nil

    private let authRepo:    AuthRepository
    private let storageRepo: StorageRepository
    private let syncRepo:    SyncRepository
    private let session:     SessionManager

    init(authRepo: AuthRepository, storageRepo: StorageRepository,
         syncRepo: SyncRepository, session: SessionManager) {
        self.authRepo    = authRepo
        self.storageRepo = storageRepo
        self.syncRepo    = syncRepo
        self.session     = session
        reload()
    }

    func reload() {
        displayName = session.displayName ?? ""
        email       = session.email       ?? ""
        photoUrl    = session.photoUrl
    }

    func uploadPhoto(_ image: UIImage, userId: String) {
        state = .loading
        Task {
            let result = await storageRepo.uploadProfilePhoto(userId: userId, image: image)
            switch result {
            case .success(let url):
                await authRepo.updatePhotoUrl(userId: userId, url: url)
                photoUrl = url
                state    = .success("Profile photo updated.")
            case .failure(let e):
                state    = .error(e.localizedDescription)
            }
        }
    }

    func syncToCloud(userId: String) {
        state = .syncing
        Task {
            await syncRepo.syncToFirestore(userId: userId)
            state = .success("Data synced to cloud.")
        }
    }

    func signOut() { authRepo.signOut() }
}
