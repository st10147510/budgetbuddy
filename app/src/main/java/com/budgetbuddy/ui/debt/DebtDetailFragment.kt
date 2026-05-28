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
import com.budgetbuddy.R
import com.budgetbuddy.data.local.entities.DebtEntity
import com.budgetbuddy.databinding.DialogEditDebtBinding
import com.budgetbuddy.databinding.DialogMakePaymentBinding
import com.budgetbuddy.databinding.FragmentDebtDetailBinding
import com.budgetbuddy.util.DateUtils
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class DebtDetailFragment : Fragment() {

    private var _binding: FragmentDebtDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DebtDetailViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentDebtDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val debtId = arguments?.getLong("debtId", -1L) ?: -1L
        if (debtId <= 0) { findNavController().navigateUp(); return }

        viewModel.load(debtId)

        binding.btnBack.setOnClickListener { findNavController().navigateUp() }

        binding.btnEdit.setOnClickListener {
            viewModel.debt.value?.let { showEditDialog(it) }
        }

        binding.btnMakePayment.setOnClickListener {
            viewModel.debt.value?.let { showPaymentDialog(it) }
        }

        binding.btnDelete.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(getString(R.string.confirm_delete))
                .setMessage(getString(R.string.delete_debt_message))
                .setPositiveButton(getString(R.string.delete)) { _, _ -> viewModel.delete() }
                .setNegativeButton(getString(R.string.cancel), null)
                .show()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.debt.collect { debt -> debt?.let { render(it) } }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.finished.collect { if (it) findNavController().navigateUp() }
            }
        }
    }

    private fun render(debt: DebtEntity) {
        val payoffPct = if (debt.originalBalance > 0)
            ((debt.originalBalance - debt.balance) / debt.originalBalance * 100).toInt().coerceIn(0, 100) else 0

        binding.tvDebtName.text = debt.name
        binding.tvBalance.text = "R %.2f".format(debt.balance)
        binding.progressDebt.progress = payoffPct
        binding.tvPayoffPercent.text = "$payoffPct% paid off"
        binding.tvOriginalBalance.text = "R %.2f".format(debt.originalBalance)
        binding.tvInterestRate.text = "%.2f%% per year".format(debt.interestRate)
        binding.tvMinPayment.text = "R %.2f / month".format(debt.minimumPayment)
        binding.tvCreatedAt.text = DateUtils.formatDate(debt.createdAt)

        val (statusText, statusColor) = if (debt.isPaidOff)
            getString(R.string.debt_paid_off) to requireContext().getColor(R.color.green_ok)
        else
            getString(R.string.debt_active) to requireContext().getColor(R.color.coral_accent)
        binding.tvStatus.text = statusText
        binding.tvStatus.setTextColor(statusColor)

        binding.btnMakePayment.isEnabled = !debt.isPaidOff
    }

    private fun showEditDialog(debt: DebtEntity) {
        val dialog = BottomSheetDialog(requireContext())
        val db = DialogEditDebtBinding.inflate(layoutInflater)
        dialog.setContentView(db.root)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        db.root.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            ?.setBackgroundResource(android.R.color.transparent)

        db.etName.setText(debt.name)
        db.etRate.setText(debt.interestRate.toBigDecimal().stripTrailingZeros().toPlainString())
        db.etMinPayment.setText(debt.minimumPayment.toBigDecimal().stripTrailingZeros().toPlainString())

        db.btnSave.setOnClickListener {
            val name = db.etName.text.toString().trim()
            val rate = db.etRate.text.toString().toDoubleOrNull() ?: debt.interestRate
            val min = db.etMinPayment.text.toString().toDoubleOrNull() ?: debt.minimumPayment
            if (name.isNotEmpty()) {
                viewModel.update(name, rate, min)
                dialog.dismiss()
            }
        }
        db.btnCancel.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun showPaymentDialog(debt: DebtEntity) {
        val dialog = BottomSheetDialog(requireContext())
        val db = DialogMakePaymentBinding.inflate(layoutInflater)
        dialog.setContentView(db.root)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        db.root.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            ?.setBackgroundResource(android.R.color.transparent)

        db.tvPaymentTitle.text = "Pay — ${debt.name}"
        db.tvDebtBalance.text = "Outstanding: R %.2f".format(debt.balance)

        db.btnPay.setOnClickListener {
            val amount = db.etAmount.text.toString().toDoubleOrNull()
            if (amount != null && amount > 0) {
                viewModel.makePayment(amount)
                dialog.dismiss()
            } else {
                db.tilAmount.error = "Enter a valid amount"
            }
        }
        db.btnCancel.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
