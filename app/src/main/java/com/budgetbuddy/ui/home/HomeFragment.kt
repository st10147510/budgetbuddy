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
import com.budgetbuddy.databinding.FragmentHomeBinding
import com.budgetbuddy.ui.expense.TransactionAdapter
import com.budgetbuddy.util.DateUtils
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
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

    @Inject lateinit var auth: FirebaseAuth

    private val adapter = TransactionAdapter()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rvTransactions.layoutManager = LinearLayoutManager(requireContext())
        binding.rvTransactions.adapter = adapter

        val user = auth.currentUser
        binding.tvUserName.text = user?.displayName ?: user?.email?.substringBefore('@') ?: "User"
        binding.tvMonth.text = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date())

        binding.btnAddExpense.setOnClickListener {
            findNavController().navigate(R.id.addExpenseFragment)
        }

        binding.tvSeeAll.setOnClickListener {
            findNavController().navigate(R.id.transactionListFragment)
        }

        user?.uid?.let { uid ->
            viewModel.init(uid)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    binding.tvMonthlyTotal.text = "R %.2f".format(state.totalSpendThisMonth)
                    val categories = viewModel.categories.value
                    val catMap = categories.associateBy { it.id }
                    val items = state.recentTransactions.map { tx ->
                        val cat = catMap[tx.categoryId]
                        com.budgetbuddy.ui.expense.TransactionWithCategory(
                            transaction = tx,
                            categoryName = cat?.name ?: "Other",
                            categoryIcon = cat?.icon ?: "📦"
                        )
                    }
                    adapter.submitList(items)
                    binding.tvEmptyState.visibility =
                        if (state.recentTransactions.isEmpty()) View.VISIBLE else View.GONE
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
