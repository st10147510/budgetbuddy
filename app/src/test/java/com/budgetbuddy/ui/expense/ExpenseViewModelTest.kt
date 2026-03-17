package com.budgetbuddy.ui.expense

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import app.cash.turbine.test
import com.budgetbuddy.data.local.entities.TransactionEntity
import com.budgetbuddy.data.local.entities.TransactionType
import com.budgetbuddy.data.repository.BadgeRepository
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
class ExpenseViewModelTest {

    @get:Rule
    val instantTaskRule = InstantTaskExecutorRule()

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var transactionRepository: TransactionRepository
    private lateinit var categoryRepository: CategoryRepository
    private lateinit var badgeRepository: BadgeRepository
    private lateinit var viewModel: ExpenseViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        transactionRepository = mock()
        categoryRepository = mock()
        badgeRepository = mock()
        whenever(categoryRepository.getAllCategories()).thenReturn(flowOf(emptyList()))
        viewModel = ExpenseViewModel(transactionRepository, categoryRepository, badgeRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is Idle`() {
        assertEquals(ExpenseUiState.Idle, viewModel.uiState.value)
    }

    @Test
    fun `saveExpense with invalid amount emits Error`() = runTest {
        viewModel.uiState.test {
            viewModel.saveExpense("user1", -10.0, 1L, System.currentTimeMillis(), null, null)
            val state = awaitItem()
            assertTrue(state is ExpenseUiState.Error)
        }
    }

    @Test
    fun `saveExpense with invalid categoryId emits Error`() = runTest {
        viewModel.uiState.test {
            viewModel.saveExpense("user1", 100.0, -1L, System.currentTimeMillis(), null, null)
            val state = awaitItem()
            assertTrue(state is ExpenseUiState.Error)
        }
    }

    @Test
    fun `saveExpense success emits Saved and checks badges`() = runTest {
        whenever(transactionRepository.insertTransaction(any())).thenReturn(1L)

        viewModel.uiState.test {
            viewModel.saveExpense("user1", 100.0, 1L, System.currentTimeMillis(), "Lunch", null)
            skipItems(1) // Loading
            val state = awaitItem()
            assertTrue(state is ExpenseUiState.Saved)
        }

        advanceUntilIdle()
        verify(badgeRepository).checkAndAwardBadges("user1")
    }

    @Test
    fun `deleteExpense emits Deleted state`() = runTest {
        val tx = TransactionEntity(id = 1L, userId = "user1", amount = 100.0,
            categoryId = 1L, date = System.currentTimeMillis(), type = TransactionType.EXPENSE)

        viewModel.uiState.test {
            viewModel.deleteExpense(tx)
            skipItems(1) // Loading
            val state = awaitItem()
            assertTrue(state is ExpenseUiState.Deleted)
        }

        verify(transactionRepository).deleteTransaction(tx)
    }

    @Test
    fun `resetState sets state back to Idle`() = runTest {
        whenever(transactionRepository.insertTransaction(any())).thenReturn(1L)
        viewModel.saveExpense("user1", 100.0, 1L, System.currentTimeMillis(), null, null)
        advanceUntilIdle()

        viewModel.resetState()
        assertEquals(ExpenseUiState.Idle, viewModel.uiState.value)
    }
}
