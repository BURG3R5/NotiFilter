package co.adityarajput.notifilter.data.filter

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import co.adityarajput.notifilter.R
import co.adityarajput.notifilter.data.notification.Notification
import co.adityarajput.notifilter.utils.toHourMinuteString
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "filters")
data class Filter(
    val packageName: String,
    val queryPattern: String,
    @ColumnInfo(defaultValue = "null")
    val secondaryQueryPattern: String?,
    @ColumnInfo(defaultValue = "OR")
    val regexTarget: RegexTarget = RegexTarget.OR,
    @ColumnInfo(defaultValue = "DISMISS")
    val action: Action,
    @ColumnInfo(defaultValue = "null")
    val buttonPattern: String? = null,
    @ColumnInfo(defaultValue = "null")
    val batchLengthInHours: Int? = null,
    @ColumnInfo(defaultValue = "0,1439")
    val activeTime: Pair<Int, Int> = 0 to 1439,
    @ColumnInfo(defaultValue = "1,2,3,4,5,6,7")
    val activeDays: Set<Int> = setOf(1, 2, 3, 4, 5, 6, 7),
    val hits: Int = 0,
    @ColumnInfo(defaultValue = "1")
    val enabled: Boolean = true,
    @ColumnInfo(defaultValue = "1")
    val historyEnabled: Boolean = true,

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
) {
    fun matchesTextOf(notification: Notification): Boolean {
        return when (regexTarget) {
            RegexTarget.TITLE ->
                Regex(queryPattern).containsMatchIn(notification.title)

            RegexTarget.CONTENT ->
                Regex(queryPattern).containsMatchIn(notification.content)

            RegexTarget.OR ->
                Regex(queryPattern).containsMatchIn(notification.title) ||
                        Regex(queryPattern).containsMatchIn(notification.content)

            RegexTarget.AND ->
                Regex(queryPattern).containsMatchIn(notification.title) &&
                        Regex(secondaryQueryPattern!!).containsMatchIn(notification.content)
        }
    }

    @Composable
    fun getActionString(): String {
        return when (action) {
            Action.DISMISS -> stringResource(R.string.dismiss_short)
            Action.TAP -> stringResource(R.string.tap_short, buttonPattern!!)
            Action.BATCH -> stringResource(
                R.string.batch_short,
                pluralStringResource(R.plurals.hour, batchLengthInHours!!, batchLengthInHours),
            )

            Action.DELAY -> stringResource(R.string.delay_short)
        }
    }

    @SuppressLint("DefaultLocale")
    @Composable
    fun getScheduleString(): String {
        return buildString {
            when (activeDays) {
                setOf(1, 2, 3, 4, 5, 6, 7) -> append("")
                setOf(2, 3, 4, 5, 6) -> append(stringResource(R.string.on_weekdays))
                setOf(1, 7) -> append(stringResource(R.string.on_weekends))
                else -> {
                    append(
                        stringResource(
                            R.string.on_days,
                            stringArrayResource(R.array.days_short)
                                .filterIndexed { i, _ -> activeDays.contains(i + 1) }
                                .joinToString(", "),
                        ),
                    )
                }
            }

            if (activeTime != (0 to 1439)) {
                append(
                    stringResource(
                        R.string.from_to,
                        activeTime.first.toHourMinuteString(),
                        activeTime.second.toHourMinuteString(),
                    ),
                )
            }
        }
    }
}

@Serializable
enum class Action(val description: Int) {
    DISMISS(R.string.dismiss_long),
    TAP(R.string.tap_long),
    BATCH(R.string.batch_long),
    DELAY(R.string.delay_long),
}

@Serializable
enum class RegexTarget(val description: Int) {
    TITLE(R.string.title),
    CONTENT(R.string.content),
    OR(R.string.title_or_content),
    AND(R.string.title_and_content),
}
