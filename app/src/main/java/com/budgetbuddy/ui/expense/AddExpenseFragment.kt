package com.budgetbuddy.ui.expense

import android.app.DatePickerDialog
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.budgetbuddy.R
import com.budgetbuddy.data.local.SessionManager
import com.budgetbuddy.data.local.entities.TransactionType
import com.budgetbuddy.databinding.FragmentAddExpenseBinding
import com.budgetbuddy.util.DateUtils
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.io.File
import java.util.Calendar
import javax.inject.Inject

@AndroidEntryPoint
class AddExpenseFragment : Fragment() {

    private var _binding: FragmentAddExpenseBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ExpenseViewModel by viewModels()

    @Inject lateinit var session: SessionManager

    private var selectedDate = System.currentTimeMillis()
    private var receiptUri: Uri? = null
    private var isIncome = false

    private val takePicture = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) { binding.ivReceiptPreview.setImageURI(receiptUri); binding.ivReceiptPreview.visibility = View.VISIBLE }
    }

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { receiptUri = it; binding.ivReceiptPreview.setImageURI(it); binding.ivReceiptPreview.visibility = View.VISIBLE }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAddExpenseBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnBack.setOnClickListener { findNavController().navigateUp() }
        updateDateDisplay()
        binding.etDate.setOnClickListener { showDatePicker() }
        binding.tilDate.setEndIconOnClickListener { showDatePicker() }

        binding.tabTransactionType.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) { isIncome = tab.position == 1 }
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

        binding.btnAttachReceipt.setOnClickListener { showReceiptOptions() }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.categories.collect { categories ->
                    val names = categories.map { "${it.icon} ${it.name}" }
                    binding.actvCategory.setAdapter(
                        ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, names)
                    )
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is ExpenseUiState.Saved, is ExpenseUiState.Deleted -> {
                            findNavController().navigateUp()
                        }
                        is ExpenseUiState.Error -> {
                            Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                            viewModel.resetState()
                        }
                        else -> Unit
                    }
                }
            }
        }

        binding.btnSave.setOnClickListener { handleSave() }
    }

    private fun handleSave() {
        val amount = binding.etAmount.text.toString().toDoubleOrNull()
        val categoryText = binding.actvCategory.text.toString()
        val notes = binding.etNotes.text.toString().takeIf { it.isNotBlank() }
        val userId = session.userId ?: return

        if (amount == null || amount <= 0) { binding.tilAmount.error = getString(R.string.error_invalid_amount); return }
        if (categoryText.isBlank()) { binding.tilCategory.error = getString(R.string.error_select_category); return }

        val categories = viewModel.categories.value
        val categoryName = categoryText.substringAfter(" ").trim()
        val category = categories.find { it.name == categoryName } ?: categories.firstOrNull() ?: return

        viewModel.saveExpense(
            userId = userId,
            amount = amount,
            categoryId = category.id,
            date = selectedDate,
            notes = notes,
            receiptPath = receiptUri?.toString(),
            type = if (isIncome) TransactionType.INCOME else TransactionType.EXPENSE
        )
    }

    private fun showDatePicker() {
        val cal = Calendar.getInstance().apply { timeInMillis = selectedDate }
        DatePickerDialog(requireContext(), { _, year, month, day ->
            cal.set(year, month, day); selectedDate = cal.timeInMillis; updateDateDisplay()
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun updateDateDisplay() { binding.etDate.setText(DateUtils.formatDate(selectedDate)) }

    private fun showReceiptOptions() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.attach_receipt))
            .setItems(arrayOf(getString(R.string.take_photo), getString(R.string.choose_gallery))) { _, which ->
                if (which == 0) launchCamera() else pickImage.launch("image/*")
            }.show()
    }

    private fun launchCamera() {
        val imgFile = File(requireContext().filesDir, "receipts/receipt_${System.currentTimeMillis()}.jpg")
            .also { it.parentFile?.mkdirs() }
        receiptUri = FileProvider.getUriForFile(requireContext(), "${requireContext().packageName}.fileprovider", imgFile)
        takePicture.launch(receiptUri)
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
