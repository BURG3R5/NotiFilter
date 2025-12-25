package co.adityarajput.notifilter.views.screens.notifications

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.viewmodel.compose.viewModel
import co.adityarajput.notifilter.R
import co.adityarajput.notifilter.utils.getLast
import co.adityarajput.notifilter.utils.toShortHumanReadableTime
import co.adityarajput.notifilter.viewmodels.NotificationsViewModel
import co.adityarajput.notifilter.viewmodels.Provider
import co.adityarajput.notifilter.views.components.AppBar
import co.adityarajput.notifilter.views.components.Tile

@Composable
fun NotificationsScreen(
    goBack: () -> Unit,
    viewModel: NotificationsViewModel = viewModel(factory = Provider.Factory),
) {
    val notificationsState = viewModel.notificationsState.collectAsState()

    Scaffold(
        topBar = {
            AppBar(
                stringResource(R.string.history),
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
                    .padding(dimensionResource(R.dimen.padding_small))
                    .fillMaxSize(),
                contentPadding = paddingValues,
            ) {
                items(notificationsState.value.notifications!!, { it.id }) {
                    Tile(
                        it.title,
                        it.content,
                        it.packageName.getLast(30),
                        it.timestamp.toShortHumanReadableTime(),
                    )
                }
            }
        }
    }
}
