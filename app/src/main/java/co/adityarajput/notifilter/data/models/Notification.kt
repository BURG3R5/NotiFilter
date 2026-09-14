package co.adityarajput.notifilter.data.models

import android.service.notification.StatusBarNotification
import androidx.room.*
import androidx.room.ForeignKey.Companion.SET_NULL
import kotlin.math.abs

@Entity(
    "notifications",
    foreignKeys = [
        ForeignKey(
            Filter::class,
            ["id"],
            ["filterId"],
            SET_NULL,
        ),
    ],
    indices = [
        Index(value = ["filterId"]),
    ],
)
data class Notification(
    val title: String,

    val content: String,

    val origin: String,

    val timestamp: Long,

    @ColumnInfo(defaultValue = "1")
    val showInHistory: Boolean = true,

    @ColumnInfo(defaultValue = "0")
    val showInWidget: Boolean = false,

    @ColumnInfo(defaultValue = "NULL")
    val filterId: Int? = null,

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
) {
    constructor(sbn: StatusBarNotification, id: Int = 0) : this(
        sbn.notification.extras.getString("android.title") ?: "",
        sbn.notification.extras.getCharSequence("android.text")?.toString() ?: "",
        sbn.packageName, sbn.postTime, id = id,
    )

    val data get() = listOf(origin, title, content, timestamp, filterId)

    /**
     * Checks whether this notification is the same as another,
     * apart from a time delay (tolerating 10 seconds of further mismatch).
     */
    fun matches(other: Notification, delay: Long = 0) =
        this.origin == other.origin
                && this.title == other.title
                && this.content == other.content
                && abs(abs(this.timestamp - other.timestamp) - delay) < 10 * 1000L
                && this.filterId == other.filterId

    fun appNameFrom(packages: List<App>) =
        packages.find { it.packageName == origin }?.name ?: origin
}
