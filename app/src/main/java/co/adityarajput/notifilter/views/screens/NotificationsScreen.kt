package co.adityarajput.notifilter.views.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import co.adityarajput.notifilter.R
import co.adityarajput.notifilter.data.notification.Notification
import co.adityarajput.notifilter.utils.toShortHumanReadableTime
import co.adityarajput.notifilter.viewmodels.NotificationsViewModel
import co.adityarajput.notifilter.viewmodels.Provider
import co.adityarajput.notifilter.views.Theme
import co.adityarajput.notifilter.views.components.AppBar
import java.util.Date

@Composable
fun NotificationsScreen(
    goBack: () -> Unit,
    viewModel: NotificationsViewModel = viewModel(factory = Provider.Factory),
) {
    val notificationsState = viewModel.notificationsState.collectAsState()

    Scaffold(
        topBar = {
            AppBar(
                stringResource(R.string.block_history),
                true,
                goBack,
            )
        },
    ) { paddingValues ->
        if (notificationsState.value.notifications == null) {
            Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }
        } else if (notificationsState.value.notifications!!.isEmpty()) {
            Box(
                Modifier.fillMaxSize(),
                Alignment.Center,
            ) {
                Text(
                    stringResource(R.string.no_notifications),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        } else {
            LazyColumn(
                Modifier
                    .padding(dimensionResource(id = R.dimen.padding_small))
                    .fillMaxSize(),
                contentPadding = paddingValues
            ) {
                items(
                    notificationsState.value.notifications!!,
                    { it.id },
                ) { NotificationTile(it) }
            }
        }
    }
}

@Composable
private fun NotificationTile(notification: Notification) {
    Card(
        Modifier
            .fillMaxWidth()
            .padding(dimensionResource(R.dimen.padding_small)),
    ) {
        Column(
            Modifier
                .padding(dimensionResource(R.dimen.padding_large))
                .fillMaxWidth(),
            Arrangement.spacedBy(dimensionResource(R.dimen.padding_small))
        ) {
            Row(
                Modifier
                    .fillMaxWidth(),
                Arrangement.SpaceBetween,
                Alignment.CenterVertically,
            ) {
                Text(
                    notification.packageName,
                    style = MaterialTheme.typography.bodySmall,
                    fontStyle = FontStyle.Italic,
                )
                Text(
                    notification.timestamp.toShortHumanReadableTime(),
                    fontSize = 8.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Visible
                )
            }
            Text(
                notification.title,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                notification.content,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Preview
@Composable
private fun NotificationTilePreview() =
    Theme {
        NotificationTile(
            Notification(
                1,
                "Notification Title",
                "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor.",
                "com.example.app",
                Date().time - 123456,
            )
        )
    }
