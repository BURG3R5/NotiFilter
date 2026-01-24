package co.adityarajput.notifilter.data.models

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import co.adityarajput.notifilter.R
import co.adityarajput.notifilter.utils.toHourMinuteString
import kotlinx.serialization.Serializable
import java.util.Calendar

@Serializable
data class Schedule(
    val start: Int = 0,
    val end: Int = 1439,
    val days: Set<Int> = setOf(1, 2, 3, 4, 5, 6, 7),
) {
    val description
        @Composable get() = buildString {
            when (days) {
                setOf(1, 2, 3, 4, 5, 6, 7) -> append("")
                setOf(2, 3, 4, 5, 6) -> append(stringResource(R.string.on_weekdays))
                setOf(1, 7) -> append(stringResource(R.string.on_weekends))
                else -> {
                    append(
                        stringResource(
                            R.string.on_days,
                            stringArrayResource(R.array.days_short)
                                .filterIndexed { i, _ -> days.contains(i + 1) }
                                .joinToString(", "),
                        ),
                    )
                }
            }

            if (start != 0 || end != 1439) {
                append(
                    stringResource(
                        R.string.from_to,
                        start.toHourMinuteString(),
                        end.toHourMinuteString(),
                    ),
                )
            }
        }

    fun includesNow(): Boolean {
        val calendar = Calendar.getInstance()
        return days.contains(calendar.get(Calendar.DAY_OF_WEEK)) &&
                calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE) in start..end
    }

    fun isRangeValid() = start in 0..1439 && end in 0..1439 && start <= end
}
