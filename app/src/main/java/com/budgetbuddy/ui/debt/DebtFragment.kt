package com.budgetbuddy.ui.debt

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
import com.budgetbuddy.data.local.entities.PayoffStrategy
import com.budgetbuddy.databinding.DialogAddDebtBinding
import com.budgetbuddy.databinding.FragmentDebtBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class DebtFragment : Fragment() {

    private var _binding: FragmentDebtBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DebtViewModel by viewModels()

    @Inject lateinit var session: SessionManager

    private val adapter = DebtAdapter { viewModel.deleteDebt(it) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentDebtBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.rvDebts.layoutManager = LinearLayoutManager(requireContext())
        binding.rvDebts.adapter = adapter

        val userId = session.userId ?: return
        viewModel.loadDebts(userId)

        binding.btnBack.setOnClickListener { findNavController().navigateUp() }
        binding.fabAddDebt.setOnClickListener { showAddDebtDialog(userId) }
        binding.rgStrategy.setOnCheckedChangeListener { _, id ->
            viewModel.setStrategy(if (id == R.id.rbSnowball) PayoffStrategy.SNOWBALL else PayoffStrategy.AVALANCHE)
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
        val dialog = BottomSheetDialog(requireContext())
        val dialogBinding = DialogAddDebtBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBinding.root)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        val sheet = dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
        sheet?.setBackgroundResource(android.R.color.transparent)

        dialogBinding.btnSave.setOnClickListener {
            val name = dialogBinding.etName.text.toString().trim()
            val balance = dialogBinding.etBalance.text.toString().toDoubleOrNull() ?: 0.0
            val rate = dialogBinding.etRate.text.toString().toDoubleOrNull() ?: 0.0
            val min = dialogBinding.etMinPayment.text.toString().toDoubleOrNull() ?: 0.0
            if (name.isNotEmpty() && balance > 0) {
                viewModel.addDebt(userId, name, balance, rate, min)
                dialog.dismiss()
            }
        }
        dialogBinding.btnCancel.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
