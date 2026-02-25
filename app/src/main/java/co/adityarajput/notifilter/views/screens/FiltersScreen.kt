package co.adityarajput.notifilter.views.screens

import android.content.Context.MODE_PRIVATE
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import androidx.lifecycle.viewmodel.compose.viewModel
import co.adityarajput.notifilter.Constants.SETTINGS
import co.adityarajput.notifilter.Constants.SHOW_MISSING_PERMISSIONS_DIALOG
import co.adityarajput.notifilter.R
import co.adityarajput.notifilter.data.models.Any
import co.adityarajput.notifilter.data.models.RegexTarget
import co.adityarajput.notifilter.utils.getFirst
import co.adityarajput.notifilter.utils.getToggleString
import co.adityarajput.notifilter.utils.isGranted
import co.adityarajput.notifilter.utils.permissionsRequired
import co.adityarajput.notifilter.viewmodels.FilterDialogState
import co.adityarajput.notifilter.viewmodels.FiltersViewModel
import co.adityarajput.notifilter.viewmodels.Provider
import co.adityarajput.notifilter.views.components.AppBar
import co.adityarajput.notifilter.views.components.ManageFilterDialog
import co.adityarajput.notifilter.views.components.MissingPermissionsDialog
import co.adityarajput.notifilter.views.components.Tile
import kotlinx.serialization.json.Json

@Composable
fun FiltersScreen(
    goToUpsertFilterScreen: (String) -> Unit,
    goToNotificationsScreen: () -> Unit,
    goToSettingsScreen: () -> Unit,
    viewModel: FiltersViewModel = viewModel(factory = Provider.Factory),
) {
    val context = LocalContext.current
    val state = viewModel.state.collectAsState()
    var hasPermissions by remember(state.value.filters) {
        mutableStateOf(context.isGranted(permissionsRequired(state.value.filters ?: listOf())))
    }
    var showMissingPermissionsDialog by remember {
        mutableStateOf(
            context.getSharedPreferences(SETTINGS, MODE_PRIVATE)
                .getBoolean(SHOW_MISSING_PERMISSIONS_DIALOG, true),
        )
    }

    Scaffold(
        topBar = {
            AppBar(stringResource(R.string.app_name), false) {
                IconButton(goToSettingsScreen) {
                    Icon(
                        painterResource(R.drawable.settings),
                        stringResource(R.string.settings),
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                }
                IconButton(goToNotificationsScreen) {
                    Icon(
                        painterResource(R.drawable.history),
                        stringResource(R.string.history),
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                { goToUpsertFilterScreen("null") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            ) { Icon(painterResource(R.drawable.add), stringResource(R.string.add_filter)) }
        },
    ) { paddingValues ->
        if (state.value.filters == null) {
            Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }
        } else if (state.value.filters!!.isEmpty()) {
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
                    .padding(paddingValues)
                    .padding(dimensionResource(R.dimen.padding_small))
                    .fillMaxSize(),
            ) {
                items(state.value.filters!!, { it.id }) {
                    Tile(
                        buildString {
                            append("/")
                            append(it.regexPattern)
                            append("/")

                            if (it.regexTarget == RegexTarget.AND) {
                                append(" && /")
                                append(it.secondaryRegexPattern)
                                append("/")
                            }
                        },
                        it.action.verb(),
                        if (it.app == Any) stringResource(R.string.any_app)
                        else it.app.name.getFirst(30),
                        if (!it.enabled) stringResource(R.string.filter_disabled)
                        else if (!it.historyEnabled) stringResource(R.string.history_disabled)
                        else pluralStringResource(R.plurals.hit, it.hits, it.hits),
                        it.schedule.description,
                        {
                            if (viewModel.selectedFilter == it) viewModel.selectedFilter = null
                            else viewModel.selectedFilter = it
                        },
                        null,
                        {
                            IconButton(
                                {
                                    viewModel.dialogState = FilterDialogState.TOGGLE_HISTORY
                                },
                            ) {
                                Icon(
                                    painterResource(R.drawable.manage_history),
                                    stringResource(
                                        R.string.toggle_history,
                                        it.historyEnabled.getToggleString(),
                                    ),
                                )
                            }
                            IconButton(
                                {
                                    viewModel.dialogState = FilterDialogState.TOGGLE_FILTER
                                },
                            ) {
                                Icon(
                                    if (it.enabled) painterResource(R.drawable.archive)
                                    else painterResource(R.drawable.unarchive),
                                    stringResource(
                                        R.string.toggle_filter,
                                        it.enabled.getToggleString(),
                                    ),
                                )
                            }
                            IconButton({ goToUpsertFilterScreen(Json.encodeToString(it)) }) {
                                Icon(
                                    painterResource(R.drawable.edit),
                                    stringResource(R.string.edit_filter),
                                )
                            }
                            IconButton(
                                { viewModel.dialogState = FilterDialogState.DELETE },
                                colors = IconButtonDefaults.iconButtonColors(
                                    contentColor = MaterialTheme.colorScheme.tertiary,
                                ),
                            ) {
                                Icon(
                                    painterResource(R.drawable.delete),
                                    stringResource(R.string.delete),
                                )
                            }
                        },
                        viewModel.selectedFilter == it,
                        true,
                    )
                }
                item { Box(Modifier.height(100.dp)) {} }
            }
        }
        if (viewModel.selectedFilter != null && viewModel.dialogState != null)
            ManageFilterDialog(viewModel)
        if (!hasPermissions.all { it.value } && showMissingPermissionsDialog) {
            MissingPermissionsDialog(
                hasPermissions.filter { !it.value }.keys,
                { showMissingPermissionsDialog = false },
                {
                    showMissingPermissionsDialog = false
                    context.getSharedPreferences(SETTINGS, MODE_PRIVATE)
                        .edit { putBoolean(SHOW_MISSING_PERMISSIONS_DIALOG, false) }
                },
            )
        }
    }
}
