package co.adityarajput.notifilter.data.models

import android.service.notification.StatusBarNotification
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notifications")
data class Notification(
    val title: String,

    val content: String,

    val origin: String,

    val packageName: String,

    val timestamp: Long,

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
) {
    constructor(sbn: StatusBarNotification, appName: String? = null, id: Int = 0) : this(
        sbn.notification.extras.getString("android.title") ?: "",
        sbn.notification.extras.getCharSequence("android.text")?.toString() ?: "",
        appName ?: sbn.packageName,
        sbn.packageName,
        sbn.postTime,
        id,
    )

    fun isSimilar(other: Notification): Boolean {
        return this.packageName == other.packageName &&
                this.title == other.title &&
                this.content == other.content &&
                this.timestamp == other.timestamp
    }
}
