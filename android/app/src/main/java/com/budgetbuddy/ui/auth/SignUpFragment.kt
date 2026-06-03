package com.budgetbuddy.ui.auth

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
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
        Log.d("SignUpFragment", "onViewCreated")

        binding.btnBack.setOnClickListener { findNavController().navigateUp() }

        // Real-time password strength feedback on every keystroke
        binding.etPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
            override fun afterTextChanged(s: Editable?) {
                val password = s?.toString() ?: ""
                viewModel.evaluatePasswordStrength(password)
                // Clear any previous password error as user types
                binding.tilPassword.error = null
            }
        })

        // Clear confirm-password error as user types
        binding.etConfirmPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
            override fun afterTextChanged(s: Editable?) { binding.tilConfirmPassword.error = null }
        })

        binding.btnSignUp.setOnClickListener {
            Log.d("SignUpFragment", "Sign up button clicked")
            // Clear previous inline errors before re-validating
            binding.tilPassword.error = null
            binding.tilConfirmPassword.error = null
            viewModel.signUp(
                email = binding.etEmail.text.toString().trim(),
                password = binding.etPassword.text.toString(),
                confirmPassword = binding.etConfirmPassword.text.toString(),
                displayName = binding.etDisplayName.text.toString().trim()
            )
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Observe auth state
                launch {
                    viewModel.uiState.collect { state ->
                        binding.progressBar.isVisible = state is AuthUiState.Loading
                        binding.btnSignUp.isEnabled = state !is AuthUiState.Loading
                        when (state) {
                            is AuthUiState.Success -> {
                                Log.i("SignUpFragment", "Registration successful, navigating to home")
                                findNavController().navigate(R.id.action_signUp_to_home)
                            }
                            is AuthUiState.Error -> {
                                Log.w("SignUpFragment", "Registration error: ${state.message}")
                                // Show inline error on the relevant field where possible
                                when {
                                    state.message.contains("password", ignoreCase = true) &&
                                    state.message.contains("match", ignoreCase = true) ->
                                        binding.tilConfirmPassword.error = state.message
                                    state.message.contains("password", ignoreCase = true) ->
                                        binding.tilPassword.error = state.message
                                    else ->
                                        Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                                }
                                viewModel.resetState()
                            }
                            else -> Unit
                        }
                    }
                }

                // Observe real-time strength score and update bar + label
                launch {
                    viewModel.passwordStrength.collect { score ->
                        binding.layoutPasswordStrength.isVisible = score > 0
                        binding.progressPasswordStrength.progress = score
                        val (label, colorRes) = when (score) {
                            1    -> "Weak"   to R.color.red_danger
                            2    -> "Medium" to R.color.amber_warning
                            else -> "Strong" to R.color.green_ok
                        }
                        binding.tvPasswordStrength.text = label
                        val color = ContextCompat.getColor(requireContext(), colorRes)
                        binding.tvPasswordStrength.setTextColor(color)
                        binding.progressPasswordStrength.progressTintList =
                            android.content.res.ColorStateList.valueOf(color)
                        Log.d("SignUpFragment", "Password strength updated: $label ($score/3)")
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
