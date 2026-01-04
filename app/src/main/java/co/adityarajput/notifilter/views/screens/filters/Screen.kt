package co.adityarajput.notifilter.views.screens.filters

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.viewmodel.compose.viewModel
import co.adityarajput.notifilter.R
import co.adityarajput.notifilter.data.filter.getScheduleString
import co.adityarajput.notifilter.utils.getLast
import co.adityarajput.notifilter.utils.getToggleString
import co.adityarajput.notifilter.utils.withUnit
import co.adityarajput.notifilter.viewmodels.DialogState
import co.adityarajput.notifilter.viewmodels.FiltersViewModel
import co.adityarajput.notifilter.viewmodels.Provider
import co.adityarajput.notifilter.views.components.AppBar
import co.adityarajput.notifilter.views.components.Tile
import co.adityarajput.notifilter.views.icons.*

@Composable
fun FiltersScreen(
    goToNotificationsScreen: () -> Unit,
    goToAboutScreen: () -> Unit,
    viewModel: FiltersViewModel = viewModel(factory = Provider.Factory),
) {
    val filtersState = viewModel.filtersState.collectAsState()

    Scaffold(
        topBar = {
            AppBar(
                stringResource(R.string.app_name),
                false,
                goToAboutScreen,
                {
                    IconButton({ viewModel.showSettingsDialog = true }) {
                        Icon(
                            Settings,
                            stringResource(R.string.app_settings),
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                    IconButton(goToNotificationsScreen) {
                        Icon(
                            History,
                            stringResource(R.string.history),
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                { viewModel.showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            ) { Icon(Add, stringResource(R.string.add_filter)) }
        },
    ) { paddingValues ->
        if (filtersState.value.filters == null) {
            Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }
        } else if (filtersState.value.filters!!.isEmpty()) {
            Box(
                Modifier.fillMaxSize(),
                Alignment.Center,
            ) {
                Text(
                    stringResource(R.string.no_filters),
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
                items(filtersState.value.filters!!, { it.id }) {
                    Tile(
                        "/${it.queryPattern}/",
                        stringResource(it.action.displayString, it.buttonPattern ?: ' '),
                        it.packageName.getLast(30),
                        if (!it.enabled) stringResource(R.string.filter_disabled)
                        else if (!it.historyEnabled) stringResource(R.string.history_disabled)
                        else it.hits.withUnit(stringResource(R.string.hit)),
                        it.getScheduleString(),
                        { viewModel.selectedFilter = it },
                        {
                            IconButton({ viewModel.dialogState = DialogState.TOGGLE_HISTORY }) {
                                Icon(
                                    ToggleHistory,
                                    stringResource(
                                        R.string.toggle_history,
                                        it.historyEnabled.getToggleString(),
                                    ),
                                )
                            }
                            IconButton({ viewModel.dialogState = DialogState.TOGGLE_FILTER }) {
                                Icon(
                                    if (it.enabled) Archive else Unarchive,
                                    stringResource(
                                        R.string.toggle_filter,
                                        it.enabled.getToggleString(),
                                    ),
                                )
                            }
                            IconButton(
                                { viewModel.dialogState = DialogState.DELETE },
                                colors = IconButtonDefaults.iconButtonColors(
                                    contentColor = MaterialTheme.colorScheme.tertiary,
                                ),
                            ) { Icon(Delete, stringResource(R.string.delete)) }
                        },
                        viewModel.selectedFilter == it,
                        true,
                    )
                }
            }
        }
        if (viewModel.showAddDialog) AddFilterDialog(viewModel)
        if (viewModel.showSettingsDialog) SettingsDialog(viewModel)
        if (viewModel.selectedFilter != null && viewModel.dialogState != null) EditFilterDialog(
            viewModel,
        )
    }
}
