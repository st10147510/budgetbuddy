package com.budgetbuddy.data.repository

import com.budgetbuddy.data.local.dao.*
import com.budgetbuddy.data.local.entities.*
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*

class SyncRepositoryTest {

    private lateinit var firestoreRepository: FirestoreRepository
    private lateinit var transactionDao: TransactionDao
    private lateinit var budgetDao: BudgetDao
    private lateinit var goalDao: GoalDao
    private lateinit var debtDao: DebtDao
    private lateinit var categoryDao: CategoryDao
    private lateinit var badgeDao: BadgeDao
    private lateinit var repository: SyncRepository

    @Before
    fun setup() {
        firestoreRepository = mock()
        transactionDao = mock()
        budgetDao = mock()
        goalDao = mock()
        debtDao = mock()
        categoryDao = mock()
        badgeDao = mock()
        repository = SyncRepository(firestoreRepository, transactionDao, budgetDao, goalDao, debtDao, categoryDao, badgeDao)
    }

    @Test
    fun `syncFromFirestore upserts all collections into Room`() = runTest {
        val tx = TransactionEntity(id=1, userId="u1", amount=10.0, categoryId=1L, date=1000L, type=TransactionType.EXPENSE)
        val budget = BudgetEntity(id=1, userId="u1", categoryId=1L, limitAmount=500.0, minAmount=0.0, month=5, year=2026)
        val goal = GoalEntity(id=1, userId="u1", name="Car", targetAmount=10000.0, savedAmount=1000.0)
        val debt = DebtEntity(id=1, userId="u1", name="Loan", originalBalance=5000.0, balance=5000.0, interestRate=5.0, minimumPayment=100.0)
        val category = CategoryEntity(id=10, name="Travel", icon="✈️", colorHex="#2196F3", isDefault=false)
        val badge = BadgeEntity(id=0, userId="u1", badgeType=BadgeType.FIRST_STEP, earnedAt=System.currentTimeMillis())

        whenever(firestoreRepository.getTransactions("u1")).thenReturn(listOf(tx))
        whenever(firestoreRepository.getBudgets("u1")).thenReturn(listOf(budget))
        whenever(firestoreRepository.getGoals("u1")).thenReturn(listOf(goal))
        whenever(firestoreRepository.getDebts("u1")).thenReturn(listOf(debt))
        whenever(firestoreRepository.getCategories("u1")).thenReturn(listOf(category))
        whenever(firestoreRepository.getBadges("u1")).thenReturn(listOf(badge))

        repository.syncFromFirestore("u1")

        verify(transactionDao).insertTransaction(tx)
        verify(budgetDao).insertOrUpdateBudget(budget)
        verify(goalDao).insertGoal(goal)
        verify(debtDao).insertDebt(debt)
        verify(categoryDao).insertCategory(category)
        verify(badgeDao).insertBadge(badge)
    }

    @Test
    fun `syncFromFirestore is a no-op when Firestore returns empty collections`() = runTest {
        whenever(firestoreRepository.getTransactions("u1")).thenReturn(emptyList())
        whenever(firestoreRepository.getBudgets("u1")).thenReturn(emptyList())
        whenever(firestoreRepository.getGoals("u1")).thenReturn(emptyList())
        whenever(firestoreRepository.getDebts("u1")).thenReturn(emptyList())
        whenever(firestoreRepository.getCategories("u1")).thenReturn(emptyList())
        whenever(firestoreRepository.getBadges("u1")).thenReturn(emptyList())

        repository.syncFromFirestore("u1")

        verify(transactionDao, never()).insertTransaction(any())
        verify(budgetDao, never()).insertOrUpdateBudget(any())
        verify(goalDao, never()).insertGoal(any())
        verify(debtDao, never()).insertDebt(any())
        verify(categoryDao, never()).insertCategory(any())
        verify(badgeDao, never()).insertBadge(any())
    }

    // ---- syncToFirestore tests ----

