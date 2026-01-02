package co.adityarajput.notifilter.data.active_notification

import android.util.Log

class ActiveNotificationsRepository(private val dao: ActiveNotificationDao) {
    suspend fun create(notification: ActiveNotification) {
        dao.create(notification)
        val count = dao.count()
        if (count > 10) {
            Log.d(
                "ActiveNotificationsRepository",
                "Deleting oldest ${count - 10} active notification(s)",
            )
            dao.trim(count - 10)
        }
    }

    fun list() = dao.list()

    suspend fun delete(id: Int) = dao.delete(id)

    suspend fun deleteAll() = dao.deleteAll()
}
