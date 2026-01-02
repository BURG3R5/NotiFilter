package co.adityarajput.notifilter.data

import android.content.Context
import co.adityarajput.notifilter.data.active_notification.ActiveNotificationsRepository
import co.adityarajput.notifilter.data.filter.Action
import co.adityarajput.notifilter.data.filter.Filter
import co.adityarajput.notifilter.data.filter.FiltersRepository
import co.adityarajput.notifilter.data.notification.Notification
import co.adityarajput.notifilter.data.notification.NotificationsRepository
import kotlinx.coroutines.runBlocking

class AppContainer(private val context: Context) {
    val filtersRepository: FiltersRepository by lazy {
        FiltersRepository(NotiFilterDatabase.getDatabase(context).filterDao())
    }
    val notificationsRepository: NotificationsRepository by lazy {
        NotificationsRepository(NotiFilterDatabase.getDatabase(context).notificationDao())
    }
    val activeNotificationsRepository by lazy {
        ActiveNotificationsRepository(
            NotiFilterDatabase.getDatabase(context).activeNotificationDao(),
        )
    }

    fun seedDemoData() {
        runBlocking {
            filtersRepository.create(
                Filter(
                    "com.google.android.deskclock",
                    "Upcoming alarm",
                    Action.DISMISS,
                    activeTime = 9 * 60 to 17 * 60,
                    hits = 87,
                ),
            )
            filtersRepository.create(
                Filter(
                    "com.wssyncmldm",
                    "software update",
                    Action.TAP,
                    "Remind me",
                    activeDays = setOf(2, 3, 4, 5, 6),
                    hits = 23,
                ),
            )
            notificationsRepository.save(
                Notification(
                    "Upcoming alarm",
                    "Wed 8:30 AM - Wake up",
                    "com.wssyncmldm",
                    System.currentTimeMillis() - 3456789 - 4 * 3600000,
                ),
            )
            notificationsRepository.save(
                Notification(
                    "Upcoming alarm",
                    "Wed 11:50 AM - Exercise",
                    "com.wssyncmldm",
                    System.currentTimeMillis() - 3456789,
                ),
            )
            notificationsRepository.save(
                Notification(
                    "Download paused",
                    "A software update is available.",
                    "com.wssyncmldm",
                    System.currentTimeMillis() - 1234567,
                ),
            )
        }
    }
}
