package co.adityarajput.notifilter.views.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import co.adityarajput.notifilter.R
import co.adityarajput.notifilter.data.filter.Action
import co.adityarajput.notifilter.data.filter.Filter
import co.adityarajput.notifilter.data.filter.RegexTarget
import co.adityarajput.notifilter.data.notification.Notification
import co.adityarajput.notifilter.utils.getLast
import co.adityarajput.notifilter.utils.toShortHumanReadableTime
import co.adityarajput.notifilter.views.Theme

@Composable
fun Tile(
    title: String,
    content: String,
    leading: String,
    trailing: String? = null,
    preContent: String? = null,
    onClick: () -> Unit = {},
    buttons: @Composable RowScope.() -> Unit = {},
    expanded: Boolean = false,
    dividerBetweenTitleAndContent: Boolean = false,
) {
    Card(
        onClick,
        Modifier
            .fillMaxWidth()
            .padding(dimensionResource(R.dimen.padding_small))
            .animateContentSize(
                tween(
                    durationMillis = 300,
                    easing = LinearOutSlowInEasing,
                ),
            ),
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(dimensionResource(R.dimen.padding_large)),
            Arrangement.spacedBy(dimensionResource(R.dimen.padding_small)),
        ) {
            Row(
                Modifier.fillMaxWidth(),
                Arrangement.SpaceBetween,
                Alignment.CenterVertically,
            ) {
                Text(
                    leading,
                    style = MaterialTheme.typography.bodySmall.copy(
                        MaterialTheme.colorScheme.onSurfaceVariant,
                        11.sp,
                    ),
                )
                if (trailing != null)
                    Text(
                        trailing,
                        style = MaterialTheme.typography.bodySmall.copy(
                            MaterialTheme.colorScheme.onSurfaceVariant,
                            8.sp,
                        ),
                    )
            }
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
            )
            if (dividerBetweenTitleAndContent) HorizontalDivider()
            if (preContent != null && preContent.isNotEmpty())
                Text(
                    preContent,
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 11.sp,
                )
            Text(
                content,
                style = MaterialTheme.typography.bodySmall,
            )
            if (expanded) Row(Modifier.fillMaxWidth(), Arrangement.End) { buttons() }
        }
    }
}

@Preview
@Composable
private fun FilterTiles() {
    val filters = listOf(
        Filter(
            "com.wssyncmldm",
            "software update",
            null,
            RegexTarget.CONTENT,
            Action.TAP,
            "Remind me",
            activeTime = 9 * 60 to 17 * 60,
            activeDays = setOf(1, 2, 3, 4, 5),
            hits = 69,
        ),
        Filter(
            "com.sec.android.app.clockpackage",
            "upcoming",
            null,
            RegexTarget.TITLE,
            Action.DISMISS,
            enabled = false,
        ),
        Filter(
            "android",
            "is displaying over other apps",
            null,
            RegexTarget.OR,
            Action.BATCH,
            batchLengthInHours = 6,
            hits = 420,
        ),
        Filter(
            "com.sec.android.app.samsungapps",
            "Galaxy",
            "update available",
            RegexTarget.AND,
            Action.DISMISS,
        ),
    )

    Theme {
        Column {
            for (filter in filters)
                Tile(
                    buildString {
                        append("/")
                        append(filter.queryPattern)
                        append("/")

                        if (filter.regexTarget == RegexTarget.AND) {
                            append(" && /")
                            append(filter.secondaryQueryPattern)
                            append("/")
                        }
                    },
                    filter.getActionString(),
                    filter.packageName.getLast(30),
                    if (!filter.enabled) stringResource(R.string.filter_disabled)
                    else if (!filter.historyEnabled) stringResource(R.string.history_disabled)
                    else pluralStringResource(R.plurals.hit, filter.hits, filter.hits),
                    filter.getScheduleString(),
                    { },
                    { Text("BUTTONS") },
                    filter.enabled,
                )
        }
    }
}

@Preview
@Composable
private fun NotificationTile() {
    val notification = Notification(
        "Notification Title",
        "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor.",
        "com.example.app",
        System.currentTimeMillis() - 12345600,
    )

    Theme {
        Tile(
            notification.title,
            notification.content,
            notification.packageName.getLast(30),
            notification.timestamp.toShortHumanReadableTime(),
        )
    }
}
