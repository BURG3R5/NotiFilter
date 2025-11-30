package co.adityarajput.notifilter.utils

import android.content.Context
import android.os.PowerManager
import android.provider.Settings

fun Context.hasNotificationListenerPermission(): Boolean {
    return Settings.Secure.getString(
        this.contentResolver,
        "enabled_notification_listeners"
    )?.contains(this.packageName) ?: true
}

fun Context.hasUnrestrictedBackgroundUsagePermission(): Boolean {
    return (getSystemService(Context.POWER_SERVICE) as PowerManager)
        .isIgnoringBatteryOptimizations(this.packageName)
}
