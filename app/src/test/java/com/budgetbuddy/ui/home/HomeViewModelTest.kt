package com.budgetbuddy.ui.home

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import app.cash.turbine.test
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
import org.mockito.kotlin.*

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    @get:Rule
    val instantTaskRule = InstantTaskExecutorRule()

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var transactionRepository: TransactionRepository
    private lateinit var categoryRepository: CategoryRepository
    private lateinit var budgetRepository: BudgetRepository
    private lateinit var viewModel: HomeViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        transactionRepository = mock()
        categoryRepository = mock()
        budgetRepository = mock()
        whenever(categoryRepository.getAllCategories()).thenReturn(flowOf(emptyList()))
        viewModel = HomeViewModel(transactionRepository, categoryRepository, budgetRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun tx(id: Long, amount: Double, type: TransactionType = TransactionType.EXPENSE) =
        TransactionEntity(
            id = id, userId = "user1", amount = amount, categoryId = 1L,
            date = System.currentTimeMillis(), type = type
        )

    @Test
    fun `initial state has zero totalSpend and empty transactions`() {
        assertEquals(0.0, viewModel.uiState.value.totalSpendThisMonth, 0.001)
        assertTrue(viewModel.uiState.value.recentTransactions.isEmpty())
    }

    @Test
    fun `init loads monthly total from repository`() = runTest {
        val transactions = listOf(tx(1L, 200.0), tx(2L, 300.0))
        whenever(transactionRepository.getRecentTransactions("user1", 5))
            .thenReturn(flowOf(transactions))
        whenever(transactionRepository.getTotalExpenseForPeriod(eq("user1"), any(), any()))
            .thenReturn(500.0)

        viewModel.init("user1")
        advanceUntilIdle()

        assertEquals(500.0, viewModel.uiState.value.totalSpendThisMonth, 0.001)
    }

    @Test
    fun `init loads recent transactions`() = runTest {
        val transactions = listOf(tx(1L, 100.0), tx(2L, 200.0))
        whenever(transactionRepository.getRecentTransactions("user1", 5))
            .thenReturn(flowOf(transactions))
        whenever(transactionRepository.getTotalExpenseForPeriod(eq("user1"), any(), any()))
            .thenReturn(300.0)

        viewModel.init("user1")
        advanceUntilIdle()

        assertEquals(2, viewModel.uiState.value.recentTransactions.size)
    }

    @Test
    fun `refresh reloads data`() = runTest {
        whenever(transactionRepository.getRecentTransactions("user1", 5))
            .thenReturn(flowOf(emptyList()))
        whenever(transactionRepository.getTotalExpenseForPeriod(eq("user1"), any(), any()))
            .thenReturn(0.0)

        viewModel.refresh("user1")
        advanceUntilIdle()

        verify(transactionRepository, atLeastOnce()).getRecentTransactions("user1", 5)
    }
}
