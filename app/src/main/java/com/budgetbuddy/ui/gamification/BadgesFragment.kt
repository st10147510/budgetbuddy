package com.budgetbuddy.ui.gamification

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
import com.budgetbuddy.data.local.entities.BadgeType
import com.budgetbuddy.databinding.FragmentBadgesBinding
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class BadgesFragment : Fragment() {

    private var _binding: FragmentBadgesBinding? = null
    private val binding get() = _binding!!

    private val viewModel: BadgesViewModel by viewModels()

    @Inject lateinit var auth: FirebaseAuth

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentBadgesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnBack.setOnClickListener { findNavController().navigateUp() }
        binding.rvBadges.layoutManager = LinearLayoutManager(requireContext())

        val userId = auth.currentUser?.uid ?: return
        viewModel.loadBadges(userId)

        val fmt = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.badges.collect { badges ->
                    val items = badges.map { badge ->
                        BadgeItem(
                            icon = badgeIcon(badge.badgeType),
                            name = badgeName(badge.badgeType),
                            earnedOn = "Earned ${fmt.format(Date(badge.earnedAt))}"
                        )
                    }
                    binding.rvBadges.adapter = BadgeAdapter(items)
                }
            }
        }
    }

    private fun badgeIcon(type: BadgeType) = when (type) {
        BadgeType.FIRST_STEP -> "🎯"
        BadgeType.DAILY_TRACKER -> "🔥"
        BadgeType.PERFECT_MONTH -> "📅"
        BadgeType.BUDGET_MASTER -> "💰"
        BadgeType.DEBT_SLAYER -> "⚔️"
        BadgeType.GOAL_GETTER -> "🏆"
        BadgeType.THRIFTY_CHAMP -> "💎"
    }

    private fun badgeName(type: BadgeType) = when (type) {
        BadgeType.FIRST_STEP -> "First Step"
        BadgeType.DAILY_TRACKER -> "Daily Tracker"
        BadgeType.PERFECT_MONTH -> "Perfect Month"
        BadgeType.BUDGET_MASTER -> "Budget Master"
        BadgeType.DEBT_SLAYER -> "Debt Slayer"
        BadgeType.GOAL_GETTER -> "Goal Getter"
        BadgeType.THRIFTY_CHAMP -> "Thrifty Champ"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
