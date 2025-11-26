package co.adityarajput.notifilter.utils

import android.content.Context
import android.provider.Settings

fun Context.hasNotificationListenerPermission(): Boolean {
    return Settings.Secure.getString(
        this.contentResolver,
        "enabled_notification_listeners"
    )?.contains(this.packageName) ?: true
}
