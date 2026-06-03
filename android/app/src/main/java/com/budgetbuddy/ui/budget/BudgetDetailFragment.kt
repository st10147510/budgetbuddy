package com.budgetbuddy.ui.budget

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.budgetbuddy.R
import com.budgetbuddy.databinding.DialogEditBudgetBinding
import com.budgetbuddy.databinding.FragmentBudgetDetailBinding
import com.budgetbuddy.util.CurrencyFormatter
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@AndroidEntryPoint
class BudgetDetailFragment : Fragment() {

    private var _binding: FragmentBudgetDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: BudgetDetailViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentBudgetDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val budgetId = arguments?.getLong("budgetId", -1L) ?: -1L
        val spent = (arguments?.getFloat("spent", 0f) ?: 0f).toDouble()
        if (budgetId <= 0) { findNavController().navigateUp(); return }

        viewModel.load(budgetId, spent)

        binding.btnBack.setOnClickListener { findNavController().navigateUp() }

        binding.btnEdit.setOnClickListener {
            viewModel.state.value.budget?.let { showEditDialog(it.limitAmount, it.minAmount) }
        }

        binding.btnDelete.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(getString(R.string.confirm_delete))
                .setMessage(getString(R.string.delete_budget_message))
                .setPositiveButton(getString(R.string.delete)) { _, _ -> viewModel.delete() }
                .setNegativeButton(getString(R.string.cancel), null)
                .show()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    val budget = state.budget ?: return@collect
                    val category = state.category
                    val spentAmount = state.spent
                    val pct = if (budget.limitAmount > 0)
                        ((spentAmount / budget.limitAmount) * 100).toInt().coerceIn(0, 150) else 0
                    val remaining = (budget.limitAmount - spentAmount).coerceAtLeast(0.0)

                    binding.tvCategoryIcon.text = category?.icon ?: "📦"
                    binding.tvCategoryName.text = category?.name ?: "Unknown"
                    binding.progressBudget.progress = pct.coerceIn(0, 100)

                    val cal = Calendar.getInstance()
                    cal.set(Calendar.MONTH, budget.month - 1)
                    cal.set(Calendar.YEAR, budget.year)
                    binding.tvPeriod.text = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(cal.time)
                    binding.tvSpent.text = CurrencyFormatter.format(requireContext(), spentAmount)
                    binding.tvLimit.text = CurrencyFormatter.format(requireContext(), budget.limitAmount)
                    binding.tvRemaining.text = CurrencyFormatter.format(requireContext(), remaining)
                    binding.tvMinGoal.text = if (budget.minAmount > 0) CurrencyFormatter.format(requireContext(), budget.minAmount) else "—"

                    val status = when {
                        pct >= 100 -> BudgetStatus.EXCEEDED
                        pct >= 80 -> BudgetStatus.WARNING
                        budget.minAmount > 0 && spentAmount < budget.minAmount -> BudgetStatus.UNDER_MIN
                        else -> BudgetStatus.OK
                    }
                    val (statusText, statusColor) = when (status) {
                        BudgetStatus.OK -> "On Track" to ContextCompat.getColor(requireContext(), R.color.green_ok)
                        BudgetStatus.WARNING -> "Near Limit" to ContextCompat.getColor(requireContext(), R.color.amber_warning)
                        BudgetStatus.EXCEEDED -> "Over Limit" to ContextCompat.getColor(requireContext(), R.color.red_danger)
                        BudgetStatus.UNDER_MIN -> "Below Goal" to ContextCompat.getColor(requireContext(), R.color.blue_info)
                    }
                    binding.tvStatus.text = statusText
                    binding.tvStatus.setTextColor(statusColor)
                    binding.tvPercent.text = "$pct%"
                    binding.tvPercent.setTextColor(statusColor)
                    binding.progressBudget.progressTintList = android.content.res.ColorStateList.valueOf(statusColor)
                    binding.tvRemaining.setTextColor(
                        if (pct >= 100) ContextCompat.getColor(requireContext(), R.color.red_danger)
                        else ContextCompat.getColor(requireContext(), R.color.green_ok)
                    )
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.finished.collect { if (it) findNavController().navigateUp() }
            }
        }
    }

    private fun showEditDialog(currentLimit: Double, currentMin: Double) {
        val dialog = BottomSheetDialog(requireContext())
        val db = DialogEditBudgetBinding.inflate(layoutInflater)
        dialog.setContentView(db.root)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        db.root.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            ?.setBackgroundResource(android.R.color.transparent)

        db.etAmount.setText(currentLimit.toBigDecimal().stripTrailingZeros().toPlainString())
        if (currentMin > 0) db.etMinAmount.setText(currentMin.toBigDecimal().stripTrailingZeros().toPlainString())

        db.btnSave.setOnClickListener {
            val limit = db.etAmount.text.toString().toDoubleOrNull() ?: 0.0
            val min = db.etMinAmount.text.toString().toDoubleOrNull() ?: 0.0
            if (limit > 0) {
                viewModel.update(limit, min)
                dialog.dismiss()
            }
        }
        db.btnCancel.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
