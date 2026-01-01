package co.adityarajput.notifilter.services

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
import kotlinx.coroutines.launch
import java.util.Calendar

class NotificationListener : NotificationListenerService() {
    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.Default + serviceJob)
    private val filtersRepository by lazy { AppContainer(this).filtersRepository }
    private val notificationsRepository by lazy { AppContainer(this).notificationsRepository }

    @Volatile
    private var filters: List<Filter> = emptyList()

    override fun onCreate() {
        super.onCreate()
        Log.d("NotificationListener", "Service created")

        serviceScope.launch {
            filtersRepository.list().collectLatest { newFilters ->
                filters = newFilters
                Log.d("NotificationListener", "Filters updated: $filters")
            }
        }
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        Log.d("NotificationListener", "Listener connected")
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val notification = Notification(
            sbn.notification.extras.getString("android.title") ?: "",
            sbn.notification.extras.getCharSequence("android.text")?.toString() ?: "",
            sbn.packageName,
        )
        Log.d("NotificationListener", "Received $notification")

        val calendar = Calendar.getInstance()
        val minutesOfDay = calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE)
        val filter = filters.find {
            notification.packageName == it.packageName &&
                    (Regex(it.queryPattern).containsMatchIn(notification.title) ||
                            Regex(it.queryPattern).containsMatchIn(notification.content)) &&
                    it.activeDays.contains(calendar.get(Calendar.DAY_OF_WEEK)) &&
                    it.activeTime.first <= minutesOfDay && minutesOfDay <= it.activeTime.second
        } ?: return

        Log.d("NotificationListener", "Matched $filter")

        if (!filter.enabled) {
            Log.d("NotificationListener", "Filter is disabled")
            return
        }

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
                    sbn.notification.actions.find { Regex(filter.buttonPattern!!).containsMatchIn(it.title) }?.actionIntent?.send()
                } catch (e: Exception) {
                    Log.e("NotificationListener", "Failed to tap button", e)
                    return
                }
        }

        serviceScope.launch {
            notificationsRepository.save(notification)
            filtersRepository.registerHit(filter)
        }
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        Log.d("NotificationListener", "Listener disconnected")
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceJob.cancel()
    }
}
