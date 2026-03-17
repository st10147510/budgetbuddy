package com.budgetbuddy.ui.reports

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.budgetbuddy.data.local.SessionManager
import com.budgetbuddy.databinding.FragmentReportsBinding
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ReportsFragment : Fragment() {

    private var _binding: FragmentReportsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ReportsViewModel by viewModels()

    @Inject lateinit var session: SessionManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentReportsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupCharts()

        val userId = session.userId ?: return
        viewModel.loadReports(userId)

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    binding.tvTotalIncome.text = "R %.2f".format(state.totalIncome)
                    binding.tvTotalExpense.text = "R %.2f".format(state.totalExpense)
                    updatePieChart(state.categorySpends)
                }
            }
        }
    }

    private fun setupCharts() {
        binding.pieChart.apply {
            description.isEnabled = false
            isDrawHoleEnabled = true
            holeRadius = 50f
            setHoleColor(Color.TRANSPARENT)
            legend.isEnabled = true
            setEntryLabelColor(Color.WHITE)
            setEntryLabelTextSize(11f)
        }
        binding.lineChart.apply {
            description.isEnabled = false
            setNoDataText("No data available")
            setNoDataTextColor(Color.WHITE)
        }
    }

    private fun updatePieChart(spends: List<CategorySpend>) {
        if (spends.isEmpty()) { binding.pieChart.setNoDataText("No expenses this month"); binding.pieChart.invalidate(); return }
        val entries = spends.map { PieEntry(it.amount.toFloat(), it.name) }
        val colors = spends.map { try { Color.parseColor(it.colorHex) } catch (e: Exception) { Color.GRAY } }
        val dataSet = PieDataSet(entries, "").apply { this.colors = colors; valueTextColor = Color.WHITE; valueTextSize = 11f }
        binding.pieChart.data = PieData(dataSet)
        binding.pieChart.invalidate()
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
