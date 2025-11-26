package co.adityarajput.notifilter.data.notification

import android.util.Log

class NotificationsRepository(private val notificationDao: NotificationDao) {
    suspend fun save(notification: Notification) {
        notificationDao.create(notification)
        val count = notificationDao.count()
        if (count > 50) {
            Log.d("NotificationsRepository", "Deleting oldest ${count - 50} notification(s)")
            notificationDao.trim(count - 50)
        }
    }

    fun list() = notificationDao.list()
}
