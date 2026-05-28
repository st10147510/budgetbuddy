package com.budgetbuddy.ui.goals

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
import com.budgetbuddy.data.local.entities.GoalEntity
import com.budgetbuddy.databinding.DialogAddGoalBinding
import com.budgetbuddy.databinding.DialogAddSavingsBinding
import com.budgetbuddy.databinding.FragmentGoalsBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class GoalsFragment : Fragment() {

    private var _binding: FragmentGoalsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: GoalsViewModel by viewModels()

    @Inject lateinit var session: SessionManager

    private val adapter = GoalAdapter(
        onItemClick = { goal ->
            findNavController().navigate(
                R.id.goalDetailFragment,
                android.os.Bundle().apply { putLong("goalId", goal.id) }
            )
        },
        onAddSavings = { goal -> showAddSavingsDialog(goal) },
        onDelete = { goal -> viewModel.deleteGoal(goal) }
    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentGoalsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.rvGoals.layoutManager = LinearLayoutManager(requireContext())
        binding.rvGoals.adapter = adapter

        val userId = session.userId ?: return
        viewModel.loadGoals(userId)

        binding.fabAddGoal.setOnClickListener { showAddGoalDialog(userId) }
        binding.btnDebt.setOnClickListener { findNavController().navigate(R.id.debtFragment) }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.goals.collect { goals ->
                    adapter.submitList(goals)
                    binding.tvEmptyState.visibility = if (goals.isEmpty()) View.VISIBLE else View.GONE
                    binding.rvGoals.visibility = if (goals.isEmpty()) View.GONE else View.VISIBLE
                }
            }
        }
    }

    private fun showAddGoalDialog(userId: String) {
        val dialog = BottomSheetDialog(requireContext())
        val dialogBinding = DialogAddGoalBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBinding.root)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        val sheet = dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
        sheet?.setBackgroundResource(android.R.color.transparent)

        dialogBinding.btnSave.setOnClickListener {
            val name = dialogBinding.etName.text.toString().trim()
            val amount = dialogBinding.etAmount.text.toString().toDoubleOrNull() ?: 0.0
            if (name.isNotEmpty() && amount > 0) {
                viewModel.saveGoal(userId, name, amount)
                dialog.dismiss()
            }
        }
        dialogBinding.btnCancel.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun showAddSavingsDialog(goal: GoalEntity) {
        val dialog = BottomSheetDialog(requireContext())
        val dialogBinding = DialogAddSavingsBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBinding.root)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        val sheet = dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
        sheet?.setBackgroundResource(android.R.color.transparent)

        dialogBinding.tvGoalName.text = goal.name

        dialogBinding.btnSave.setOnClickListener {
            val amount = dialogBinding.etAmount.text.toString().toDoubleOrNull() ?: 0.0
            if (amount > 0) {
                viewModel.updateSaved(goal, amount)
                dialog.dismiss()
            }
        }
        dialogBinding.btnCancel.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
