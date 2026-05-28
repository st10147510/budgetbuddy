package com.budgetbuddy.ui.budget

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import app.cash.turbine.test
import com.budgetbuddy.data.local.entities.BudgetEntity
import com.budgetbuddy.data.local.entities.CategoryEntity
import com.budgetbuddy.data.local.entities.TransactionEntity
import com.budgetbuddy.data.local.entities.TransactionType
import com.budgetbuddy.data.repository.BudgetRepository
import com.budgetbuddy.data.repository.CategoryRepository
import com.budgetbuddy.data.repository.TransactionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class BudgetViewModelTest {

    @get:Rule
    val instantTaskRule = InstantTaskExecutorRule()

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var budgetRepository: BudgetRepository
    private lateinit var categoryRepository: CategoryRepository
    private lateinit var transactionRepository: TransactionRepository
    private lateinit var viewModel: BudgetViewModel

    private val testCategory = CategoryEntity(
        id = 1L, name = "Food", icon = "🛒", colorHex = "#4CAF50", isDefault = true
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        budgetRepository = mock()
        categoryRepository = mock()
        transactionRepository = mock()
        viewModel = BudgetViewModel(budgetRepository, categoryRepository, transactionRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun budget(id: Long, limit: Double, month: Int = 3, year: Int = 2026) =
        BudgetEntity(id = id, userId = "user1", categoryId = 1L, limitAmount = limit, month = month, year = year)

    private fun budgetWithMin(id: Long, limit: Double, min: Double, month: Int = 3, year: Int = 2026) =
        BudgetEntity(id = id, userId = "user1", categoryId = 1L, limitAmount = limit, minAmount = min, month = month, year = year)

    private fun expenseTx(amount: Double) =
        TransactionEntity(id = 0, userId = "user1", amount = amount, categoryId = 1L,
            date = System.currentTimeMillis(), type = TransactionType.EXPENSE)

    @Test
    fun `loadBudgets emits BudgetWithSpend with correct status OK`() = runTest {
        val budget = budget(1L, 1000.0)
        whenever(budgetRepository.getBudgetsForMonth(eq("user1"), any(), any()))
            .thenReturn(flowOf(listOf(budget)))
        whenever(categoryRepository.getCategoryById(1L)).thenReturn(testCategory)
        whenever(transactionRepository.getTransactionsByDateRange(eq("user1"), any(), any()))
            .thenReturn(flowOf(listOf(expenseTx(500.0)))) // 50% — OK

        viewModel.budgetsWithSpend.test {
            skipItems(1)
            viewModel.loadBudgets("user1")
            val result = awaitItem()
            assertEquals(1, result.size)
            assertEquals(BudgetStatus.OK, result[0].status)
            assertEquals(50, result[0].progressPercent)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `loadBudgets emits WARNING status at 85 percent`() = runTest {
        val budget = budget(1L, 1000.0)
        whenever(budgetRepository.getBudgetsForMonth(eq("user1"), any(), any()))
            .thenReturn(flowOf(listOf(budget)))
        whenever(categoryRepository.getCategoryById(1L)).thenReturn(testCategory)
        whenever(transactionRepository.getTransactionsByDateRange(eq("user1"), any(), any()))
            .thenReturn(flowOf(listOf(expenseTx(850.0)))) // 85%

        viewModel.budgetsWithSpend.test {
            skipItems(1)
            viewModel.loadBudgets("user1")
            val result = awaitItem()
            assertEquals(BudgetStatus.WARNING, result[0].status)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `loadBudgets emits EXCEEDED status at 100 percent`() = runTest {
        val budget = budget(1L, 1000.0)
        whenever(budgetRepository.getBudgetsForMonth(eq("user1"), any(), any()))
            .thenReturn(flowOf(listOf(budget)))
        whenever(categoryRepository.getCategoryById(1L)).thenReturn(testCategory)
        whenever(transactionRepository.getTransactionsByDateRange(eq("user1"), any(), any()))
            .thenReturn(flowOf(listOf(expenseTx(1100.0)))) // 110% — exceeded

        viewModel.budgetsWithSpend.test {
            skipItems(1)
            viewModel.loadBudgets("user1")
            val result = awaitItem()
            assertEquals(BudgetStatus.EXCEEDED, result[0].status)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `loadBudgets skips budgets with missing category`() = runTest {
        val budget = budget(1L, 1000.0)
        whenever(budgetRepository.getBudgetsForMonth(eq("user1"), any(), any()))
            .thenReturn(flowOf(listOf(budget)))
        whenever(categoryRepository.getCategoryById(1L)).thenReturn(null)
        whenever(transactionRepository.getTransactionsByDateRange(eq("user1"), any(), any()))
            .thenReturn(flowOf(emptyList()))

        viewModel.loadBudgets("user1")
        advanceUntilIdle()
        assertTrue(viewModel.budgetsWithSpend.value.isEmpty())
    }

    @Test
    fun `saveBudget calls repository insertOrUpdate`() = runTest {
        viewModel.saveBudget("user1", 1L, 500.0)
        advanceUntilIdle()
        verify(budgetRepository).insertOrUpdateBudget(any())
    }

    @Test
    fun `deleteBudget calls repository delete`() = runTest {
        val budget = budget(1L, 1000.0)
        viewModel.deleteBudget(budget)
        advanceUntilIdle()
        verify(budgetRepository).deleteBudget(budget)
    }

    // ---- UNDER_MIN status tests ----

    @Test
    fun `loadBudgets emits UNDER_MIN when spend is below minimum goal`() = runTest {
        val budget = budgetWithMin(1L, limit = 1000.0, min = 300.0)
        whenever(budgetRepository.getBudgetsForMonth(eq("user1"), any(), any()))
            .thenReturn(flowOf(listOf(budget)))
        whenever(categoryRepository.getCategoryById(1L)).thenReturn(testCategory)
        whenever(transactionRepository.getTransactionsByDateRange(eq("user1"), any(), any()))
            .thenReturn(flowOf(listOf(expenseTx(200.0)))) // 200 < 300 min, 20% of limit

        viewModel.budgetsWithSpend.test {
            skipItems(1)
            viewModel.loadBudgets("user1")
            val result = awaitItem()
            assertEquals(BudgetStatus.UNDER_MIN, result[0].status)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `UNDER_MIN is not triggered when minAmount is zero`() = runTest {
        val budget = budget(1L, 1000.0) // minAmount defaults to 0
        whenever(budgetRepository.getBudgetsForMonth(eq("user1"), any(), any()))
            .thenReturn(flowOf(listOf(budget)))
        whenever(categoryRepository.getCategoryById(1L)).thenReturn(testCategory)
        whenever(transactionRepository.getTransactionsByDateRange(eq("user1"), any(), any()))
            .thenReturn(flowOf(listOf(expenseTx(50.0))))

        viewModel.budgetsWithSpend.test {
            skipItems(1)
            viewModel.loadBudgets("user1")
            val result = awaitItem()
            assertEquals(BudgetStatus.OK, result[0].status)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `WARNING takes priority over UNDER_MIN when spend is 85 percent of limit`() = runTest {
        // spend = 850, min = 900 → below min BUT already WARNING
        val budget = budgetWithMin(1L, limit = 1000.0, min = 900.0)
        whenever(budgetRepository.getBudgetsForMonth(eq("user1"), any(), any()))
            .thenReturn(flowOf(listOf(budget)))
        whenever(categoryRepository.getCategoryById(1L)).thenReturn(testCategory)
        whenever(transactionRepository.getTransactionsByDateRange(eq("user1"), any(), any()))
            .thenReturn(flowOf(listOf(expenseTx(850.0))))

        viewModel.budgetsWithSpend.test {
            skipItems(1)
            viewModel.loadBudgets("user1")
            val result = awaitItem()
            assertEquals(BudgetStatus.WARNING, result[0].status)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `EXCEEDED takes priority over UNDER_MIN when spend is over limit`() = runTest {
        // absurd min > limit; spend exceeds limit
        val budget = budgetWithMin(1L, limit = 500.0, min = 2000.0)
        whenever(budgetRepository.getBudgetsForMonth(eq("user1"), any(), any()))
            .thenReturn(flowOf(listOf(budget)))
        whenever(categoryRepository.getCategoryById(1L)).thenReturn(testCategory)
        whenever(transactionRepository.getTransactionsByDateRange(eq("user1"), any(), any()))
            .thenReturn(flowOf(listOf(expenseTx(600.0))))

        viewModel.budgetsWithSpend.test {
            skipItems(1)
            viewModel.loadBudgets("user1")
            val result = awaitItem()
            assertEquals(BudgetStatus.EXCEEDED, result[0].status)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `OK when spend meets minimum goal`() = runTest {
        val budget = budgetWithMin(1L, limit = 1000.0, min = 200.0)
        whenever(budgetRepository.getBudgetsForMonth(eq("user1"), any(), any()))
            .thenReturn(flowOf(listOf(budget)))
        whenever(categoryRepository.getCategoryById(1L)).thenReturn(testCategory)
        whenever(transactionRepository.getTransactionsByDateRange(eq("user1"), any(), any()))
            .thenReturn(flowOf(listOf(expenseTx(200.0)))) // exactly at min

        viewModel.budgetsWithSpend.test {
            skipItems(1)
            viewModel.loadBudgets("user1")
            val result = awaitItem()
            assertEquals(BudgetStatus.OK, result[0].status)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `saveBudget with minAmount passes correct entity to repository`() = runTest {
        viewModel.saveBudget("user1", 1L, 500.0, 150.0)
        advanceUntilIdle()

        val captor = argumentCaptor<BudgetEntity>()
        verify(budgetRepository).insertOrUpdateBudget(captor.capture())
        assertEquals(150.0, captor.firstValue.minAmount, 0.01)
        assertEquals(500.0, captor.firstValue.limitAmount, 0.01)
        assertEquals("user1", captor.firstValue.userId)
    }

    @Test
    fun `saveBudget with no minAmount argument defaults to zero`() = runTest {
        viewModel.saveBudget("user1", 1L, 500.0) // minAmount omitted
        advanceUntilIdle()

        val captor = argumentCaptor<BudgetEntity>()
        verify(budgetRepository).insertOrUpdateBudget(captor.capture())
        assertEquals(0.0, captor.firstValue.minAmount, 0.01)
    }

    // ---- Existing progress cap test ----

    @Test
    fun `progressPercent is capped at 150 for large overspend`() = runTest {
        val budget = budget(1L, 1000.0)
        whenever(budgetRepository.getBudgetsForMonth(eq("user1"), any(), any()))
            .thenReturn(flowOf(listOf(budget)))
        whenever(categoryRepository.getCategoryById(1L)).thenReturn(testCategory)
        whenever(transactionRepository.getTransactionsByDateRange(eq("user1"), any(), any()))
            .thenReturn(flowOf(listOf(expenseTx(5000.0)))) // 500%

        viewModel.budgetsWithSpend.test {
            skipItems(1)
            viewModel.loadBudgets("user1")
            val result = awaitItem()
            assertEquals(150, result[0].progressPercent)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
