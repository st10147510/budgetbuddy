package com.budgetbuddy.data.repository

import com.budgetbuddy.data.local.dao.DebtDao
import com.budgetbuddy.data.local.entities.DebtEntity
import com.budgetbuddy.data.local.entities.PayoffStrategy
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*

class DebtRepositoryTest {

    private lateinit var debtDao: DebtDao
    private lateinit var repository: DebtRepository

    @Before
    fun setup() {
        debtDao = mock()
        repository = DebtRepository(debtDao)
    }

    private fun debt(id: Long, name: String, balance: Double, rate: Double, minPayment: Double) =
        DebtEntity(id = id, userId = "user1", name = name, originalBalance = balance, balance = balance,
            interestRate = rate, minimumPayment = minPayment)

    // ---- Payoff schedule tests ----

    @Test
    fun `computePayoffSchedule returns empty for empty debts`() {
        val result = repository.computePayoffSchedule(emptyList(), PayoffStrategy.SNOWBALL)
        assertTrue(result.isEmpty())
    }

    @Test
    fun `snowball strategy sorts by smallest balance first`() {
        val debts = listOf(
            debt(1, "Large", 5000.0, 15.0, 100.0),
            debt(2, "Small", 500.0, 20.0, 50.0)
        )
        val schedule = repository.computePayoffSchedule(debts, PayoffStrategy.SNOWBALL)
        // First payment should be for the small debt (500) which comes first in snowball
        assertEquals("Small", schedule.first().debtName)
    }

    @Test
    fun `avalanche strategy sorts by highest interest rate first`() {
        val debts = listOf(
            debt(1, "LowRate", 1000.0, 5.0, 50.0),
            debt(2, "HighRate", 1000.0, 25.0, 50.0)
        )
        val schedule = repository.computePayoffSchedule(debts, PayoffStrategy.AVALANCHE)
        assertEquals("HighRate", schedule.first().debtName)
    }

    @Test
    fun `schedule terminates when all balances reach zero`() {
        val debts = listOf(
            debt(1, "Card", 200.0, 0.0, 200.0) // pays off in 1 month
        )
        val schedule = repository.computePayoffSchedule(debts, PayoffStrategy.SNOWBALL)
        assertTrue(schedule.isNotEmpty())
        assertEquals(0.0, schedule.last().remainingBalance, 0.01)
    }

    @Test
    fun `extra payment reduces total months to payoff`() {
        val debts = listOf(
            debt(1, "Loan", 2400.0, 12.0, 100.0)
        )
        val withExtra = repository.computePayoffSchedule(debts, PayoffStrategy.SNOWBALL, extraPayment = 200.0)
        val withoutExtra = repository.computePayoffSchedule(debts, PayoffStrategy.SNOWBALL, extraPayment = 0.0)

        val lastMonthWith = withExtra.maxOf { it.month }
        val lastMonthWithout = withoutExtra.maxOf { it.month }
        assertTrue("Extra payment should reduce payoff time", lastMonthWith < lastMonthWithout)
    }

    @Test
    fun `schedule does not exceed 360 months`() {
        val debts = listOf(
            debt(1, "Huge", 1_000_000.0, 99.0, 1.0) // nearly impossible to pay off
        )
        val schedule = repository.computePayoffSchedule(debts, PayoffStrategy.SNOWBALL)
        assertTrue(schedule.all { it.month <= 360 })
    }

    @Test
    fun `payment does not exceed remaining balance plus interest`() {
        val debts = listOf(
            debt(1, "Small", 50.0, 10.0, 200.0) // min payment > balance
        )
        val schedule = repository.computePayoffSchedule(debts, PayoffStrategy.SNOWBALL)
        // Actual payment should be capped at balance + interest, not 200
        assertTrue(schedule.first().payment <= 60.0)
    }

    // ---- DAO delegation tests ----

    @Test
    fun `getActiveDebts delegates to dao`() = runTest {
        whenever(debtDao.getActiveDebts("user1")).thenReturn(flowOf(emptyList()))
        repository.getActiveDebts("user1")
        verify(debtDao).getActiveDebts("user1")
    }

    @Test
    fun `insertDebt delegates to dao`() = runTest {
        val d = debt(0, "Test", 100.0, 5.0, 10.0)
        whenever(debtDao.insertDebt(d)).thenReturn(1L)
        val id = repository.insertDebt(d)
        verify(debtDao).insertDebt(d)
        assertEquals(1L, id)
    }

    @Test
    fun `markDebtPaidOff delegates to dao`() = runTest {
        repository.markDebtPaidOff(5L)
        verify(debtDao).markDebtPaidOff(5L)
    }
}
