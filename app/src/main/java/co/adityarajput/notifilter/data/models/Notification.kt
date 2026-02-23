package co.adityarajput.notifilter.data.models

import android.service.notification.StatusBarNotification
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notifications")
data class Notification(
    val title: String,

    val content: String,

    val origin: String,

    val timestamp: Long,

    @ColumnInfo(defaultValue = "1")
    val showInHistory: Boolean = true,

    @ColumnInfo(defaultValue = "0")
    val showInWidget: Boolean = false,

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
) {
    constructor(
        sbn: StatusBarNotification,
        showInHistory: Boolean = true,
        showInWidget: Boolean = false,
        id: Int = 0,
    ) : this(
        sbn.notification.extras.getString("android.title") ?: "",
        sbn.notification.extras.getCharSequence("android.text")?.toString() ?: "",
        sbn.packageName, sbn.postTime, showInHistory, showInWidget, id,
    )

    val data get() = listOf(origin, title, content, timestamp)

    fun appNameFrom(packages: List<App>) =
        packages.find { it.packageName == origin }?.name ?: origin
}
