package co.adityarajput.notifilter.utils

import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import co.adityarajput.notifilter.R
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

fun Long.toDelta(): String {
    val now = System.currentTimeMillis()
    val delta = now - this

    val seconds = delta / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24

    return when {
        days > 1000 -> "1k+ days ago"
        days > 0 -> "$days day${if (days > 1) "s" else ""} ago"
        hours > 0 -> "$hours hr${if (hours > 1) "s" else ""} ago"
        minutes > 0 -> "$minutes min${if (minutes > 1) "s" else ""} ago"
        seconds > 0 -> "$seconds sec${if (seconds > 1) "s" else ""} ago"
        else -> "just now"
    }
}

fun Long.toReadableTime(): String {
    return Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).toLocalDateTime().format(
        if (System.currentTimeMillis() - this > 24 * 60 * 60 * 1000)
            DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)
        else
            DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT),
    )
}

fun String.getFirst(length: Int): String =
    if (this.length <= length) this else this.take(length - 3) + "..."

fun Int.toHourMinuteString() =
    "${(this / 60).toString().padStart(2, '0')}:${(this % 60).toString().padStart(2, '0')}"

@Composable
fun Boolean.getToggleString(): String =
    stringResource(if (this) R.string.disable else R.string.enable)

@Suppress("DEPRECATION")
val Bundle.printable get() = "Bundle(${keySet().joinToString(", ") { "$it=${get(it)}" }})"
