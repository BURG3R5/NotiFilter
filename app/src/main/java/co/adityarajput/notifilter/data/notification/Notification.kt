package co.adityarajput.notifilter.data.notification

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notifications")
data class Notification(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val content: String,
    val packageName: String,
    val timestamp: Long = System.currentTimeMillis(),
)
