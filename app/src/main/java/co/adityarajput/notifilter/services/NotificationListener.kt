package co.adityarajput.notifilter.services

import android.app.Notification.FLAG_GROUP_SUMMARY
import android.content.pm.ApplicationInfo
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import co.adityarajput.notifilter.data.AppContainer
import co.adityarajput.notifilter.data.filter.Action
import co.adityarajput.notifilter.data.filter.Filter
import co.adityarajput.notifilter.data.notification.Notification
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit.MILLIS
import java.util.Calendar
import kotlin.math.min

class NotificationListener : NotificationListenerService() {
    companion object {
        @Volatile
        var instance: NotificationListener? = null
    }

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.Default + serviceJob)
    private val filtersRepository by lazy { AppContainer(this).filtersRepository }
    private val notificationsRepository by lazy { AppContainer(this).notificationsRepository }

    @Volatile
    private var filters: List<Filter> = emptyList()

    @Volatile
    private var notifications: List<Notification> = emptyList()

    override fun onCreate() {
        super.onCreate()
        instance = this
        Log.d("NotificationListener", "Service created")

        serviceScope.launch {
            filtersRepository.list().collectLatest { newFilters ->
                filters = newFilters
                Log.d("NotificationListener", "Filters updated: $filters")
            }
            notifications = notificationsRepository.list().first()
        }
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

        val notification = Notification(sbn)
        Log.d("NotificationListener", "Received $notification")

        val calendar = Calendar.getInstance()
        val minutesOfDay = calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE)
        val filter = filters.filter {
            notification.packageName == it.packageName &&
                    it.enabled &&
                    it.activeDays.contains(calendar.get(Calendar.DAY_OF_WEEK)) &&
                    it.activeTime.first <= minutesOfDay && minutesOfDay <= it.activeTime.second &&
                    it.matchesTextOf(notification)
        }.minByOrNull { it.id } ?: return

        Log.d("NotificationListener", "Matched $filter")

        when (filter.action) {
            Action.DISMISS ->
                if (sbn.isClearable) {
                    cancelNotification(sbn.key)
                } else {
                    Log.d("NotificationListener", "Is unclearable")
                    snoozeNotification(sbn.key, 5 * 60 * 60 * 1000L)
                }

            Action.TAP ->
                try {
                    sbn.notification.actions.find {
                        Regex(filter.buttonPattern!!).containsMatchIn(it.title)
                    }?.actionIntent?.send()
                } catch (e: Exception) {
                    Log.e("NotificationListener", "Failed to tap button", e)
                    return
                }

            Action.BATCH -> {
                val zone = ZoneId.systemDefault()
                val now = ZonedDateTime.now(zone)
                val today = now.toLocalDate().atStartOfDay(zone)
                var batchLength = filter.batchLengthInHours!! * 60 * 60 * 1000L
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

            Action.DELAY -> {
                val zone = ZoneId.systemDefault()
                val now = ZonedDateTime.now(zone)
                val delay = now.until(
                    now.toLocalDate().atStartOfDay(zone)
                        .plusMinutes(filter.activeTime.second.toLong()),
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
            notificationsRepository.save(notification)
            filtersRepository.registerHit(filter)
            notifications = notificationsRepository.list().first()
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
        serviceJob.cancel()
    }
}
