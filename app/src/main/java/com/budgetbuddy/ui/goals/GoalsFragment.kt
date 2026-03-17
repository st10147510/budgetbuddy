package com.budgetbuddy.ui.goals

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
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
import com.budgetbuddy.databinding.FragmentGoalsBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
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
        val nameInput = TextInputEditText(requireContext()).apply { hint = "Goal name" }
        val amountInput = TextInputEditText(requireContext()).apply {
            hint = getString(R.string.target_amount)
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
        }
        val container = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL; setPadding(48, 16, 48, 8)
            addView(TextInputLayout(requireContext()).apply { addView(nameInput) })
            addView(TextInputLayout(requireContext()).apply { prefixText = "R "; addView(amountInput) })
        }
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.add_goal)).setView(container)
            .setPositiveButton(R.string.save) { _, _ ->
                val name = nameInput.text.toString().trim()
                val amount = amountInput.text.toString().toDoubleOrNull() ?: 0.0
                if (name.isNotEmpty() && amount > 0) viewModel.saveGoal(userId, name, amount)
            }
            .setNegativeButton(R.string.cancel, null).show()
    }

    private fun showAddSavingsDialog(goal: GoalEntity) {
        val input = TextInputEditText(requireContext()).apply {
            hint = getString(R.string.saved_amount)
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
        }
        val container = TextInputLayout(requireContext()).apply { prefixText = "R "; addView(input); setPadding(48, 16, 48, 8) }
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Add savings to: ${goal.name}").setView(container)
            .setPositiveButton(R.string.save) { _, _ ->
                val amount = input.text.toString().toDoubleOrNull() ?: 0.0
                if (amount > 0) viewModel.updateSaved(goal, amount)
            }
            .setNegativeButton(R.string.cancel, null).show()
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
