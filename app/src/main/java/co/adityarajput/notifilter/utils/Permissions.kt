package co.adityarajput.notifilter.utils

import android.content.Context
import android.os.PowerManager
import android.provider.Settings

fun Context.hasNotificationListenerPermission() =
    Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
        ?.contains(packageName) ?: false

fun Context.hasAccessibilityServicePermission() =
    Settings.Secure.getString(contentResolver, "enabled_accessibility_services")
        ?.contains(packageName) ?: false

fun Context.hasUnrestrictedBackgroundUsagePermission() =
    (getSystemService(Context.POWER_SERVICE) as PowerManager)
        .isIgnoringBatteryOptimizations(packageName)
