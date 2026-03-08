package co.adityarajput.notifilter.utils

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlarmManager
import android.app.NotificationManager
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Context.POWER_SERVICE
import android.content.Intent
import android.os.Build
import android.os.Build.VERSION_CODES.TIRAMISU
import android.os.PowerManager
import android.provider.Settings
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.net.toUri
import co.adityarajput.notifilter.data.models.Action
import co.adityarajput.notifilter.data.models.Filter

enum class Permission {
    NOTIFICATION_LISTENER,
    ACCESSIBILITY_SERVICE,
    UNRESTRICTED_BACKGROUND_USAGE,
    POST_NOTIFICATIONS,
    NOTIFICATION_POLICY,
    SCHEDULE_EXACT_ALARM,
}

fun Context.isGranted(permission: Permission) = when (permission) {
    Permission.NOTIFICATION_LISTENER ->
        Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
            ?.contains(packageName) ?: false

    Permission.ACCESSIBILITY_SERVICE ->
        Settings.Secure.getString(contentResolver, "enabled_accessibility_services")
            ?.contains(packageName) ?: false

    Permission.UNRESTRICTED_BACKGROUND_USAGE ->
        (getSystemService(POWER_SERVICE) as PowerManager)
            .isIgnoringBatteryOptimizations(packageName)

    Permission.POST_NOTIFICATIONS ->
        (getSystemService(NOTIFICATION_SERVICE) as NotificationManager)
            .areNotificationsEnabled()

    Permission.NOTIFICATION_POLICY ->
        (getSystemService(NOTIFICATION_SERVICE) as NotificationManager)
            .isNotificationPolicyAccessGranted()

    Permission.SCHEDULE_EXACT_ALARM ->
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) true else
            (getSystemService(Context.ALARM_SERVICE) as AlarmManager)
                .canScheduleExactAlarms()
}

fun Context.isGranted(permissions: Iterable<Permission>) =
    permissions.associateWith(::isGranted).withDefault { false }

@SuppressLint("BatteryLife")
fun Context.request(permission: Permission, remove: Boolean = false) = try {
    when (permission) {
        Permission.NOTIFICATION_LISTENER ->
            startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))

        Permission.ACCESSIBILITY_SERVICE ->
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))

        Permission.UNRESTRICTED_BACKGROUND_USAGE, Permission.SCHEDULE_EXACT_ALARM ->
            startActivity(
                if (remove)
                    Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                else
                    Intent(
                        Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                        "package:${packageName}".toUri(),
                    ),
            )

        Permission.POST_NOTIFICATIONS ->
            if (Build.VERSION.SDK_INT >= TIRAMISU) {
                requestPermissions(
                    this as Activity,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    0,
                )
            } else {
                startActivity(
                    Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                        .apply { putExtra(Settings.EXTRA_APP_PACKAGE, packageName) },
                )
            }

        Permission.NOTIFICATION_POLICY ->
            startActivity(Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS))
    }
} catch (e: Exception) {
    Logger.e("Permissions", "Error while requesting $permission", e)
}

fun permissionsRequired(filters: List<Filter>) = buildList {
    if (filters.any { it.action is Action.TAP_NOTIFICATION })
        add(Permission.ACCESSIBILITY_SERVICE)

    if (filters.any { it.action is Action.ALERT })
        add(Permission.POST_NOTIFICATIONS)

    if (filters.any { it.action is Action.DISTURB })
        add(Permission.NOTIFICATION_POLICY)

    if (filters.any { it.action is Action.DISMISS_STALE })
        add(Permission.SCHEDULE_EXACT_ALARM)
}