    @Test
    fun `syncToFirestore pushes all local collections to Firestore`() = runTest {
        val tx = TransactionEntity(id=1, userId="u1", amount=10.0, categoryId=1L, date=1000L, type=TransactionType.EXPENSE)
        val budget = BudgetEntity(id=1, userId="u1", categoryId=1L, limitAmount=500.0, minAmount=0.0, month=5, year=2026)
        val goal = GoalEntity(id=1, userId="u1", name="Car", targetAmount=10000.0, savedAmount=1000.0)
        val debt = DebtEntity(id=1, userId="u1", name="Loan", originalBalance=5000.0, balance=5000.0, interestRate=5.0, minimumPayment=100.0)
        val category = CategoryEntity(id=10, name="Travel", icon="✈️", colorHex="#2196F3", isDefault=false)
        val badge = BadgeEntity(id=0, userId="u1", badgeType=BadgeType.FIRST_STEP, earnedAt=System.currentTimeMillis())

        whenever(transactionDao.getAllTransactionsOnce("u1")).thenReturn(listOf(tx))
        whenever(budgetDao.getAllBudgetsOnce("u1")).thenReturn(listOf(budget))
        whenever(goalDao.getAllGoalsOnce("u1")).thenReturn(listOf(goal))
        whenever(debtDao.getAllDebtsOnce("u1")).thenReturn(listOf(debt))
        whenever(categoryDao.getNonDefaultCategories()).thenReturn(listOf(category))
        whenever(badgeDao.getAllBadgesOnce("u1")).thenReturn(listOf(badge))

        repository.syncToFirestore("u1")

        verify(firestoreRepository).saveTransaction("u1", tx)
        verify(firestoreRepository).saveBudget("u1", budget)
        verify(firestoreRepository).saveGoal("u1", goal)
        verify(firestoreRepository).saveDebt("u1", debt)
        verify(firestoreRepository).saveCategory("u1", category)
        verify(firestoreRepository).saveBadge("u1", badge)
    }

    @Test
    fun `syncToFirestore is a no-op when Room collections are empty`() = runTest {
        whenever(transactionDao.getAllTransactionsOnce("u1")).thenReturn(emptyList())
        whenever(budgetDao.getAllBudgetsOnce("u1")).thenReturn(emptyList())
        whenever(goalDao.getAllGoalsOnce("u1")).thenReturn(emptyList())
        whenever(debtDao.getAllDebtsOnce("u1")).thenReturn(emptyList())
        whenever(categoryDao.getNonDefaultCategories()).thenReturn(emptyList())
        whenever(badgeDao.getAllBadgesOnce("u1")).thenReturn(emptyList())

        repository.syncToFirestore("u1")

        verify(firestoreRepository, never()).saveTransaction(any(), any())
        verify(firestoreRepository, never()).saveBudget(any(), any())
        verify(firestoreRepository, never()).saveGoal(any(), any())
        verify(firestoreRepository, never()).saveDebt(any(), any())
        verify(firestoreRepository, never()).saveCategory(any(), any())
        verify(firestoreRepository, never()).saveBadge(any(), any())
    }

    @Test
    fun `syncToFirestore continues pushing remaining collections on partial failure`() = runTest {
        val budget = BudgetEntity(id=1, userId="u1", categoryId=1L, limitAmount=500.0, minAmount=0.0, month=5, year=2026)

        whenever(transactionDao.getAllTransactionsOnce("u1")).thenThrow(RuntimeException("db error"))
        whenever(budgetDao.getAllBudgetsOnce("u1")).thenReturn(listOf(budget))
        whenever(goalDao.getAllGoalsOnce("u1")).thenReturn(emptyList())
        whenever(debtDao.getAllDebtsOnce("u1")).thenReturn(emptyList())
        whenever(categoryDao.getNonDefaultCategories()).thenReturn(emptyList())
        whenever(badgeDao.getAllBadgesOnce("u1")).thenReturn(emptyList())

        // All entity types are attempted; exception is thrown after all operations complete
        org.junit.Assert.assertThrows(Exception::class.java) {
            kotlinx.coroutines.runBlocking { repository.syncToFirestore("u1") }
        }

        verify(firestoreRepository, never()).saveTransaction(any(), any())
        verify(firestoreRepository).saveBudget("u1", budget)
    }

    @Test
    fun `syncFromFirestore continues on partial collection failure`() = runTest {
        whenever(firestoreRepository.getTransactions("u1")).thenThrow(RuntimeException("network error"))
        whenever(firestoreRepository.getBudgets("u1")).thenReturn(emptyList())
        whenever(firestoreRepository.getGoals("u1")).thenReturn(emptyList())
        whenever(firestoreRepository.getDebts("u1")).thenReturn(emptyList())
        whenever(firestoreRepository.getCategories("u1")).thenReturn(emptyList())
        whenever(firestoreRepository.getBadges("u1")).thenReturn(emptyList())

        // All entity types are attempted; exception is thrown after all operations complete
        org.junit.Assert.assertThrows(Exception::class.java) {
            kotlinx.coroutines.runBlocking { repository.syncFromFirestore("u1") }
        }

        verify(transactionDao, never()).insertTransaction(any())
        // remaining collections still attempted
        verify(firestoreRepository).getBudgets("u1")
    }
}
