package co.adityarajput.notifilter.services

import android.app.Notification.FLAG_GROUP_SUMMARY
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.pm.ApplicationInfo
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.core.app.NotificationCompat
import co.adityarajput.notifilter.Constants
import co.adityarajput.notifilter.R
import co.adityarajput.notifilter.data.AppContainer
import co.adityarajput.notifilter.data.models.Action
import co.adityarajput.notifilter.data.models.Filter
import co.adityarajput.notifilter.data.models.Notification
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit.MILLIS
import kotlin.math.min

class NotificationListener : NotificationListenerService() {
    companion object {
        @Volatile
        var instance: NotificationListener? = null

        fun updateForegroundStatus(runInForeground: Boolean) {
            if (runInForeground) {
                instance!!.startForeground()
            } else {
                instance!!.stopForeground(STOP_FOREGROUND_REMOVE)
            }
        }
    }

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.Default + serviceJob)
    private val repository by lazy { AppContainer(this).repository }
    private val sharedPreferences by lazy { getSharedPreferences(Constants.SETTINGS, MODE_PRIVATE) }

    @Volatile
    private var filters: List<Filter> = emptyList()

    @Volatile
    private var notifications: List<Notification> = emptyList()

    override fun onCreate() {
        super.onCreate()
        instance = this
        Log.d("NotificationListener", "Service created")

        if (sharedPreferences.getBoolean(Constants.RUN_IN_FOREGROUND, false))
            startForeground()

        serviceScope.launch {
            repository.filters().collectLatest { newFilters ->
                filters = newFilters
                Log.d("NotificationListener", "Filters updated: $filters")
            }
            notifications = repository.notifications().first()
        }
    }

    fun startForeground() {
        getSystemService(NotificationManager::class.java).createNotificationChannel(
            NotificationChannel(
                Constants.FOREGROUND_NOTIFICATION_CHANNEL_ID,
                "NotiFilter Foreground Service",
                NotificationManager.IMPORTANCE_LOW,
            ).apply {
                description = "Required for foreground service"
                enableLights(false)
                enableVibration(false)
                setShowBadge(false)
                setSound(null, null)
            },
        )
        startForeground(
            Constants.FOREGROUND_NOTIFICATION_ID,
            NotificationCompat.Builder(this, Constants.FOREGROUND_NOTIFICATION_CHANNEL_ID)
                .setContentTitle(getString(R.string.app_name_launcher))
                .setContentText(getString(R.string.foreground_notification_content))
                .setOngoing(true).setSilent(true).build(),
            if (SDK_INT >= UPSIDE_DOWN_CAKE) FOREGROUND_SERVICE_TYPE_SPECIAL_USE else 0,
        )
        Log.d("NotificationListener", "Promoted to foreground")
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        Log.d("NotificationListener", "Listener connected")
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        if (sbn.notification.flags and FLAG_GROUP_SUMMARY != 0) {
            Log.d("NotificationListener", "Ignoring group summary notification")
            return
        }

        var notification = Notification(sbn)
        Log.d("NotificationListener", "Received $notification")

        val filter = filters.filter {
            notification.origin == it.app.packageName
                    && it.enabled
                    && it.schedule.includesNow()
                    && it.matchesTextOf(notification)
        }.minByOrNull { it.id } ?: return

        Log.d("NotificationListener", "Matched $filter")

        when (filter.action) {
            is Action.DISMISS ->
                if (sbn.isClearable) {
                    cancelNotification(sbn.key)
                } else {
                    Log.d("NotificationListener", "Is unclearable")
                    snoozeNotification(sbn.key, 5 * 60 * 60 * 1000L)
                }

            is Action.TAP ->
                try {
                    sbn.notification.actions.find {
                        Regex(filter.action.buttonRegex).containsMatchIn(it.title)
                    }?.actionIntent?.send()
                } catch (e: Exception) {
                    Log.e("NotificationListener", "Failed to tap button", e)
                    return
                }

            is Action.BATCH -> {
                val zone = ZoneId.systemDefault()
                val now = ZonedDateTime.now(zone)
                val today = now.toLocalDate().atStartOfDay(zone)
                var batchLength = filter.action.batchLength * 60 * 60 * 1000L
                if (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0) {
                    // INFO: While debugging, batch for minutes instead of hours
                    batchLength /= 60
                }

                val untilNextBatch = batchLength - today.until(now, MILLIS) % batchLength

                if ((untilNextBatch < 1000L) || (batchLength - untilNextBatch < 1000L)) {
                    Log.d("NotificationListener", "Less than 1 second to batch boundary")
                    return
                }
                if (notifications.any(notification::isSimilar)) {
                    Log.d("NotificationListener", "Already snoozed")
                    return
                }

                snoozeNotification(
                    sbn.key,
                    min(untilNextBatch, now.until(today.plusDays(1), MILLIS)),
                )
            }

            is Action.DELAY -> {
                val zone = ZoneId.systemDefault()
                val now = ZonedDateTime.now(zone)
                val delay = now.until(
                    now.toLocalDate().atStartOfDay(zone)
                        .plusMinutes(filter.schedule.end.toLong()),
                    MILLIS,
                )

                if (delay < 1000L) {
                    Log.d("NotificationListener", "Less than 1 second until filter deactivation")
                    return
                }

                snoozeNotification(sbn.key, delay)
            }
        }

        if (!filter.historyEnabled) {
            Log.d("NotificationListener", "History is disabled for filter")
            return
        }

        serviceScope.launch {
            repository.registerHit(filter, notification.copy(origin = filter.app.name))
            notifications = repository.notifications().first()
            Log.d("NotificationListener", "Notifications updated: $notifications")
        }
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        if (instance == this) instance = null
        Log.d("NotificationListener", "Listener disconnected")
    }

    override fun onDestroy() {
        super.onDestroy()
        if (sharedPreferences.getBoolean(Constants.RUN_IN_FOREGROUND, false))
            stopForeground(STOP_FOREGROUND_REMOVE)
        serviceJob.cancel()
    }
}
