package com.budgetbuddy.ui.debt

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.budgetbuddy.data.local.entities.DebtEntity
import com.budgetbuddy.data.local.entities.PayoffStrategy
import com.budgetbuddy.data.repository.DebtPayoffMonth
import com.budgetbuddy.data.repository.DebtRepository
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
class PaymentPlanViewModelTest {

    @get:Rule
    val instantTaskRule = InstantTaskExecutorRule()

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var debtRepository: DebtRepository
    private lateinit var viewModel: PaymentPlanViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        debtRepository = mock()
        viewModel = PaymentPlanViewModel(debtRepository, testDispatcher)
    }

    @After
    fun tearDown() { Dispatchers.resetMain() }

    private fun debt(id: Long, balance: Double, rate: Double = 12.0, minPayment: Double = 100.0) =
        DebtEntity(id = id, userId = "u1", name = "Debt$id",
            originalBalance = balance, balance = balance,
            interestRate = rate, minimumPayment = minPayment)

    private fun fakeSchedule(months: Int, debtName: String = "Debt1", payment: Double = 100.0): List<DebtPayoffMonth> =
        (1..months).map { DebtPayoffMonth(it, debtName, payment, maxOf(0.0, months - it.toDouble()) * payment) }

    // ---- loadDebts ----

    @Test
    fun `initial uiState has no summaries and isEmpty=false`() {
        val state = viewModel.uiState.value
        assertNull(state.snowball)
        assertNull(state.avalanche)
        assertFalse(state.isEmpty)
    }

    @Test
    fun `loadDebts with empty list sets isEmpty=true`() = runTest {
        whenever(debtRepository.getActiveDebts("u1")).thenReturn(flowOf(emptyList()))
        viewModel.loadDebts("u1")
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.isEmpty)
    }

    @Test
    fun `loadDebts with debts sets isEmpty=false`() = runTest {
        whenever(debtRepository.getActiveDebts("u1")).thenReturn(flowOf(listOf(debt(1, 1000.0))))
        viewModel.loadDebts("u1")
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.isEmpty)
    }

    // ---- calculatePlan ----

    @Test
    fun `calculatePlan with empty debts sets isEmpty=true and leaves summaries null`() = runTest {
        whenever(debtRepository.getActiveDebts("u1")).thenReturn(flowOf(emptyList()))
        viewModel.loadDebts("u1")
        viewModel.calculatePlan(0.0)
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.isEmpty)
        assertNull(viewModel.uiState.value.snowball)
    }

    @Test
    fun `calculatePlan populates both snowball and avalanche summaries`() = runTest {
        val debts = listOf(debt(1, 1000.0))
        whenever(debtRepository.getActiveDebts("u1")).thenReturn(flowOf(debts))
        whenever(debtRepository.computePayoffSchedule(any(), eq(PayoffStrategy.SNOWBALL), any()))
            .thenReturn(fakeSchedule(10))
        whenever(debtRepository.computePayoffSchedule(any(), eq(PayoffStrategy.AVALANCHE), any()))
            .thenReturn(fakeSchedule(9))

        viewModel.loadDebts("u1")
        advanceUntilIdle()
        viewModel.calculatePlan(0.0)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNotNull(state.snowball)
        assertNotNull(state.avalanche)
        assertEquals(PayoffStrategy.SNOWBALL, state.snowball!!.strategy)
        assertEquals(PayoffStrategy.AVALANCHE, state.avalanche!!.strategy)
        assertEquals(10, state.snowball!!.totalMonths)
        assertEquals(9, state.avalanche!!.totalMonths)
    }

    @Test
    fun `totalInterestPaid is totalPaid minus sum of current balances`() = runTest {
        val debts = listOf(debt(1, 500.0))
        val schedule = fakeSchedule(5, payment = 100.0)
        whenever(debtRepository.getActiveDebts("u1")).thenReturn(flowOf(debts))
        whenever(debtRepository.computePayoffSchedule(any(), any(), any())).thenReturn(schedule)

        viewModel.loadDebts("u1")
        advanceUntilIdle()
        viewModel.calculatePlan(0.0)
        advanceUntilIdle()

        val summary = viewModel.uiState.value.snowball!!
        val expectedInterest = maxOf(0.0, summary.totalPaid - 500.0)
        assertEquals(expectedInterest, summary.totalInterestPaid, 0.01)
    }

    @Test
    fun `calculatePlan with more extra payment returns fewer months`() = runTest {
        val debts = listOf(debt(1, 2400.0))
        whenever(debtRepository.getActiveDebts("u1")).thenReturn(flowOf(debts))
        whenever(debtRepository.computePayoffSchedule(any(), any(), eq(0.0)))
            .thenReturn(fakeSchedule(24))
        whenever(debtRepository.computePayoffSchedule(any(), any(), eq(200.0)))
            .thenReturn(fakeSchedule(10))

        viewModel.loadDebts("u1")
        advanceUntilIdle()

        viewModel.calculatePlan(0.0)
        advanceUntilIdle()
        val monthsWithout = viewModel.uiState.value.snowball!!.totalMonths

        viewModel.calculatePlan(200.0)
        advanceUntilIdle()
        val monthsWith = viewModel.uiState.value.snowball!!.totalMonths

        assertTrue(monthsWith < monthsWithout)
    }

    @Test
    fun `calculatePlan isLoading is false after completion`() = runTest {
        val debts = listOf(debt(1, 500.0))
        whenever(debtRepository.getActiveDebts("u1")).thenReturn(flowOf(debts))
        whenever(debtRepository.computePayoffSchedule(any(), any(), any())).thenReturn(fakeSchedule(5))

        viewModel.loadDebts("u1")
        advanceUntilIdle()
        viewModel.calculatePlan(0.0)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLoading)
    }

    // ---- selectStrategy ----

    @Test
    fun `selectStrategy SNOWBALL populates schedule with headers and debt rows`() = runTest {
        val debts = listOf(debt(1, 500.0))
        whenever(debtRepository.getActiveDebts("u1")).thenReturn(flowOf(debts))
        whenever(debtRepository.computePayoffSchedule(any(), any(), any())).thenReturn(fakeSchedule(3))

        viewModel.loadDebts("u1")
        advanceUntilIdle()
        viewModel.calculatePlan(0.0)
        advanceUntilIdle()
        viewModel.selectStrategy(PayoffStrategy.SNOWBALL)

        val rows = viewModel.uiState.value.schedule
        assertTrue(rows.isNotEmpty())
        assertTrue(rows.any { it is ScheduleRow.Header })
        assertTrue(rows.any { it is ScheduleRow.DebtRow })
    }

    @Test
    fun `schedule header count equals totalMonths`() = runTest {
        val debts = listOf(debt(1, 300.0))
        val months = 3
        whenever(debtRepository.getActiveDebts("u1")).thenReturn(flowOf(debts))
        whenever(debtRepository.computePayoffSchedule(any(), any(), any())).thenReturn(fakeSchedule(months))

        viewModel.loadDebts("u1")
        advanceUntilIdle()
        viewModel.calculatePlan(0.0)
        advanceUntilIdle()
        viewModel.selectStrategy(PayoffStrategy.SNOWBALL)

        val headerCount = viewModel.uiState.value.schedule.filterIsInstance<ScheduleRow.Header>().size
        assertEquals(months, headerCount)
    }

    @Test
    fun `selectStrategy switches selection without re-running calculation`() = runTest {
        val debts = listOf(debt(1, 500.0))
        whenever(debtRepository.getActiveDebts("u1")).thenReturn(flowOf(debts))
        whenever(debtRepository.computePayoffSchedule(any(), any(), any())).thenReturn(fakeSchedule(5))

        viewModel.loadDebts("u1")
        advanceUntilIdle()
        viewModel.calculatePlan(0.0)
        advanceUntilIdle()

        viewModel.selectStrategy(PayoffStrategy.AVALANCHE)
        assertEquals(PayoffStrategy.AVALANCHE, viewModel.uiState.value.selectedStrategy)

        viewModel.selectStrategy(PayoffStrategy.SNOWBALL)
        assertEquals(PayoffStrategy.SNOWBALL, viewModel.uiState.value.selectedStrategy)

        // computePayoffSchedule called exactly twice (once for each strategy in calculatePlan)
        verify(debtRepository, times(2)).computePayoffSchedule(any(), any(), any())
    }
}
