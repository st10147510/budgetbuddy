import Foundation

final class SessionManager {
    static let shared = SessionManager()
    private let defaults = UserDefaults.standard

    private enum Key {
        static let userId      = "bb_userId"
        static let displayName = "bb_displayName"
        static let email       = "bb_email"
        static let photoUrl    = "bb_photoUrl"
    }

    var userId: String? {
        get { defaults.string(forKey: Key.userId) }
        set { defaults.set(newValue, forKey: Key.userId) }
    }
    var displayName: String? {
        get { defaults.string(forKey: Key.displayName) }
        set { defaults.set(newValue, forKey: Key.displayName) }
    }
    var email: String? {
        get { defaults.string(forKey: Key.email) }
        set { defaults.set(newValue, forKey: Key.email) }
    }
    var photoUrl: String? {
        get { defaults.string(forKey: Key.photoUrl) }
        set { defaults.set(newValue, forKey: Key.photoUrl) }
    }

    var isLoggedIn: Bool { userId != nil }

    func clear() {
        [Key.userId, Key.displayName, Key.email, Key.photoUrl].forEach {
            defaults.removeObject(forKey: $0)
        }
    }
}
