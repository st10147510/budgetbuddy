package com.budgetbuddy.ui.debt

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
import com.budgetbuddy.data.local.entities.PayoffStrategy
import com.budgetbuddy.databinding.FragmentDebtBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class DebtFragment : Fragment() {

    private var _binding: FragmentDebtBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DebtViewModel by viewModels()

    @Inject lateinit var auth: FirebaseAuth

    private val adapter = DebtAdapter { debt -> viewModel.deleteDebt(debt) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentDebtBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rvDebts.layoutManager = LinearLayoutManager(requireContext())
        binding.rvDebts.adapter = adapter

        val userId = auth.currentUser?.uid ?: return
        viewModel.loadDebts(userId)

        binding.btnBack.setOnClickListener { findNavController().navigateUp() }
        binding.fabAddDebt.setOnClickListener { showAddDebtDialog(userId) }

        binding.rgStrategy.setOnCheckedChangeListener { _, checkedId ->
            viewModel.setStrategy(
                if (checkedId == R.id.rbSnowball) PayoffStrategy.SNOWBALL else PayoffStrategy.AVALANCHE
            )
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.debts.collect { debts ->
                    adapter.submitList(debts)
                    binding.tvEmptyState.visibility = if (debts.isEmpty()) View.VISIBLE else View.GONE
                    binding.rvDebts.visibility = if (debts.isEmpty()) View.GONE else View.VISIBLE
                }
            }
        }
    }

    private fun showAddDebtDialog(userId: String) {
        val nameInput = TextInputEditText(requireContext()).apply { hint = "Debt name (e.g. Credit Card)" }
        val balanceInput = TextInputEditText(requireContext()).apply {
            hint = "Balance"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
        }
        val rateInput = TextInputEditText(requireContext()).apply {
            hint = getString(R.string.interest_rate)
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
        }
        val minInput = TextInputEditText(requireContext()).apply {
            hint = getString(R.string.minimum_payment)
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
        }
        val container = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 16, 48, 8)
            addView(TextInputLayout(requireContext()).apply { addView(nameInput) })
            addView(TextInputLayout(requireContext()).apply { prefixText = "R "; addView(balanceInput) })
            addView(TextInputLayout(requireContext()).apply { suffixText = "%"; addView(rateInput) })
            addView(TextInputLayout(requireContext()).apply { prefixText = "R "; addView(minInput) })
        }
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.add_debt))
            .setView(container)
            .setPositiveButton(R.string.save) { _, _ ->
                val name = nameInput.text.toString().trim()
                val balance = balanceInput.text.toString().toDoubleOrNull() ?: 0.0
                val rate = rateInput.text.toString().toDoubleOrNull() ?: 0.0
                val min = minInput.text.toString().toDoubleOrNull() ?: 0.0
                if (name.isNotEmpty() && balance > 0) {
                    viewModel.addDebt(userId, name, balance, rate, min)
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
