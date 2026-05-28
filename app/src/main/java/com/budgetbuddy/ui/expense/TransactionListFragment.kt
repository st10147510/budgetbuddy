package com.budgetbuddy.ui.expense

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
import com.budgetbuddy.databinding.FragmentTransactionListBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class TransactionListFragment : Fragment() {

    private var _binding: FragmentTransactionListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ExpenseViewModel by viewModels()

    @Inject lateinit var session: SessionManager

    private val adapter = TransactionAdapter(onItemClick = { item ->
        findNavController().navigate(
            R.id.transactionDetailFragment,
            android.os.Bundle().apply { putLong("transactionId", item.transaction.id) }
        )
    })

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentTransactionListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnBack.setOnClickListener { findNavController().navigateUp() }
        binding.rvTransactions.layoutManager = LinearLayoutManager(requireContext())
        binding.rvTransactions.adapter = adapter

        val userId = session.userId ?: return
        viewModel.loadAllTransactions(userId)

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                combine(
                    viewModel.transactions,
                    viewModel.categories
                ) { transactions, categories ->
                    val catMap = categories.associateBy { it.id }
                    transactions.map { tx ->
                        val cat = catMap[tx.categoryId]
                        TransactionWithCategory(tx, cat?.name ?: "Other", cat?.icon ?: "📦")
                    }
                }.collect { items ->
                    adapter.submitList(items)
                }
            }
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
