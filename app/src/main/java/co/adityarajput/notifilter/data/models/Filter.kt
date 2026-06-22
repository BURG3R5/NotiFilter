package co.adityarajput.notifilter.data.models

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import co.adityarajput.notifilter.utils.containsMatchIn
import co.adityarajput.notifilter.utils.evaluateAgainst
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "filters")
data class Filter(
    @Embedded(prefix = "app_")
    val app: App,

    // INFO: Can also hold an expression to be evaulated
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

    @ColumnInfo(defaultValue = "0")
    val priority: Int = 0,

    val hits: Int = 0,

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
) {
    init {
        if (regexTarget == RegexTarget.AND) {
            requireNotNull(secondaryRegexPattern) { "Secondary pattern must be provided for AND target" }
        }
    }

    fun matchesTextOf(notification: Notification): Boolean {
        return when (regexTarget) {
            RegexTarget.TITLE ->
                regexPattern.containsMatchIn(notification.title)

            RegexTarget.CONTENT ->
                regexPattern.containsMatchIn(notification.content)

            RegexTarget.OR ->
                regexPattern.containsMatchIn(notification.title) ||
                        regexPattern.containsMatchIn(notification.content)

            RegexTarget.AND ->
                regexPattern.containsMatchIn(notification.title) &&
                        secondaryRegexPattern!!.containsMatchIn(notification.content)

            RegexTarget.EXPRESSION ->
                regexPattern.evaluateAgainst(notification)
        }
    }
}
