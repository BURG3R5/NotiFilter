package co.adityarajput.notifilter.data.active_notification

import androidx.room.Embedded
import androidx.room.Entity
import co.adityarajput.notifilter.data.notification.Notification

@Entity("active_notifications", primaryKeys = ["id"])
data class ActiveNotification(@Embedded val notification: Notification)
