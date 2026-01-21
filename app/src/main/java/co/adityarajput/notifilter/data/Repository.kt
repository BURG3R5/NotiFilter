package co.adityarajput.notifilter.data

import android.util.Log
import co.adityarajput.notifilter.data.filter.Filter
import co.adityarajput.notifilter.data.filter.FilterDao
import co.adityarajput.notifilter.data.notification.Notification
import co.adityarajput.notifilter.data.notification.NotificationDao

class Repository(
    private val filterDao: FilterDao,
    private val notificationDao: NotificationDao,
) {
    suspend fun upsert(vararg filters: Filter) = filterDao.upsert(*filters)

    suspend fun upsert(vararg notifications: Notification) = notificationDao.upsert(*notifications)

    fun filters() = filterDao.list()

    fun notifications() = notificationDao.list()

    suspend fun registerHit(filter: Filter, notification: Notification) {
        filterDao.registerHit(filter.id)
        notificationDao.upsert(notification)

        val count = notificationDao.count()
        if (count > 50) {
            Log.d("Repository", "Deleting oldest ${count - 50} notification(s)")
            notificationDao.trim(count - 50)
        }
    }

    suspend fun toggleHistory(filter: Filter) = filterDao.toggleHistory(filter.id)

    suspend fun toggleEnabled(filter: Filter) = filterDao.toggleEnabled(filter.id)

    suspend fun delete(filter: Filter) = filterDao.delete(filter)

    suspend fun deleteFilters() = filterDao.deleteAll()
}
