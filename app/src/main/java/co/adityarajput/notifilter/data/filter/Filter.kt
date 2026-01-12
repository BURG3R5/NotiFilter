package co.adityarajput.notifilter.data.filter

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import co.adityarajput.notifilter.R
import kotlinx.serialization.Serializable
import java.util.Locale

@Serializable
@Entity(tableName = "filters")
data class Filter(
    val packageName: String,
    val queryPattern: String,
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
)

@Serializable
enum class Action(val description: Int) {
    DISMISS(R.string.dismiss_long),
    TAP(R.string.tap_long),
    BATCH(R.string.batch_long),
}

@Composable
fun Filter.getActionString(): String {
    return when (action) {
        Action.DISMISS -> stringResource(R.string.dismiss_short)
        Action.TAP -> stringResource(R.string.tap_short, buttonPattern!!)
        Action.BATCH -> stringResource(
            R.string.batch_short,
            pluralStringResource(R.plurals.hour, batchLengthInHours!!, batchLengthInHours),
        )
    }
}

fun Filter.getScheduleString(): String {
    return buildString {
        when (activeDays) {
            setOf(1, 2, 3, 4, 5, 6, 7) -> append("")
            setOf(2, 3, 4, 5, 6) -> append("on weekdays ")
            setOf(1, 7) -> append("on weekends ")
            else -> {
                val daysList = activeDays.sorted().map {
                    when (it) {
                        1 -> "sun"
                        2 -> "mon"
                        3 -> "tue"
                        4 -> "wed"
                        5 -> "thu"
                        6 -> "fri"
                        else -> "sat"
                    }
                }
                append("on " + daysList.joinToString(", ") + " ")
            }
        }

        if (activeTime != (0 to 1439)) {
            append(
                String.format(
                    Locale.getDefault(),
                    "from %02d:%02d to %02d:%02d",
                    activeTime.first / 60,
                    activeTime.first % 60,
                    activeTime.second / 60,
                    activeTime.second % 60,
                ),
            )
        }
    }
}
