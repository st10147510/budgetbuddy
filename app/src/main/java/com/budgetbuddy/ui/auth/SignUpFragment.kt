package com.budgetbuddy.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.budgetbuddy.R
import com.budgetbuddy.databinding.FragmentSignUpBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SignUpFragment : Fragment() {

    private var _binding: FragmentSignUpBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSignUpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnSignUp.setOnClickListener {
            viewModel.signUp(
                email = binding.etEmail.text.toString().trim(),
                password = binding.etPassword.text.toString(),
                displayName = binding.etFullName.text.toString().trim()
            )
        }
        binding.tvSignIn.setOnClickListener { findNavController().navigate(R.id.action_signUp_to_signIn) }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    binding.progressBar.isVisible = state is AuthUiState.Loading
                    binding.btnSignUp.isEnabled = state !is AuthUiState.Loading
                    when (state) {
                        is AuthUiState.Success -> findNavController().navigate(R.id.action_signUp_to_home)
                        is AuthUiState.Error -> { Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show(); viewModel.resetState() }
                        else -> Unit
                    }
                }
            }
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
