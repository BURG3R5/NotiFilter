package co.adityarajput.notifilter.data

import co.adityarajput.notifilter.Constants
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

    fun history() = notificationDao.history()

    fun log() = notificationDao.widget()

    suspend fun registerHit(filter: Filter, notification: Notification) {
        filterDao.registerHit(filter.id)
        notificationDao.upsert(notification)

        val count = notificationDao.count()
        if (count > Constants.HISTORY_SIZE) {
            Logger.d(
                "Repository",
                "Deleting oldest ${count - Constants.HISTORY_SIZE} notification(s)",
            )
            notificationDao.listOldestN(count - Constants.HISTORY_SIZE).forEach {
                Cache.intents.remove(it.data.hashCode())
                notificationDao.delete(it)
            }
        }
    }

    suspend fun toggleHistory(filter: Filter) = filterDao.toggleHistory(filter.id)

    suspend fun toggleEnabled(filter: Filter) = filterDao.toggleEnabled(filter.id)

    suspend fun delete(filter: Filter) = filterDao.delete(filter)

    suspend fun delete(notification: Notification) {
        Cache.intents.remove(notification.data.hashCode())
        notificationDao.delete(notification)
    }

    suspend fun deleteFilters() = filterDao.deleteAll()

    suspend fun deleteNotifications() {
        Cache.intents.clear()
        notificationDao.deleteAll()
    }
}
