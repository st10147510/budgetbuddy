import Foundation
import SwiftData

final class CategoryRepository {
    private let context: ModelContext

    init(context: ModelContext) { self.context = context }

    func all() throws -> [BBCategory] {
        try context.fetch(FetchDescriptor<BBCategory>(sortBy: [SortDescriptor(\.name)]))
    }

    func byId(_ id: UUID) throws -> BBCategory? {
        let pred = #Predicate<BBCategory> { $0.id == id }
        return try context.fetch(FetchDescriptor<BBCategory>(predicate: pred)).first
    }

    func insert(_ category: BBCategory) throws {
        context.insert(category)
        try context.save()
    }

    func delete(_ category: BBCategory) throws {
        context.delete(category)
        try context.save()
    }
}
