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
import com.budgetbuddy.data.local.entities.DebtEntity
import com.budgetbuddy.databinding.DialogAddDebtBinding
import com.budgetbuddy.databinding.DialogMakePaymentBinding
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

    private lateinit var userId: String
    private val adapter = DebtAdapter(
        onItemClick = { debt ->
            findNavController().navigate(
                R.id.debtDetailFragment,
                android.os.Bundle().apply { putLong("debtId", debt.id) }
            )
        },
        onDelete = { viewModel.deleteDebt(it) },
        onPayment = { showPaymentDialog(it) }
    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentDebtBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.rvDebts.layoutManager = LinearLayoutManager(requireContext())
        binding.rvDebts.adapter = adapter

        userId = session.userId ?: return
        viewModel.loadDebts(userId)

        binding.btnBack.setOnClickListener { findNavController().navigateUp() }
        binding.fabAddDebt.setOnClickListener { showAddDebtDialog(userId) }
        binding.btnViewPlan.setOnClickListener {
            findNavController().navigate(R.id.action_debt_to_paymentPlan)
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

    private fun showPaymentDialog(debt: DebtEntity) {
        val dialog = BottomSheetDialog(requireContext())
        val dialogBinding = DialogMakePaymentBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBinding.root)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        val sheet = dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
        sheet?.setBackgroundResource(android.R.color.transparent)

        dialogBinding.tvPaymentTitle.text = "Pay — ${debt.name}"
        dialogBinding.tvDebtBalance.text = "Outstanding: R %.2f".format(debt.balance)

        dialogBinding.btnPay.setOnClickListener {
            val amount = dialogBinding.etAmount.text.toString().toDoubleOrNull()
            if (amount != null && amount > 0) {
                viewModel.makePayment(debt, amount)
                dialog.dismiss()
            } else {
                dialogBinding.tilAmount.error = "Enter a valid amount"
            }
        }
        dialogBinding.btnCancel.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
