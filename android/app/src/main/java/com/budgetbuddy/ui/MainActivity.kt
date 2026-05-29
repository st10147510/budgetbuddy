package com.budgetbuddy.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.budgetbuddy.R
import com.budgetbuddy.data.local.SessionManager
import com.budgetbuddy.data.repository.AuthRepository
import com.budgetbuddy.databinding.ActivityMainBinding
import com.budgetbuddy.util.LocaleHelper
import com.budgetbuddy.util.SecurityUtils
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    @Inject lateinit var sessionManager: SessionManager
    @Inject lateinit var authRepository: AuthRepository

    private var sessionWarningDialog: AlertDialog? = null
    private var sessionCountdown: CountDownTimer? = null

    private val requestNotificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            Log.d("MainActivity", "POST_NOTIFICATIONS granted: $granted")
        }

    private val authFragments = setOf(
        R.id.welcomeFragment,
        R.id.signInFragment,
        R.id.signUpFragment,
        R.id.forgotPasswordFragment,
        R.id.policyAcceptanceFragment,
        R.id.addExpenseFragment,
        R.id.badgesFragment,
        R.id.categoriesFragment,
        R.id.debtFragment,
        R.id.transactionListFragment,
        R.id.paymentPlanFragment,
        R.id.transactionDetailFragment,
        R.id.goalDetailFragment,
        R.id.debtDetailFragment,
        R.id.budgetDetailFragment,
        R.id.uploadStatementFragment,
    )

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.applyLocale(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Prevent screenshots and screen recording of financial data
        window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Warn (non-blocking) if device appears rooted
        if (!SecurityUtils.isDebugBuild() && SecurityUtils.isDeviceRooted()) {
            Log.w("MainActivity", "Running on a potentially rooted device")
            MaterialAlertDialogBuilder(this)
                .setTitle("Security Warning")
                .setMessage("This device may be rooted or compromised. Your financial data may be at increased risk. We recommend using BudgetBuddy on a non-rooted device.")
                .setPositiveButton("I Understand") { d, _ -> d.dismiss() }
                .setNegativeButton("Exit") { _, _ -> finish() }
                .setCancelable(false)
                .show()
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.navHostFragment.updatePadding(top = insets.top)
            binding.bottomNav.updatePadding(bottom = insets.bottom)
            WindowInsetsCompat.CONSUMED
        }

        binding.bottomNav.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            binding.bottomNav.visibility =
                if (destination.id in authFragments) View.GONE else View.VISIBLE

            // Dismiss the warning if the user navigated to an auth screen another way
            if (destination.id in authFragments) cancelSessionWarning()
        }
    }

    override fun onResume() {
        super.onResume()
        checkSessionExpiry()
    }

    override fun onDestroy() {
        super.onDestroy()
        cancelSessionWarning()
    }

    // ── Session enforcement ───────────────────────────────────────────────────

    private fun checkSessionExpiry() {
        if (!sessionManager.isLoggedIn) return
        if (!sessionManager.isSessionExpired()) return

        val currentDest = navController.currentDestination?.id ?: return
        if (currentDest in authFragments) return

        // Don't stack multiple dialogs
        if (sessionWarningDialog?.isShowing == true) return

        showSessionWarningDialog()
    }

    private fun showSessionWarningDialog() {
        val dialog = MaterialAlertDialogBuilder(this)
            .setTitle("Session Expiring")
            .setMessage(countdownMessage(30))
            .setCancelable(false)
            .setPositiveButton("Stay Signed In") { _, _ ->
                cancelSessionWarning()
                sessionManager.extendSession()
                Log.i("MainActivity", "Session extended by user")
            }
            .setNegativeButton("Sign Out") { _, _ ->
                cancelSessionWarning()
                performSignOut()
            }
            .create()

        dialog.show()
        sessionWarningDialog = dialog

        sessionCountdown = object : CountDownTimer(COUNTDOWN_MS, 1_000) {
            override fun onTick(millisUntilFinished: Long) {
                val seconds = (millisUntilFinished / 1_000).toInt()
                if (dialog.isShowing) {
                    dialog.setMessage(countdownMessage(seconds))
                }
            }

            override fun onFinish() {
                Log.i("MainActivity", "Session countdown expired — signing out")
                if (dialog.isShowing) dialog.dismiss()
                sessionWarningDialog = null
                performSignOut()
            }
        }.start()
    }

    private fun cancelSessionWarning() {
        sessionCountdown?.cancel()
        sessionCountdown = null
        sessionWarningDialog?.dismiss()
        sessionWarningDialog = null
    }

    private fun performSignOut() {
        authRepository.signOut()
        navController.navigate(
            R.id.welcomeFragment,
            null,
            NavOptions.Builder()
                .setPopUpTo(R.id.nav_graph, true)
                .build()
        )
    }

    private fun countdownMessage(seconds: Int): String {
        val unit = if (seconds == 1) "second" else "seconds"
        return "Your session has expired.\n\nYou will be automatically signed out in $seconds $unit. Tap \"Stay Signed In\" to extend your session."
    }

    override fun onSupportNavigateUp(): Boolean =
        navController.navigateUp() || super.onSupportNavigateUp()

    companion object {
        private const val COUNTDOWN_MS = 30_000L
    }
}
