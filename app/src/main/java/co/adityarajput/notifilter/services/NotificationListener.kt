package co.adityarajput.notifilter.services

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import co.adityarajput.notifilter.data.AppContainer
import co.adityarajput.notifilter.data.filter.Filter
import co.adityarajput.notifilter.data.filter.FiltersRepository
import co.adityarajput.notifilter.data.notification.Notification
import co.adityarajput.notifilter.data.notification.NotificationsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class NotificationListener : NotificationListenerService() {
    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.Default + serviceJob)
    private val filtersRepository: FiltersRepository by lazy {
        AppContainer(this).filtersRepository
    }
    private val notificationsRepository: NotificationsRepository by lazy {
        AppContainer(this).notificationsRepository
    }

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
            title = sbn.notification.extras.getString("android.title") ?: "",
            content = sbn.notification.extras.getCharSequence("android.text")?.toString() ?: "",
            packageName = sbn.packageName,
        )
        Log.d("NotificationListener", "Received $notification")

        if (!sbn.isClearable) {
            Log.d("NotificationListener", "Is unclearable")
            return
        }

        val filter = filters.find {
            notification.packageName == it.packageName &&
                    (Regex(it.queryPattern).containsMatchIn(notification.title) ||
                            Regex(it.queryPattern).containsMatchIn(notification.content))
        } ?: return

        Log.d("NotificationListener", "Matched $filter")

        cancelNotification(sbn.key)
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
