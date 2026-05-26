package com.budgetbuddy.ui.reports

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
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
import kotlinx.coroutines.runBlocking
import org.mockito.kotlin.any
import org.mockito.kotlin.clearInvocations
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class ReportsViewModelTest {

    @get:Rule
    val instantTaskRule = InstantTaskExecutorRule()

    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var transactionRepository: TransactionRepository
    private lateinit var categoryRepository: CategoryRepository
    private lateinit var budgetRepository: BudgetRepository
    private lateinit var viewModel: ReportsViewModel

    private val foodCategory = CategoryEntity(id = 1L, name = "Food", icon = "🛒", colorHex = "#4CAF50")
    private val transportCategory = CategoryEntity(id = 2L, name = "Transport", icon = "🚗", colorHex = "#2196F3")

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        transactionRepository = mock()
        categoryRepository = mock()
        budgetRepository = mock()

        // Default stubs — overridden in individual tests as needed
        whenever(transactionRepository.getTransactionsByDateRange(any(), any(), any()))
            .thenReturn(flowOf(emptyList()))
        whenever(budgetRepository.getBudgetsForMonth(any(), any(), any()))
            .thenReturn(flowOf(emptyList()))
        whenever(categoryRepository.getAllCategories()).thenReturn(flowOf(emptyList()))
        runBlocking {
            whenever(transactionRepository.getTotalExpenseForPeriod(any(), any(), any())).thenReturn(0.0)
            whenever(transactionRepository.getTotalIncomeForPeriod(any(), any(), any())).thenReturn(0.0)
        }

        viewModel = ReportsViewModel(transactionRepository, categoryRepository, budgetRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun expense(id: Long, amount: Double, categoryId: Long) =
        TransactionEntity(id = id, userId = "u1", amount = amount, categoryId = categoryId,
            date = 0L, type = TransactionType.EXPENSE)

    private fun income(id: Long, amount: Double) =
        TransactionEntity(id = id, userId = "u1", amount = amount, categoryId = 1L,
            date = 0L, type = TransactionType.INCOME)

    private fun budget(categoryId: Long, limit: Double, min: Double = 0.0) =
        BudgetEntity(id = 1L, userId = "u1", categoryId = categoryId,
            limitAmount = limit, minAmount = min, month = 5, year = 2026)

    // ─── CategoryBudgetBar construction ────────────────────────────────────────

    @Test
    fun `categoryBudgetBars is empty when no budgets are set`() = runTest {
        whenever(categoryRepository.getAllCategories()).thenReturn(flowOf(listOf(foodCategory)))
        whenever(transactionRepository.getTransactionsByDateRange(any(), any(), any()))
            .thenReturn(flowOf(listOf(expense(1L, 200.0, 1L))))
        // budgetRepository stub already returns emptyList

        viewModel.loadReports("u1")
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.categoryBudgetBars.isEmpty())
    }

    @Test
    fun `categoryBudgetBars contains one entry per budget`() = runTest {
        whenever(categoryRepository.getAllCategories())
            .thenReturn(flowOf(listOf(foodCategory, transportCategory)))
        whenever(transactionRepository.getTransactionsByDateRange(any(), any(), any()))
            .thenReturn(flowOf(listOf(expense(1L, 200.0, 1L), expense(2L, 80.0, 2L))))
        whenever(budgetRepository.getBudgetsForMonth(any(), any(), any()))
            .thenReturn(flowOf(listOf(budget(1L, 500.0), budget(2L, 300.0))))

        viewModel.loadReports("u1")
        advanceUntilIdle()

        assertEquals(2, viewModel.uiState.value.categoryBudgetBars.size)
    }

    @Test
    fun `categoryBudgetBars correctly joins spend with budget data`() = runTest {
        whenever(categoryRepository.getAllCategories()).thenReturn(flowOf(listOf(foodCategory)))
        whenever(transactionRepository.getTransactionsByDateRange(any(), any(), any()))
            .thenReturn(flowOf(listOf(expense(1L, 250.0, 1L))))
        whenever(budgetRepository.getBudgetsForMonth(any(), any(), any()))
            .thenReturn(flowOf(listOf(budget(1L, limit = 600.0, min = 100.0))))

        viewModel.loadReports("u1")
        advanceUntilIdle()

        val bar = viewModel.uiState.value.categoryBudgetBars[0]
        assertEquals("Food", bar.categoryName)
        assertEquals("🛒", bar.icon)
        assertEquals(250.0, bar.spent, 0.01)
        assertEquals(600.0, bar.limitAmount, 0.01)
        assertEquals(100.0, bar.minAmount, 0.01)
    }

    @Test
    fun `categoryBudgetBars shows zero spend for budgeted category with no transactions`() = runTest {
        whenever(categoryRepository.getAllCategories()).thenReturn(flowOf(listOf(foodCategory)))
        whenever(transactionRepository.getTransactionsByDateRange(any(), any(), any()))
            .thenReturn(flowOf(emptyList()))
        whenever(budgetRepository.getBudgetsForMonth(any(), any(), any()))
            .thenReturn(flowOf(listOf(budget(1L, 500.0))))

        viewModel.loadReports("u1")
        advanceUntilIdle()

        val bars = viewModel.uiState.value.categoryBudgetBars
        assertEquals(1, bars.size)
        assertEquals(0.0, bars[0].spent, 0.01)
    }

    @Test
    fun `categoryBudgetBars is sorted by spent descending`() = runTest {
        whenever(categoryRepository.getAllCategories())
            .thenReturn(flowOf(listOf(foodCategory, transportCategory)))
        whenever(transactionRepository.getTransactionsByDateRange(any(), any(), any()))
            .thenReturn(flowOf(listOf(
                expense(1L, 50.0, 1L),   // Food: 50
                expense(2L, 300.0, 2L)   // Transport: 300
            )))
        whenever(budgetRepository.getBudgetsForMonth(any(), any(), any()))
            .thenReturn(flowOf(listOf(budget(1L, 500.0), budget(2L, 400.0))))

        viewModel.loadReports("u1")
        advanceUntilIdle()

        val bars = viewModel.uiState.value.categoryBudgetBars
        assertTrue("Transport (300) should come before Food (50)", bars[0].spent >= bars[1].spent)
    }

    @Test
    fun `categories without budgets are excluded from categoryBudgetBars`() = runTest {
        whenever(categoryRepository.getAllCategories())
            .thenReturn(flowOf(listOf(foodCategory, transportCategory)))
        whenever(transactionRepository.getTransactionsByDateRange(any(), any(), any()))
            .thenReturn(flowOf(listOf(expense(1L, 200.0, 1L), expense(2L, 150.0, 2L))))
        whenever(budgetRepository.getBudgetsForMonth(any(), any(), any()))
            .thenReturn(flowOf(listOf(budget(categoryId = 1L, limit = 500.0)))) // only Food has budget

        viewModel.loadReports("u1")
        advanceUntilIdle()

        val bars = viewModel.uiState.value.categoryBudgetBars
        assertEquals(1, bars.size)
        assertEquals("Food", bars[0].categoryName)
    }

    // ─── Month offset ───────────────────────────────────────────────────────────

    @Test
    fun `selectMonth(0) queries budget repository with 1-based month 1`() = runTest {
        viewModel.loadReports("u1") // sets userId
        advanceUntilIdle()
        clearInvocations(budgetRepository)

        viewModel.selectMonth(0, 2026) // Calendar.MONTH 0 = January → DB month 1
        advanceUntilIdle()

        verify(budgetRepository).getBudgetsForMonth("u1", 1, 2026)
    }

    @Test
    fun `selectMonth(11) queries budget repository with 1-based month 12`() = runTest {
        viewModel.loadReports("u1")
        advanceUntilIdle()
        clearInvocations(budgetRepository)

        viewModel.selectMonth(11, 2026) // Calendar.MONTH 11 = December → DB month 12
        advanceUntilIdle()

        verify(budgetRepository).getBudgetsForMonth("u1", 12, 2026)
    }

    // ─── Balance and spend calculation ─────────────────────────────────────────

    @Test
    fun `balance equals income minus expense`() = runTest {
        whenever(categoryRepository.getAllCategories()).thenReturn(flowOf(listOf(foodCategory)))
        whenever(transactionRepository.getTransactionsByDateRange(any(), any(), any()))
            .thenReturn(flowOf(listOf(income(1L, 3000.0), expense(2L, 800.0, 1L))))

        viewModel.loadReports("u1")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(2200.0, state.balance, 0.01)
        assertEquals(800.0, state.totalExpense, 0.01)
        assertEquals(3000.0, state.totalIncome, 0.01)
    }

    @Test
    fun `categorySpends excludes income transactions`() = runTest {
        whenever(categoryRepository.getAllCategories()).thenReturn(flowOf(listOf(foodCategory)))
        whenever(transactionRepository.getTransactionsByDateRange(any(), any(), any()))
            .thenReturn(flowOf(listOf(income(1L, 5000.0), expense(2L, 400.0, 1L))))

        viewModel.loadReports("u1")
        advanceUntilIdle()

        val spends = viewModel.uiState.value.categorySpends
        assertEquals(1, spends.size)
        assertEquals(400.0, spends[0].amount, 0.01)
    }

    @Test
    fun `categorySpends groups transactions by category and sums amounts`() = runTest {
        whenever(categoryRepository.getAllCategories()).thenReturn(flowOf(listOf(foodCategory)))
        whenever(transactionRepository.getTransactionsByDateRange(any(), any(), any()))
            .thenReturn(flowOf(listOf(expense(1L, 100.0, 1L), expense(2L, 250.0, 1L))))

        viewModel.loadReports("u1")
        advanceUntilIdle()

        val spends = viewModel.uiState.value.categorySpends
        assertEquals(1, spends.size)
        assertEquals(350.0, spends[0].amount, 0.01)
    }
}
