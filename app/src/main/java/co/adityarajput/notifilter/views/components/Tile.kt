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
import co.adityarajput.notifilter.data.models.*
import co.adityarajput.notifilter.utils.getFirst
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
            App("Clock", "com.google.android.deskclock"),
            "Upcoming alarm",
            Action.DISMISS,
            RegexTarget.TITLE,
            enabled = false,
        ),
        Filter(
            App("Software update", "com.wssyncmldm"),
            "software update",
            Action.TAP_BUTTON("Remind me"),
            RegexTarget.CONTENT,
            schedule = Schedule(days = setOf(2, 3, 4, 5, 6)),
            hits = 23,
        ),
        Filter(
            App("Gmail", "com.google.android.gm"),
            "[Nn]ewsletter",
            Action.BATCH(3),
            RegexTarget.OR,
            historyEnabled = false,
        ),
        Filter(
            App("WhatsApp", "com.whatsapp"),
            "Book Club",
            Action.DELAY,
            RegexTarget.AND,
            "^Bob",
            schedule = Schedule(start = 9 * 60, end = 17 * 60),
            hits = 15,
        ),
        Filter(
            App("WhatsApp", "com.whatsapp"),
            "Roommate",
            Action.DEBOUNCE(2),
            RegexTarget.TITLE,
            historyEnabled = false,
        ),
    )

    Theme {
        Column {
            for (filter in filters)
                Tile(
                    buildString {
                        append("/")
                        append(filter.regexPattern)
                        append("/")

                        if (filter.regexTarget == RegexTarget.AND) {
                            append(" && /")
                            append(filter.secondaryRegexPattern)
                            append("/")
                        }
                    },
                    filter.action.verb(),
                    filter.app.name.getFirst(30),
                    if (!filter.enabled) stringResource(R.string.filter_disabled)
                    else if (!filter.historyEnabled) stringResource(R.string.history_disabled)
                    else pluralStringResource(R.plurals.hit, filter.hits, filter.hits),
                    filter.schedule.description,
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
        "App Name",
        System.currentTimeMillis() - 12345600,
    )

    Theme {
        Tile(
            notification.title,
            notification.content,
            notification.origin.getFirst(30),
            notification.timestamp.toShortHumanReadableTime(),
        )
    }
}
