package com.budgetbuddy.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.budgetbuddy.R
import com.budgetbuddy.databinding.FragmentPolicyAcceptanceBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PolicyAcceptanceFragment : Fragment() {

    private var _binding: FragmentPolicyAcceptanceBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PolicyAcceptanceViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPolicyAcceptanceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnAccept.setOnClickListener {
            if (!binding.cbTerms.isChecked || !binding.cbPrivacy.isChecked) {
                binding.tvError.text = "Please read and accept both policies to continue."
                binding.tvError.isVisible = true
                return@setOnClickListener
            }
            binding.tvError.isVisible = false
            viewModel.acceptPolicies()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is PolicyUiState.Loading -> {
                            binding.progressBar.isVisible = true
                            binding.contentGroup.isVisible = false
                        }
                        is PolicyUiState.AlreadyAccepted -> {
                            navigateToHome()
                        }
                        is PolicyUiState.NeedsAcceptance -> {
                            binding.progressBar.isVisible = false
                            binding.contentGroup.isVisible = true
                            binding.tvTermsVersion.text = "v${state.versions.termsVersion}"
                            binding.tvPrivacyVersion.text = "v${state.versions.privacyVersion}"
                        }
                        is PolicyUiState.Accepted -> {
                            navigateToHome()
                        }
                        is PolicyUiState.Error -> {
                            binding.progressBar.isVisible = false
                            binding.contentGroup.isVisible = true
                            binding.tvError.text = state.message
                            binding.tvError.isVisible = true
                        }
                    }
                }
            }
        }
    }

    private fun navigateToHome() {
        findNavController().navigate(R.id.action_policyAcceptance_to_home)
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
