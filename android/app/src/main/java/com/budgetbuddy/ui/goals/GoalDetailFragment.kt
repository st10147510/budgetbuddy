package com.budgetbuddy.ui.goals

import android.app.DatePickerDialog
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
import com.budgetbuddy.R
import com.budgetbuddy.data.local.entities.GoalEntity
import com.budgetbuddy.databinding.DialogAddSavingsBinding
import com.budgetbuddy.databinding.DialogEditGoalBinding
import com.budgetbuddy.databinding.FragmentGoalDetailBinding
import com.budgetbuddy.util.CurrencyFormatter
import com.budgetbuddy.util.DateUtils
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.Calendar

@AndroidEntryPoint
class GoalDetailFragment : Fragment() {

    private var _binding: FragmentGoalDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: GoalDetailViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentGoalDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val goalId = arguments?.getLong("goalId", -1L) ?: -1L
        if (goalId <= 0) { findNavController().navigateUp(); return }

        viewModel.load(goalId)

        binding.btnBack.setOnClickListener { findNavController().navigateUp() }

        binding.btnEdit.setOnClickListener {
            viewModel.goal.value?.let { showEditDialog(it) }
        }

        binding.btnAddSavings.setOnClickListener {
            viewModel.goal.value?.let { showAddSavingsDialog(it) }
        }

        binding.btnDelete.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(getString(R.string.confirm_delete))
                .setMessage(getString(R.string.delete_goal_message))
                .setPositiveButton(getString(R.string.delete)) { _, _ -> viewModel.delete() }
                .setNegativeButton(getString(R.string.cancel), null)
                .show()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.goal.collect { goal -> goal?.let { render(it) } }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.finished.collect { if (it) findNavController().navigateUp() }
            }
        }
    }

    private fun render(goal: GoalEntity) {
        val pct = goal.progressPercent
        binding.tvGoalName.text = goal.name
        binding.progressGoal.progress = pct.coerceIn(0, 100)
        binding.tvPercent.text = "$pct%"
        binding.tvSaved.text = CurrencyFormatter.format(requireContext(), goal.savedAmount)
        binding.tvTarget.text = CurrencyFormatter.format(requireContext(), goal.targetAmount)
        binding.tvTargetDate.text = goal.targetDate?.let { DateUtils.formatDate(it) } ?: getString(R.string.no_date)
        binding.tvCreatedAt.text = DateUtils.formatDate(goal.createdAt)

        val (statusText, statusColor) = if (goal.isCompleted)
            getString(R.string.goal_completed) to requireContext().getColor(R.color.green_ok)
        else
            getString(R.string.goal_in_progress) to requireContext().getColor(R.color.teal_accent)
        binding.tvStatus.text = statusText
        binding.tvStatus.setTextColor(statusColor)

        binding.btnAddSavings.isEnabled = !goal.isCompleted
    }

    private fun showEditDialog(goal: GoalEntity) {
        val dialog = BottomSheetDialog(requireContext())
        val db = DialogEditGoalBinding.inflate(layoutInflater)
        dialog.setContentView(db.root)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        db.root.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            ?.setBackgroundResource(android.R.color.transparent)

        db.etName.setText(goal.name)
        db.etAmount.setText(goal.targetAmount.toBigDecimal().stripTrailingZeros().toPlainString())

        var selectedDate: Long? = goal.targetDate
        db.etDate.setText(selectedDate?.let { DateUtils.formatDate(it) } ?: "")
        db.etDate.setOnClickListener { showDatePicker(selectedDate) { d -> selectedDate = d; db.etDate.setText(DateUtils.formatDate(d)) } }
        db.tilDate.setEndIconOnClickListener { showDatePicker(selectedDate) { d -> selectedDate = d; db.etDate.setText(DateUtils.formatDate(d)) } }

        db.btnSave.setOnClickListener {
            val name = db.etName.text.toString().trim()
            val amount = db.etAmount.text.toString().toDoubleOrNull() ?: 0.0
            if (name.isNotEmpty() && amount > 0) {
                viewModel.update(name, amount, selectedDate)
                dialog.dismiss()
            }
        }
        db.btnCancel.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun showAddSavingsDialog(goal: GoalEntity) {
        val dialog = BottomSheetDialog(requireContext())
        val db = DialogAddSavingsBinding.inflate(layoutInflater)
        dialog.setContentView(db.root)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        db.root.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            ?.setBackgroundResource(android.R.color.transparent)

        db.tvGoalName.text = goal.name
        db.btnSave.setOnClickListener {
            val amount = db.etAmount.text.toString().toDoubleOrNull() ?: 0.0
            if (amount > 0) { viewModel.addSavings(amount); dialog.dismiss() }
        }
        db.btnCancel.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun showDatePicker(current: Long?, onPick: (Long) -> Unit) {
        val cal = Calendar.getInstance().apply { if (current != null) timeInMillis = current }
        DatePickerDialog(requireContext(), { _, y, m, d ->
            cal.set(y, m, d); onPick(cal.timeInMillis)
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
