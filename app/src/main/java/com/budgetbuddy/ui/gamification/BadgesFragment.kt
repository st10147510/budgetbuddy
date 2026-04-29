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
import com.budgetbuddy.data.local.SessionManager
import com.budgetbuddy.data.local.entities.BadgeType
import com.budgetbuddy.databinding.FragmentBadgesBinding
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

    @Inject lateinit var session: SessionManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentBadgesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnBack.setOnClickListener { findNavController().navigateUp() }
        binding.rvBadges.layoutManager = LinearLayoutManager(requireContext())

        val userId = session.userId ?: return
        viewModel.loadBadges(userId)

        val fmt = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.badges.collect { earnedBadges ->
                    val earnedMap = earnedBadges.associateBy { it.badgeType }
                    // Always show all badge types — earned ones show the date, locked ones show "Locked"
                    val items = BadgeType.values().map { type ->
                        val earned = earnedMap[type]
                        BadgeItem(
                            icon = badgeIcon(type),
                            name = badgeName(type),
                            description = badgeDescription(type),
                            earnedOn = earned?.let { "Earned ${fmt.format(Date(it.earnedAt))}" }
                        )
                    }
                    // Show earned badges first, then locked ones
                    binding.rvBadges.adapter = BadgeAdapter(
                        items.sortedByDescending { it.isEarned }
                    )
                }
            }
        }
    }

    private fun badgeIcon(type: BadgeType) = when (type) {
        BadgeType.FIRST_STEP    -> "🎯"
        BadgeType.DAILY_TRACKER -> "🔥"
        BadgeType.PERFECT_MONTH -> "📅"
        BadgeType.BUDGET_MASTER -> "💰"
        BadgeType.DEBT_SLAYER   -> "⚔️"
        BadgeType.GOAL_GETTER   -> "🏆"
        BadgeType.THRIFTY_CHAMP -> "💎"
    }

    private fun badgeName(type: BadgeType) = when (type) {
        BadgeType.FIRST_STEP    -> "First Step"
        BadgeType.DAILY_TRACKER -> "Daily Tracker"
        BadgeType.PERFECT_MONTH -> "Perfect Month"
        BadgeType.BUDGET_MASTER -> "Budget Master"
        BadgeType.DEBT_SLAYER   -> "Debt Slayer"
        BadgeType.GOAL_GETTER   -> "Goal Getter"
        BadgeType.THRIFTY_CHAMP -> "Thrifty Champ"
    }

    private fun badgeDescription(type: BadgeType) = when (type) {
        BadgeType.FIRST_STEP    -> "Log your very first transaction"
        BadgeType.DAILY_TRACKER -> "Log a transaction every day for 7 days in a row"
        BadgeType.PERFECT_MONTH -> "Earn more than you spend in a calendar month"
        BadgeType.BUDGET_MASTER -> "Keep every budget category under its limit this month"
        BadgeType.DEBT_SLAYER   -> "Fully pay off at least one debt"
        BadgeType.GOAL_GETTER   -> "Complete at least one savings goal"
        BadgeType.THRIFTY_CHAMP -> "Save at least 33% of your income in a month"
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
