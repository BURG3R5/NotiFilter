package co.adityarajput.notifilter.views.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.viewmodel.compose.viewModel
import co.adityarajput.notifilter.R
import co.adityarajput.notifilter.data.Cache
import co.adityarajput.notifilter.utils.Logger
import co.adityarajput.notifilter.utils.getFirst
import co.adityarajput.notifilter.utils.toDelta
import co.adityarajput.notifilter.viewmodels.NotificationDialogState
import co.adityarajput.notifilter.viewmodels.NotificationsViewModel
import co.adityarajput.notifilter.viewmodels.Provider
import co.adityarajput.notifilter.views.components.AppBar
import co.adityarajput.notifilter.views.components.ManageHistoryDialog
import co.adityarajput.notifilter.views.components.Tile

@Composable
fun NotificationsScreen(
    goBack: () -> Unit,
    viewModel: NotificationsViewModel = viewModel(factory = Provider.Factory),
) {
    val context = LocalContext.current
    val state = viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            AppBar(stringResource(R.string.history), true, goBack) {
                IconButton({ viewModel.dialogState = NotificationDialogState.CLEAR_HISTORY }) {
                    Icon(
                        painterResource(R.drawable.clear_all),
                        stringResource(R.string.clear_history),
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        },
    ) { paddingValues ->
        if (state.value.notifications == null || viewModel.allPackages.isEmpty()) {
            Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }
        } else if (state.value.notifications!!.isEmpty()) {
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
                    .padding(paddingValues)
                    .padding(dimensionResource(R.dimen.padding_small))
                    .fillMaxSize(),
            ) {
                items(state.value.notifications!!, { it.id }) {
                    val intents = Cache.intents[it.data.hashCode()]

                    Tile(
                        it.title,
                        it.content,
                        it.appNameFrom(viewModel.allPackages).getFirst(30),
                        it.timestamp.toDelta(),
                        null,
                        {
                            try {
                                if (intents?.main != null) intents.launchMain() else {
                                    context.startActivity(
                                        context.packageManager.getLaunchIntentForPackage(
                                            it.origin,
                                        ),
                                    )
                                }
                            } catch (e: Exception) {
                                Logger.e("NotificationsScreen", "Error clicking $it", e)
                            }
                        },
                        { viewModel.delete(it) },
                        {
                            intents?.actions?.forEach { (title, intent) ->
                                Spacer(Modifier.width(dimensionResource(R.dimen.padding_medium)))
                                Text(
                                    title.uppercase(),
                                    Modifier.clickable { intent?.send() },
                                    style = MaterialTheme.typography.titleSmall.copy(MaterialTheme.colorScheme.primary),
                                )
                            }
                        },
                        true,
                    )
                }
            }
        }
        if (viewModel.dialogState != null)
            ManageHistoryDialog(viewModel)
    }
}
