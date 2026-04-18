package com.holywarrior.silence_of_salah_engine.permissions

import android.app.Activity
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings

object PermissionManager {

    // ─────────────────────────────────────────────
    // Foreground Service (no runtime permission)
    // ─────────────────────────────────────────────

    fun hasForegroundServicePermission(): Boolean {
        // Always true if declared in manifest
        return true
    }

    // ─────────────────────────────────────────────
    // Exact Alarm Permission (Android 12+)
    // ─────────────────────────────────────────────

    fun hasExactAlarmPermission(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return true

        val alarmManager =
            context.getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager

        return alarmManager.canScheduleExactAlarms()
    }

    fun requestExactAlarmPermission(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return

        val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
            data = Uri.parse("package:${context.packageName}")
        }

        context.startActivity(intent)
    }

    // ─────────────────────────────────────────────
    // Do Not Disturb (for silent mode control)
    // ─────────────────────────────────────────────

    fun hasDndAccess(context: Context): Boolean {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        return nm.isNotificationPolicyAccessGranted
    }

    fun requestDndAccess(context: Context) {
        val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
        context.startActivity(intent)
    }

    // ─────────────────────────────────────────────
    // Battery Optimization (IMPORTANT for background tasks)
    // ─────────────────────────────────────────────

    fun isIgnoringBatteryOptimizations(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return true

        val pm = context.getSystemService(Context.POWER_SERVICE) as android.os.PowerManager
        return pm.isIgnoringBatteryOptimizations(context.packageName)
    }

    fun requestIgnoreBatteryOptimizations(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return

        val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
            data = Uri.parse("package:${context.packageName}")
        }

        context.startActivity(intent)
    }

    // ─────────────────────────────────────────────
    // Notification Permission (Android 13+)
    // ─────────────────────────────────────────────

    fun hasNotificationPermission(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true

        return context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) ==
                android.content.pm.PackageManager.PERMISSION_GRANTED
    }

    fun requestNotificationPermission(activity: Activity, requestCode: Int) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return

        activity.requestPermissions(
            arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
            requestCode
        )
    }

    // ─────────────────────────────────────────────
    // Combined Check (VERY USEFUL)
    // ─────────────────────────────────────────────

    data class PermissionStatus(
        val exactAlarm: Boolean,
        val dnd: Boolean,
        val batteryOptimization: Boolean,
        val notifications: Boolean
    ) {
        fun allGranted(): Boolean {
            return exactAlarm && dnd && batteryOptimization && notifications
        }
    }

    fun checkAll(context: Context): PermissionStatus {
        return PermissionStatus(
            exactAlarm = hasExactAlarmPermission(context),
            dnd = hasDndAccess(context),
            batteryOptimization = isIgnoringBatteryOptimizations(context),
            notifications = hasNotificationPermission(context)
        )
    }
}