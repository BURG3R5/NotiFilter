package co.adityarajput.notifilter.data.models

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "filters")
data class Filter(
    @Embedded(prefix = "app_")
    val app: App,

    val regexPattern: String,

    val action: Action,

    val regexTarget: RegexTarget = RegexTarget.OR,

    val secondaryRegexPattern: String? = null,

    @Embedded(prefix = "schedule_")
    val schedule: Schedule = Schedule(),

    val enabled: Boolean = true,

    val historyEnabled: Boolean = true,

    @ColumnInfo(defaultValue = "0")
    val widgetEnabled: Boolean = false,

    val hits: Int = 0,

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
) {
    fun matchesTextOf(notification: Notification): Boolean {
        return when (regexTarget) {
            RegexTarget.TITLE ->
                Regex(regexPattern).containsMatchIn(notification.title)

            RegexTarget.CONTENT ->
                Regex(regexPattern).containsMatchIn(notification.content)

            RegexTarget.OR ->
                Regex(regexPattern).containsMatchIn(notification.title) ||
                        Regex(regexPattern).containsMatchIn(notification.content)

            RegexTarget.AND ->
                Regex(regexPattern).containsMatchIn(notification.title) &&
                        Regex(secondaryRegexPattern!!).containsMatchIn(notification.content)
        }
    }
}
