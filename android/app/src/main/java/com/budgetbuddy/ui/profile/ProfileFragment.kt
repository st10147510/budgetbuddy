package com.budgetbuddy.ui.profile

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.budgetbuddy.R
import com.budgetbuddy.databinding.FragmentProfileBinding
import com.budgetbuddy.ui.auth.AuthViewModel
import com.budgetbuddy.util.CurrencyFormatter
import com.budgetbuddy.util.LocaleHelper
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.io.File

@AndroidEntryPoint
class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val authViewModel: AuthViewModel by viewModels()
    private val profileViewModel: ProfileViewModel by viewModels()

    private var cameraUri: Uri? = null

    private val takePicture = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) cameraUri?.let { profileViewModel.uploadProfilePhoto(it) }
    }

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { profileViewModel.uploadProfilePhoto(it) }
    }

    private val requestCameraPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) openCamera() else Toast.makeText(requireContext(), getString(R.string.camera_permission_denied), Toast.LENGTH_SHORT).show()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val session = profileViewModel.sessionManager
        binding.tvDisplayName.text = session.displayName ?: "User"
        binding.tvEmail.text = session.email ?: ""
        loadAvatar(session.photoUrl)

        binding.ivAvatar.setOnClickListener { showPhotoOptions() }

        binding.tvCategories.setOnClickListener { findNavController().navigate(R.id.categoriesFragment) }
        binding.tvBadges.setOnClickListener { findNavController().navigate(R.id.badgesFragment) }
        binding.tvNotifications.setOnClickListener { /* future */ }
        binding.tvLanguage.setOnClickListener { showLanguagePicker() }

        binding.btnSyncCloud.setOnClickListener { profileViewModel.syncToCloud() }

        binding.btnSignOut.setOnClickListener {
            authViewModel.signOut()
            findNavController().navigate(R.id.welcomeFragment)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                profileViewModel.uiState.collect { state ->
                    when (state) {
                        is ProfileUiState.Loading -> binding.ivAvatar.alpha = 0.5f
                        is ProfileUiState.Syncing -> {
                            binding.btnSyncCloud.isEnabled = false
                            binding.btnSyncCloud.text = getString(R.string.syncing)
                        }
                        is ProfileUiState.SyncSuccess -> {
                            binding.btnSyncCloud.isEnabled = true
                            binding.btnSyncCloud.text = getString(R.string.sync_to_cloud)
                            Toast.makeText(requireContext(), getString(R.string.sync_success), Toast.LENGTH_SHORT).show()
                            profileViewModel.resetState()
                        }
                        is ProfileUiState.PhotoUpdated -> {
                            binding.ivAvatar.alpha = 1f
                            loadAvatar(state.url)
                            profileViewModel.resetState()
                        }
                        is ProfileUiState.Error -> {
                            binding.ivAvatar.alpha = 1f
                            binding.btnSyncCloud.isEnabled = true
                            binding.btnSyncCloud.text = getString(R.string.sync_to_cloud)
                            Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                            profileViewModel.resetState()
                        }
                        else -> {
                            binding.ivAvatar.alpha = 1f
                            binding.btnSyncCloud.isEnabled = true
                            binding.btnSyncCloud.text = getString(R.string.sync_to_cloud)
                        }
                    }
                }
            }
        }
    }

    private fun showPhotoOptions() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.change_photo))
            .setItems(arrayOf(getString(R.string.take_photo), getString(R.string.choose_gallery))) { _, which ->
                if (which == 0) launchCamera() else pickImage.launch("image/*")
            }.show()
    }

    private fun launchCamera() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            openCamera()
        } else {
            requestCameraPermission.launch(Manifest.permission.CAMERA)
        }
    }

    private fun openCamera() {
        val imgFile = File(requireContext().filesDir, "profile/profile_${System.currentTimeMillis()}.jpg")
            .also { it.parentFile?.mkdirs() }
        cameraUri = FileProvider.getUriForFile(requireContext(), "${requireContext().packageName}.fileprovider", imgFile)
        takePicture.launch(cameraUri)
    }

    private fun showLanguagePicker() {
        val languages = LocaleHelper.getSupportedLanguages()
        val currentTag = LocaleHelper.getSavedTag(requireContext())
        val currentIndex = languages.indexOfFirst { it.tag == currentTag }.coerceAtLeast(0)
        val names = languages.map { it.displayName }.toTypedArray()

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.select_language))
            .setSingleChoiceItems(names, currentIndex) { dialog, which ->
                val chosen = languages[which]
                LocaleHelper.setLanguage(requireContext(), chosen.tag)
                CurrencyFormatter.resetCache()
                dialog.dismiss()
                requireActivity().recreate()
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun loadAvatar(url: String?) {
        if (!url.isNullOrBlank()) {
            Glide.with(this)
                .load(url)
                .circleCrop()
                .placeholder(R.drawable.ic_profile)
                .into(binding.ivAvatar)
        } else {
            binding.ivAvatar.setImageResource(R.drawable.ic_profile)
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
