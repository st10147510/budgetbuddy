package com.budgetbuddy.ui.expense

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.budgetbuddy.R
import com.budgetbuddy.data.local.entities.TransactionEntity
import com.budgetbuddy.data.local.entities.TransactionType
import com.budgetbuddy.data.repository.CategoryRepository
import com.budgetbuddy.data.repository.TransactionRepository
import com.budgetbuddy.databinding.FragmentTransactionDetailBinding
import com.budgetbuddy.util.CurrencyFormatter
import com.budgetbuddy.util.DateUtils
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TransactionDetailState(
    val transaction: TransactionEntity? = null,
    val categoryName: String = "",
    val categoryIcon: String = "📦",
    val finished: Boolean = false
)

@HiltViewModel
class TransactionDetailViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _state = MutableStateFlow(TransactionDetailState())
    val state: StateFlow<TransactionDetailState> = _state.asStateFlow()

    fun load(id: Long) {
        viewModelScope.launch {
            val tx = transactionRepository.getTransactionById(id) ?: return@launch
            val cat = categoryRepository.getCategoryById(tx.categoryId)
            _state.value = TransactionDetailState(tx, cat?.name ?: "Other", cat?.icon ?: "📦")
        }
    }

    fun delete() {
        val tx = _state.value.transaction ?: return
        viewModelScope.launch {
            transactionRepository.deleteTransaction(tx)
            _state.value = _state.value.copy(finished = true)
        }
    }
}

@AndroidEntryPoint
class TransactionDetailFragment : Fragment() {

    private var _binding: FragmentTransactionDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TransactionDetailViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentTransactionDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val transactionId = arguments?.getLong("transactionId", -1L) ?: -1L
        if (transactionId <= 0) { findNavController().navigateUp(); return }

        viewModel.load(transactionId)

        binding.btnBack.setOnClickListener { findNavController().navigateUp() }

        binding.btnEdit.setOnClickListener {
            findNavController().navigate(
                R.id.addExpenseFragment,
                bundleOf("transactionId" to transactionId)
            )
        }

        binding.btnDelete.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(getString(R.string.confirm_delete))
                .setMessage(getString(R.string.delete_transaction_message))
                .setPositiveButton(getString(R.string.delete)) { _, _ -> viewModel.delete() }
                .setNegativeButton(getString(R.string.cancel), null)
                .show()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    if (state.finished) { findNavController().navigateUp(); return@collect }
                    state.transaction?.let { render(it, state.categoryName, state.categoryIcon) }
                }
            }
        }
    }

    private fun render(tx: TransactionEntity, categoryName: String, categoryIcon: String) {
        val sign = if (tx.type == TransactionType.INCOME) "+" else "-"
        val color = if (tx.type == TransactionType.INCOME)
            requireContext().getColor(R.color.income_green)
        else requireContext().getColor(R.color.expense_red)

        binding.tvAmount.text = "$sign${CurrencyFormatter.format(requireContext(), tx.amount)}"
        binding.tvAmount.setTextColor(color)
        binding.tvType.text = if (tx.type == TransactionType.INCOME) "Income" else "Expense"
        binding.tvCategory.text = "$categoryIcon $categoryName"
        binding.tvDate.text = DateUtils.formatDate(tx.date)
        binding.tvNotes.text = tx.notes ?: getString(R.string.no_notes)
        binding.tvCreatedAt.text = DateUtils.formatDate(tx.createdAt)

        if (!tx.receiptImagePath.isNullOrBlank()) {
            binding.ivReceipt.visibility = View.VISIBLE
            binding.tvNoReceipt.visibility = View.GONE
            Glide.with(this)
                .load(tx.receiptImagePath)
                .placeholder(R.drawable.ic_camera)
                .into(binding.ivReceipt)
        } else {
            binding.ivReceipt.visibility = View.GONE
            binding.tvNoReceipt.visibility = View.VISIBLE
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
