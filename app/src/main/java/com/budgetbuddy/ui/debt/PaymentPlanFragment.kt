package com.budgetbuddy.ui.debt

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.budgetbuddy.R
import com.budgetbuddy.data.local.SessionManager
import com.budgetbuddy.data.local.entities.PayoffStrategy
import com.budgetbuddy.databinding.FragmentPaymentPlanBinding
import com.budgetbuddy.util.CurrencyFormatter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class PaymentPlanFragment : Fragment() {

    private var _binding: FragmentPaymentPlanBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PaymentPlanViewModel by viewModels()
    private val scheduleAdapter = ScheduleAdapter()

    @Inject lateinit var session: SessionManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPaymentPlanBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val userId = session.userId ?: return

        binding.rvSchedule.layoutManager = LinearLayoutManager(requireContext())
        binding.rvSchedule.adapter = scheduleAdapter

        viewModel.loadDebts(userId)

        binding.btnBack.setOnClickListener { findNavController().navigateUp() }

        binding.btnCalculate.setOnClickListener {
            val extra = binding.etExtraPayment.text.toString().toDoubleOrNull() ?: 0.0
            viewModel.calculatePlan(extra)
        }

        binding.btnSelectSnowball.setOnClickListener {
            viewModel.selectStrategy(PayoffStrategy.SNOWBALL)
        }
        binding.btnSelectAvalanche.setOnClickListener {
            viewModel.selectStrategy(PayoffStrategy.AVALANCHE)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state -> render(state) }
            }
        }
    }

    private fun render(state: PaymentPlanUiState) {
        // Empty state
        if (state.isEmpty) {
            binding.tvEmptyDebts.visibility = View.VISIBLE
            binding.tilExtraPayment.visibility = View.GONE
            binding.btnCalculate.visibility = View.GONE
            binding.llStrategyCards.visibility = View.GONE
            return
        }
        binding.tvEmptyDebts.visibility = View.GONE
        binding.tilExtraPayment.visibility = View.VISIBLE
        binding.llStrategyCards.visibility = View.VISIBLE

        // Loading
        binding.btnCalculate.isEnabled = !state.isLoading
        binding.btnCalculate.text = if (state.isLoading)
            getString(R.string.plan_calculating) else getString(R.string.calculate_plan)

        // Snowball summary
        state.snowball?.let { s ->
            binding.tvSnowballMonths.text = getString(R.string.plan_months, s.totalMonths)
            binding.tvSnowballInterest.text = getString(R.string.plan_interest, CurrencyFormatter.format(requireContext(), s.totalInterestPaid))
            binding.tvSnowballTotal.text = getString(R.string.plan_total, CurrencyFormatter.format(requireContext(), s.totalPaid))
        }

        // Avalanche summary
        state.avalanche?.let { a ->
            binding.tvAvalancheMonths.text = getString(R.string.plan_months, a.totalMonths)
            binding.tvAvalancheInterest.text = getString(R.string.plan_interest, CurrencyFormatter.format(requireContext(), a.totalInterestPaid))
            binding.tvAvalancheTotal.text = getString(R.string.plan_total, CurrencyFormatter.format(requireContext(), a.totalPaid))
        }

        // Selected card highlight (4dp stroke = 12px at mdpi; use resources for density independence)
        val strokePx = (4 * resources.displayMetrics.density).toInt()
        binding.cardSnowball.strokeWidth =
            if (state.selectedStrategy == PayoffStrategy.SNOWBALL) strokePx else 0
        binding.cardAvalanche.strokeWidth =
            if (state.selectedStrategy == PayoffStrategy.AVALANCHE) strokePx else 0

        // Schedule
        if (state.schedule.isNotEmpty()) {
            binding.tvScheduleTitle.visibility = View.VISIBLE
            binding.rvSchedule.visibility = View.VISIBLE
            scheduleAdapter.submitList(state.schedule)
        } else {
            binding.tvScheduleTitle.visibility = View.GONE
            binding.rvSchedule.visibility = View.GONE
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
