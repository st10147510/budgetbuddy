package com.budgetbuddy.ui.budget

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.budgetbuddy.R
import com.budgetbuddy.data.local.entities.CategoryEntity
import com.budgetbuddy.databinding.FragmentBudgetBinding
import com.budgetbuddy.data.repository.CategoryRepository
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class BudgetFragment : Fragment() {

    private var _binding: FragmentBudgetBinding? = null
    private val binding get() = _binding!!

    private val viewModel: BudgetViewModel by viewModels()

    @Inject lateinit var auth: FirebaseAuth
    @Inject lateinit var categoryRepository: CategoryRepository

    private val adapter = BudgetAdapter { budget ->
        viewModel.deleteBudget(budget.budget)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentBudgetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rvBudgets.layoutManager = LinearLayoutManager(requireContext())
        binding.rvBudgets.adapter = adapter

        val userId = auth.currentUser?.uid ?: return
        viewModel.loadBudgets(userId)

        binding.fabAddBudget.setOnClickListener { showAddBudgetDialog(userId) }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.budgetsWithSpend.collect { budgets ->
                    adapter.submitList(budgets)
                    binding.tvEmptyState.visibility =
                        if (budgets.isEmpty()) View.VISIBLE else View.GONE
                    binding.rvBudgets.visibility =
                        if (budgets.isEmpty()) View.GONE else View.VISIBLE
                }
            }
        }
    }

    private fun showAddBudgetDialog(userId: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            val categories = categoryRepository.getAllCategories().first()
            if (categories.isEmpty()) return@launch

            var selectedCategory: CategoryEntity = categories[0]
            val categoryNames = categories.map { "${it.icon} ${it.name}" }.toTypedArray()

            val dialogView = layoutInflater.inflate(android.R.layout.simple_list_item_1, null)
            // Simple dialog with category picker and amount
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Set Budget")
                .setSingleChoiceItems(categoryNames, 0) { _, which ->
                    selectedCategory = categories[which]
                }
                .setPositiveButton(R.string.save) { _, _ ->
                    // Show amount input
                    showAmountDialog(userId, selectedCategory)
                }
                .setNegativeButton(R.string.cancel, null)
                .show()
        }
    }

    private fun showAmountDialog(userId: String, category: CategoryEntity) {
        val input = TextInputEditText(requireContext()).apply {
            hint = getString(R.string.budget_limit)
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
        }
        val container = TextInputLayout(requireContext()).apply {
            prefixText = "R "
            addView(input)
            setPadding(48, 16, 48, 8)
        }
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("${category.icon} ${category.name}")
            .setView(container)
            .setPositiveButton(R.string.save) { _, _ ->
                val amount = input.text.toString().toDoubleOrNull() ?: return@setPositiveButton
                if (amount > 0) viewModel.saveBudget(userId, category.id, amount)
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
