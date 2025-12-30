package co.adityarajput.notifilter.data.filter

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import co.adityarajput.notifilter.R
import java.util.Locale

@Entity(tableName = "filters")
data class Filter(
    val packageName: String,
    val queryPattern: String,
    @ColumnInfo(defaultValue = "DISMISS")
    val action: Action,
    @ColumnInfo(defaultValue = "null")
    val buttonPattern: String? = null,
    @ColumnInfo(defaultValue = "0,1439")
    val activeTime: Pair<Int, Int> = 0 to 1439,
    @ColumnInfo(defaultValue = "1,2,3,4,5,6,7")
    val activeDays: Set<Int> = setOf(1, 2, 3, 4, 5, 6, 7),
    val hits: Int = 0,
    @ColumnInfo(defaultValue = "1")
    val enabled: Boolean = true,

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
)

enum class Action(val displayString: Int, val descriptionString: Int) {
    DISMISS(R.string.dismiss_short, R.string.dismiss_long),
    TAP(R.string.tap_short, R.string.tap_long),
}

fun Filter.getScheduleString(): String {
    return buildString {
        when (activeDays) {
            setOf(1, 2, 3, 4, 5, 6, 7) -> append("")
            setOf(1, 2, 3, 4, 5) -> append("on weekdays ")
            setOf(6, 7) -> append("on weekends ")
            else -> {
                val daysList = activeDays.sorted().map {
                    when (it) {
                        1 -> "mon"
                        2 -> "tue"
                        3 -> "wed"
                        4 -> "thu"
                        5 -> "fri"
                        6 -> "sat"
                        else -> "sun"
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
                    activeTime.first % 60,
                ),
            )
        }
    }
}
