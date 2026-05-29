package com.budgetbuddy.util

import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import java.io.File

object SecurityUtils {

    private const val TAG = "SecurityUtils"

    /**
     * Heuristic root detection. Returns true if the device appears to be rooted.
     * This is a best-effort check — it cannot be made foolproof on Android.
     * Use as a risk signal, not a hard block.
     */
    fun isDeviceRooted(): Boolean {
        return checkBuildTags() || checkSuBinaries() || checkRootPaths()
    }

    private fun checkBuildTags(): Boolean {
        val tags = Build.TAGS
        val rooted = tags != null && tags.contains("test-keys")
        if (rooted) Log.w(TAG, "Root detected: test-keys build")
        return rooted
    }

    private fun checkSuBinaries(): Boolean {
        val paths = listOf(
            "/sbin/su", "/system/bin/su", "/system/xbin/su",
            "/data/local/xbin/su", "/data/local/bin/su", "/data/local/su",
            "/system/sd/xbin/su", "/system/bin/failsafe/su", "/dev/com.koushikdutta.superuser.daemon/"
        )
        return paths.any { path ->
            File(path).exists().also { if (it) Log.w(TAG, "Root detected: $path") }
        }
    }

    private fun checkRootPaths(): Boolean {
        val paths = listOf(
            "/system/app/Superuser.apk",
            "/system/app/SuperSU.apk",
            "/data/data/com.noshufou.android.su",
            "/system/xbin/daemonsu",
        )
        return paths.any { path ->
            File(path).exists().also { if (it) Log.w(TAG, "Root detected: $path") }
        }
    }

    /**
     * Returns true if this is a debug build based on the signing certificate.
     * Use to apply stricter controls in production.
     */
    fun isDebugBuild(): Boolean = com.budgetbuddy.BuildConfig.DEBUG
}
