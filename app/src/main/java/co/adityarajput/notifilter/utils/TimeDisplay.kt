package co.adityarajput.notifilter.utils

fun Long.toShortHumanReadableTime(): String {
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
