package co.adityarajput.notifilter.data

import co.adityarajput.notifilter.data.models.Filter
import co.adityarajput.notifilter.data.models.Notification
import co.adityarajput.notifilter.utils.Logger

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
            Logger.d("Repository.registerHit", "Deleting oldest ${count - 50} notification(s)")
            notificationDao.trim(count - 50)
        }
    }

    suspend fun toggleHistory(filter: Filter) = filterDao.toggleHistory(filter.id)

    suspend fun toggleEnabled(filter: Filter) = filterDao.toggleEnabled(filter.id)

    suspend fun delete(filter: Filter) = filterDao.delete(filter)

    suspend fun deleteFilters() = filterDao.deleteAll()
}
