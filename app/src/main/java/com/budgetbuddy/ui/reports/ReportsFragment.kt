package com.budgetbuddy.ui.reports

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.budgetbuddy.R
import com.budgetbuddy.data.local.SessionManager
import com.budgetbuddy.databinding.FragmentReportsBinding
import com.budgetbuddy.ui.expense.TransactionAdapter
import com.budgetbuddy.ui.expense.TransactionWithCategory
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class ReportsFragment : Fragment() {

    private var _binding: FragmentReportsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ReportsViewModel by viewModels()

    @Inject lateinit var session: SessionManager

    private val transactionAdapter = TransactionAdapter()

    private val monthLabels = listOf("Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec")
    private var selectedMonthIndex = Calendar.getInstance().get(Calendar.MONTH)
    private var selectedYear = Calendar.getInstance().get(Calendar.YEAR)
    private val monthChips = mutableListOf<TextView>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentReportsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rvTransactions.layoutManager = LinearLayoutManager(requireContext())
        binding.rvTransactions.adapter = transactionAdapter

        setupLineChart()
        setupMonthPicker()

        val userId = session.userId ?: return
        viewModel.loadReports(userId)

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                combine(viewModel.uiState, viewModel.categories) { state, categories ->
                    val catMap = categories.associateBy { it.id }
                    val items = state.transactions.map { tx ->
                        val cat = catMap[tx.categoryId]
                        TransactionWithCategory(tx, cat?.name ?: "Other", cat?.icon ?: "📦")
                    }
                    Pair(state, items)
                }.collect { (state, items) ->
                    // Balance = income − expense for selected month
                    binding.tvBalance.text = "R %.2f".format(state.balance)

                    // Month label
                    binding.tvSelectedMonth.text = SimpleDateFormat("MMM yyyy", Locale.getDefault())
                        .format(Calendar.getInstance().apply {
                            set(selectedYear, selectedMonthIndex, 1)
                        }.time)

                    // Line chart
                    if (state.monthlyTotals.isNotEmpty()) updateLineChart(state.monthlyTotals)

                    // Transactions
                    transactionAdapter.submitList(items)
                    binding.tvEmptyState.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
                    binding.rvTransactions.visibility = if (items.isEmpty()) View.GONE else View.VISIBLE
                }
            }
        }
    }

    private fun setupLineChart() {
        binding.lineChart.apply {
            description.isEnabled = false
            legend.isEnabled = false
            setTouchEnabled(false)
            setDrawGridBackground(false)
            setBackgroundColor(Color.TRANSPARENT)
            setNoDataText("")
            xAxis.isEnabled = false
            axisLeft.isEnabled = false
            axisRight.isEnabled = false
            setViewPortOffsets(0f, 0f, 0f, 0f)
        }
    }

    private fun updateLineChart(totals: List<MonthTotal>) {
        val entries = totals.mapIndexed { i, m -> Entry(i.toFloat(), m.total) }
        val teal = Color.parseColor("#6EDCD3")
        val dataSet = LineDataSet(entries, "").apply {
            color = teal
            lineWidth = 3f
            setDrawCircles(false)
            setDrawValues(false)
            mode = LineDataSet.Mode.CUBIC_BEZIER
            setDrawFilled(true)
            fillColor = teal
            fillAlpha = 40
            highLightColor = Color.WHITE
            setDrawHighlightIndicators(false)
        }
        binding.lineChart.data = LineData(dataSet)
        binding.lineChart.invalidate()
    }

    private fun setupMonthPicker() {
        val now = Calendar.getInstance()
        val currentMonth = now.get(Calendar.MONTH)
        val currentYear = now.get(Calendar.YEAR)

        for (i in 0..11) {
            val chip = TextView(requireContext()).apply {
                text = monthLabels[i]
                textSize = 14f
                gravity = Gravity.CENTER
                setPadding(28, 12, 28, 12)
                setTextColor(if (i == selectedMonthIndex) Color.parseColor("#0D0D0D") else Color.parseColor("#888888"))
                background = if (i == selectedMonthIndex)
                    ContextCompat.getDrawable(requireContext(), R.drawable.bg_pill_cream)
                else null
                setOnClickListener { selectMonth(i, currentYear) }
            }
            monthChips.add(chip)
            binding.monthPickerRow.addView(chip)
        }
    }

    private fun selectMonth(monthIndex: Int, year: Int) {
        // Update chip styles
        monthChips.forEachIndexed { i, chip ->
            chip.setTextColor(if (i == monthIndex) Color.parseColor("#0D0D0D") else Color.parseColor("#888888"))
            chip.background = if (i == monthIndex)
                ContextCompat.getDrawable(requireContext(), R.drawable.bg_pill_cream)
            else null
        }
        selectedMonthIndex = monthIndex
        selectedYear = year
        viewModel.selectMonth(monthIndex, year)
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
