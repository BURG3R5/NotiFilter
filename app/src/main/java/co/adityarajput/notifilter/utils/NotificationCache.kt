package co.adityarajput.notifilter.utils

import android.app.PendingIntent
import co.adityarajput.notifilter.data.models.Notification
import java.util.Collections

object NotificationCache {
    private val cache = Collections.synchronizedMap(mutableMapOf<String, PendingIntent>())

    fun put(notification: Notification, intent: PendingIntent?) {
        if (intent == null) return
        val key = getCacheKey(notification)
        cache[key] = intent
    }

    fun get(notification: Notification): PendingIntent? {
        return cache[getCacheKey(notification)]
    }

    private fun getCacheKey(notification: Notification): String {
        return "${notification.packageName}|${notification.title}|${notification.content}|${notification.timestamp}"
    }

    fun clear() {
        cache.clear()
    }
}
