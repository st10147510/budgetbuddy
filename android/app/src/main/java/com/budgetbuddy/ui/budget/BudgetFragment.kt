package com.budgetbuddy.ui.budget

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import com.budgetbuddy.R
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.budgetbuddy.data.local.SessionManager
import com.budgetbuddy.data.local.entities.CategoryEntity
import com.budgetbuddy.data.repository.CategoryRepository
import com.budgetbuddy.databinding.DialogAddBudgetBinding
import com.budgetbuddy.databinding.FragmentBudgetBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class BudgetFragment : Fragment() {

    private var _binding: FragmentBudgetBinding? = null
    private val binding get() = _binding!!

    private val viewModel: BudgetViewModel by viewModels()

    @Inject lateinit var session: SessionManager
    @Inject lateinit var categoryRepository: CategoryRepository

    private val adapter = BudgetAdapter(
        onItemClick = { bws ->
            findNavController().navigate(
                R.id.budgetDetailFragment,
                android.os.Bundle().apply {
                    putLong("budgetId", bws.budget.id)
                    putFloat("spent", bws.spent.toFloat())
                }
            )
        },
        onDelete = { viewModel.deleteBudget(it.budget) }
    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentBudgetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.rvBudgets.layoutManager = LinearLayoutManager(requireContext())
        binding.rvBudgets.adapter = adapter

        val userId = session.userId ?: return
        viewModel.loadBudgets(userId)

        binding.fabAddBudget.setOnClickListener { showAddBudgetDialog(userId) }

        binding.emptyState.tvEmptyTitle.setText(R.string.no_budgets_title)
        binding.emptyState.tvEmptySubtitle.setText(R.string.no_budgets_sub)
        binding.emptyState.tvEmptySubtitle.visibility = View.VISIBLE

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.budgetsWithSpend.collect { budgets ->
                    adapter.submitList(budgets)
                    binding.rvBudgets.visibility = if (budgets.isEmpty()) View.GONE else View.VISIBLE
                    binding.emptyState.root.visibility = if (budgets.isEmpty()) View.VISIBLE else View.GONE
                }
            }
        }
    }

    private fun showAddBudgetDialog(userId: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            val categories = categoryRepository.getAllCategories().first()
            if (categories.isEmpty()) return@launch

            val dialog = BottomSheetDialog(requireContext())
            val dialogBinding = DialogAddBudgetBinding.inflate(layoutInflater)
            dialog.setContentView(dialogBinding.root)
            dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
            val sheet = dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            sheet?.setBackgroundResource(android.R.color.transparent)

            var selectedCategory: CategoryEntity = categories[0]
            val labels = categories.map { "${it.icon} ${it.name}" }
            val dropdownAdapter = ArrayAdapter(requireContext(), R.layout.item_dropdown, labels)
            dialogBinding.actvCategory.setAdapter(dropdownAdapter)
            dialogBinding.actvCategory.setText(labels[0], false)
            dialogBinding.actvCategory.setOnItemClickListener { _, _, position, _ ->
                selectedCategory = categories[position]
            }

            dialogBinding.btnSave.setOnClickListener {
                val limit = dialogBinding.etAmount.text.toString().toDoubleOrNull() ?: 0.0
                val min = dialogBinding.etMinAmount.text.toString().toDoubleOrNull() ?: 0.0
                if (limit > 0) {
                    viewModel.saveBudget(userId, selectedCategory.id, limit, min)
                    dialog.dismiss()
                }
            }
            dialogBinding.btnCancel.setOnClickListener { dialog.dismiss() }
            dialog.show()
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
