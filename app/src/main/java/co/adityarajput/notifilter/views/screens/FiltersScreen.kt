package co.adityarajput.notifilter.views.screens

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import co.adityarajput.notifilter.R
import co.adityarajput.notifilter.data.models.Action
import co.adityarajput.notifilter.data.models.Any
import co.adityarajput.notifilter.services.NotificationListener
import co.adityarajput.notifilter.services.Preferences
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
import kotlinx.coroutines.delay
import kotlinx.serialization.json.Json
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.seconds

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
        mutableStateOf(Preferences.showMissingPermissionsDialog)
    }
    var isAdjustingPriorities by remember { mutableStateOf(false) }
    var isListenerServiceInitialized by remember {
        mutableStateOf(NotificationListener.isServiceInitialized)
    }

    LaunchedEffect(Unit) {
        while (!isListenerServiceInitialized) {
            isListenerServiceInitialized = NotificationListener.isServiceInitialized
            delay(1.seconds)
        }
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
        } else if (!isListenerServiceInitialized) {
            Box(Modifier.fillMaxSize(), Alignment.Center) {
                Text(
                    AnnotatedString.fromHtml(
                        stringResource(R.string.listener_uninitialized),
                        TextLinkStyles(
                            SpanStyle(
                                MaterialTheme.colorScheme.primary,
                                textDecoration = TextDecoration.Underline,
                            ),
                        ),
                    ),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
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
                    var dropdownExpanded by remember { mutableStateOf(false) }
                    var dropdownOffset by remember { mutableStateOf(Offset.Zero) }

                    Box(
                        Modifier.pointerInput(Unit) {
                            detectTapGestures(
                                onLongPress = { position ->
                                    dropdownOffset = position
                                    viewModel.selectedFilter = it
                                    dropdownExpanded = true
                                },
                            )
                        },
                    ) {
                        Tile(
                            it.title,
                            it.action.verb(),
                            if (it.app == Any) stringResource(R.string.any_app)
                            else it.app.name.getFirst(30),
                            if (!it.enabled) stringResource(R.string.filter_disabled)
                            else if (!it.historyEnabled) stringResource(R.string.history_disabled)
                            else pluralStringResource(R.plurals.hit, it.hits, it.hits),
                            it.schedule.description,
                            buttons = {
                                IconButton({ viewModel.updatePriority(it, true) }) {
                                    Icon(
                                        painterResource(R.drawable.up),
                                        stringResource(R.string.move_up),
                                    )
                                }
                                IconButton({ viewModel.updatePriority(it, false) }) {
                                    Icon(
                                        painterResource(R.drawable.down),
                                        stringResource(R.string.move_down),
                                    )
                                }
                                IconButton({ isAdjustingPriorities = false }) {
                                    Icon(
                                        painterResource(R.drawable.check),
                                        stringResource(R.string.save),
                                    )
                                }
                            },
                            expanded = isAdjustingPriorities,
                            dividerBetweenTitleAndContent = true,
                        )
                        Box(
                            Modifier
                                .offset {
                                    IntOffset(
                                        dropdownOffset.x.roundToInt(),
                                        dropdownOffset.y.roundToInt(),
                                    )
                                }
                                .size(1.dp),
                        ) {
                            DropdownMenu(
                                dropdownExpanded,
                                {
                                    viewModel.selectedFilter = null
                                    dropdownExpanded = false
                                },
                            ) {
                                DropdownMenuItem(
                                    { Text(stringResource(R.string.edit_filter)) },
                                    {
                                        dropdownExpanded = false
                                        goToUpsertFilterScreen(Json.encodeToString(it))
                                    },
                                    leadingIcon = {
                                        Icon(
                                            painterResource(R.drawable.edit),
                                            stringResource(R.string.edit_filter),
                                        )
                                    },
                                )
                                DropdownMenuItem(
                                    {
                                        Text(
                                            stringResource(
                                                R.string.toggle_filter,
                                                it.enabled.getToggleString(),
                                            ),
                                        )
                                    },
                                    {
                                        dropdownExpanded = false
                                        viewModel.dialogState = FilterDialogState.TOGGLE_FILTER
                                    },
                                    leadingIcon = {
                                        Icon(
                                            if (it.enabled) painterResource(R.drawable.archive)
                                            else painterResource(R.drawable.unarchive),
                                            stringResource(
                                                R.string.toggle_filter,
                                                it.enabled.getToggleString(),
                                            ),
                                        )
                                    },
                                )
                                DropdownMenuItem(
                                    {
                                        Text(
                                            stringResource(
                                                R.string.toggle_history,
                                                it.historyEnabled.getToggleString(),
                                            ),
                                        )
                                    },
                                    {
                                        dropdownExpanded = false
                                        viewModel.dialogState = FilterDialogState.TOGGLE_HISTORY
                                    },
                                    leadingIcon = {
                                        Icon(
                                            painterResource(R.drawable.manage_history),
                                            stringResource(
                                                R.string.toggle_history,
                                                it.historyEnabled.getToggleString(),
                                            ),
                                        )
                                    },
                                )
                                DropdownMenuItem(
                                    { Text(stringResource(R.string.rearrange_filters)) },
                                    {
                                        dropdownExpanded = false
                                        viewModel.normalizePriorities()
                                        isAdjustingPriorities = true
                                    },
                                    leadingIcon = {
                                        Icon(
                                            painterResource(R.drawable.format_line_spacing),
                                            stringResource(R.string.rearrange_filters),
                                        )
                                    },
                                )
                                if (it.action is Action.REPLACE)
                                    DropdownMenuItem(
                                        { Text(stringResource(R.string.configure_notifications)) },
                                        {
                                            dropdownExpanded = false
                                            NotificationListener.createReplaceNotificationChannel(
                                                it.id,
                                                true,
                                            )
                                        },
                                        leadingIcon = {
                                            Icon(
                                                painterResource(R.drawable.notification_settings),
                                                stringResource(R.string.configure_notifications),
                                            )
                                        },
                                    )
                                DropdownMenuItem(
                                    { Text(stringResource(R.string.delete_filter)) },
                                    {
                                        dropdownExpanded = false
                                        viewModel.dialogState = FilterDialogState.DELETE
                                    },
                                    colors = MenuDefaults.itemColors(
                                        textColor = MaterialTheme.colorScheme.tertiary,
                                        leadingIconColor = MaterialTheme.colorScheme.tertiary,
                                    ),
                                    leadingIcon = {
                                        Icon(
                                            painterResource(R.drawable.delete),
                                            stringResource(R.string.delete_filter),
                                        )
                                    },
                                )
                            }
                        }
                    }
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
                    Preferences.showMissingPermissionsDialog = false
                    showMissingPermissionsDialog = false
                },
            )
        }
    }
}
