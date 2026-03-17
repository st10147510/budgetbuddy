package com.budgetbuddy.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.budgetbuddy.R
import com.budgetbuddy.data.local.SessionManager
import com.budgetbuddy.databinding.FragmentProfileBinding
import com.budgetbuddy.ui.auth.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val authViewModel: AuthViewModel by viewModels()

    @Inject lateinit var session: SessionManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.tvDisplayName.text = session.displayName ?: "User"
        binding.tvEmail.text = session.email ?: ""

        binding.tvCategories.setOnClickListener { findNavController().navigate(R.id.categoriesFragment) }
        binding.tvBadges.setOnClickListener { findNavController().navigate(R.id.badgesFragment) }
        binding.tvNotifications.setOnClickListener { /* future */ }

        binding.btnSignOut.setOnClickListener {
            authViewModel.signOut()
            findNavController().navigate(R.id.welcomeFragment)
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
