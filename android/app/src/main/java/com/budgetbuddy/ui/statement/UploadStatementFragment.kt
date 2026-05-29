package com.budgetbuddy.ui.statement

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.budgetbuddy.databinding.FragmentUploadStatementBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

@AndroidEntryPoint
class UploadStatementFragment : Fragment() {

    private var _binding: FragmentUploadStatementBinding? = null
    private val binding get() = _binding!!
    private val viewModel: UploadStatementViewModel by viewModels()

    private val pickPdf = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri -> handleSelectedFile(uri) }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentUploadStatementBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnBack.setOnClickListener { findNavController().navigateUp() }
        binding.btnPickFile.setOnClickListener { openFilePicker() }
        binding.btnUploadAnother.setOnClickListener { viewModel.reset() }
        binding.btnTryAgain.setOnClickListener { viewModel.reset() }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.uiState.collect { render(it) } }
            }
        }
    }

    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "application/pdf"
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        pickPdf.launch(Intent.createChooser(intent, "Select bank statement PDF"))
    }

    private fun handleSelectedFile(uri: Uri) {
        val fileName = getFileName(uri) ?: "statement.pdf"
        val tempFile = File(requireContext().cacheDir, fileName)

        requireContext().contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(tempFile).use { output -> input.copyTo(output) }
        }

        viewModel.upload(tempFile)
    }

    private fun getFileName(uri: Uri): String? {
        requireContext().contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (cursor.moveToFirst() && nameIndex >= 0) return cursor.getString(nameIndex)
        }
        return uri.lastPathSegment
    }

    private fun render(state: UploadUiState) {
        binding.idleGroup.isVisible    = state is UploadUiState.Idle
        binding.uploadingGroup.isVisible = state is UploadUiState.Uploading || state is UploadUiState.Processing || state is UploadUiState.Queued
        binding.doneGroup.isVisible    = state is UploadUiState.Done
        binding.errorGroup.isVisible   = state is UploadUiState.Failed

        when (state) {
            is UploadUiState.Uploading   -> binding.tvUploadStatus.text = "Uploading…"
            is UploadUiState.Queued      -> binding.tvUploadStatus.text = "Queued for processing…"
            is UploadUiState.Processing  -> binding.tvUploadStatus.text = "Processing transactions…"
            is UploadUiState.Done        -> {
                binding.tvDoneMessage.text =
                    "Successfully imported ${state.rowsImported} transaction(s) from ${state.filename}."
            }
            is UploadUiState.Failed      -> binding.tvErrorMessage.text = state.error
            else -> Unit
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
