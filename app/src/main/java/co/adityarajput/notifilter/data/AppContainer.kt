package co.adityarajput.notifilter.data

import android.content.Context
import co.adityarajput.notifilter.data.filter.FiltersRepository
import co.adityarajput.notifilter.data.notification.NotificationsRepository

class AppContainer(private val context: Context) {
    val filtersRepository: FiltersRepository by lazy {
        FiltersRepository(NotiFilterDatabase.getDatabase(context).filterDao())
    }
    val notificationsRepository: NotificationsRepository by lazy {
        NotificationsRepository(NotiFilterDatabase.getDatabase(context).notificationDao())
    }
}
