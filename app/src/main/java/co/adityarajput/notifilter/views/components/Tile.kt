package co.adityarajput.notifilter.views.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import co.adityarajput.notifilter.R
import co.adityarajput.notifilter.data.filter.Action
import co.adityarajput.notifilter.data.filter.Filter
import co.adityarajput.notifilter.data.notification.Notification
import co.adityarajput.notifilter.utils.getLast
import co.adityarajput.notifilter.utils.toShortHumanReadableTime
import co.adityarajput.notifilter.utils.withUnit
import co.adityarajput.notifilter.views.Theme
import java.util.Date

@Composable
fun Tile(
    title: String,
    content: String,
    subtitle: String,
    trailing: String,
    onClick: () -> Unit = {},
    dividerBetweenTitleAndContent: Boolean = false,
) {
    Card(
        onClick,
        Modifier
            .fillMaxWidth()
            .padding(dimensionResource(R.dimen.padding_small)),
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
                    subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        MaterialTheme.colorScheme.onSurfaceVariant,
                        11.sp,
                    ),
                )
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
            Text(
                content,
                style = MaterialTheme.typography.bodySmall,
            )
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
            Action.TAP,
            "Remind me",
            69,
        ),
        Filter(
            "com.sec.android.app.clockpackage",
            "upcoming",
            Action.DISMISS,
            enabled = false,
        ),
    )

    Theme {
        Column {
            for (filter in filters)
                Tile(
                    "/${filter.queryPattern}/",
                    stringResource(filter.action.displayString, filter.buttonPattern ?: ' '),
                    filter.packageName.getLast(30),
                    if (filter.enabled) filter.hits.withUnit(stringResource(R.string.hit))
                    else stringResource(R.string.disabled),
                    { },
                    true,
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
        Date().time - 12345600,
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
