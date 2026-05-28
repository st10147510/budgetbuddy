package com.budgetbuddy.ui.home

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
import com.budgetbuddy.databinding.FragmentHomeBinding
import com.budgetbuddy.ui.expense.TransactionAdapter
import com.budgetbuddy.ui.expense.TransactionWithCategory
import com.budgetbuddy.util.CurrencyFormatter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels()

    @Inject lateinit var session: SessionManager

    private val adapter = TransactionAdapter()
    private val goalAdapter = GoalPreviewAdapter()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rvTransactions.layoutManager = LinearLayoutManager(requireContext())
        binding.rvTransactions.adapter = adapter

        binding.rvGoals.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.rvGoals.adapter = goalAdapter

        binding.tvUserName.text = session.displayName ?: session.email?.substringBefore('@') ?: "User"
        binding.tvMonth.text = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date())

        binding.btnAddExpense.setOnClickListener { findNavController().navigate(R.id.addExpenseFragment) }
        binding.tvSeeAll.setOnClickListener { findNavController().navigate(R.id.transactionListFragment) }
        binding.tvGoalsViewAll.setOnClickListener { findNavController().navigate(R.id.goalsFragment) }

        val userId = session.userId ?: return
        viewModel.init(userId)

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                combine(viewModel.uiState, viewModel.categories) { state, categories ->
                    val catMap = categories.associateBy { it.id }
                    val items = state.recentTransactions.map { tx ->
                        val cat = catMap[tx.categoryId]
                        TransactionWithCategory(tx, cat?.name ?: "Other", cat?.icon ?: "📦")
                    }
                    Pair(state, items)
                }.collect { (state, items) ->
                    // Balance = income − expenses for the current month
                    binding.tvMonthlyTotal.text = CurrencyFormatter.format(requireContext(), state.balance)
                    adapter.submitList(items)
                    binding.tvEmptyState.visibility =
                        if (state.recentTransactions.isEmpty()) View.VISIBLE else View.GONE
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.goals.collect { goals ->
                    goalAdapter.submitList(goals)
                    binding.goalsSection.visibility = if (goals.isEmpty()) View.GONE else View.VISIBLE
                }
            }
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
