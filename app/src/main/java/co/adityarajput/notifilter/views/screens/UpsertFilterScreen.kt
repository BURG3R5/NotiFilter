package co.adityarajput.notifilter.views.screens

import android.app.TimePickerDialog
import android.os.Handler
import android.os.Looper
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.lifecycle.viewmodel.compose.viewModel
import co.adityarajput.notifilter.R
import co.adityarajput.notifilter.data.models.*
import co.adityarajput.notifilter.services.NotificationListener
import co.adityarajput.notifilter.utils.*
import co.adityarajput.notifilter.viewmodels.FormError
import co.adityarajput.notifilter.viewmodels.FormPage
import co.adityarajput.notifilter.viewmodels.Provider
import co.adityarajput.notifilter.viewmodels.UpsertFilterViewModel
import co.adityarajput.notifilter.views.components.*
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun UpsertFilterScreen(
    filterString: String,
    goBack: () -> Unit,
    viewModel: UpsertFilterViewModel = viewModel(factory = Provider.createUFVM(filterString)),
) {
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            AppBar(
                stringResource(
                    if (viewModel.state.values.filterId == 0) R.string.add_filter
                    else R.string.edit_filter,
                ),
                true,
                goBack,
            )
        },
    ) { paddingValues ->
        Column(
            Modifier.padding(paddingValues),
            Arrangement.SpaceBetween,
        ) {
            AnimatedContent(
                viewModel.state.page,
                Modifier
                    .weight(1f)
                    .padding(dimensionResource(R.dimen.padding_small))
                    .padding(
                        dimensionResource(R.dimen.padding_large),
                        dimensionResource(R.dimen.padding_medium),
                    ),
                { fadeIn() togetherWith fadeOut() },
            ) {
                Column(
                    Modifier.fillMaxWidth().run {
                        if (it == FormPage.ACTION) this.verticalScroll(rememberScrollState())
                        else this
                    },
                    Arrangement.spacedBy(dimensionResource(R.dimen.padding_medium)),
                ) {
                    when (it) {
                        FormPage.ZAPPER -> ZapperPage(viewModel)

                        FormPage.PACKAGE -> PackagePage(viewModel)

                        FormPage.PATTERN -> PatternPage(viewModel)

                        FormPage.ACTION -> ActionPage(viewModel)

                        FormPage.SCHEDULE -> SchedulePage(viewModel)
                    }
                }
            }
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = dimensionResource(R.dimen.padding_large)),
                Arrangement.Center,
                Alignment.Bottom,
            ) {
                TextButton(
                    {
                        if (viewModel.state.page.isFirstPage()) {
                            goBack()
                        } else {
                            viewModel.updateForm(
                                viewModel.state.page.previous(),
                                viewModel.state.values,
                            )
                        }
                    },
                    Modifier
                        .fillMaxWidth(0.5f)
                        .padding(end = dimensionResource(R.dimen.padding_small)),
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary),
                ) {
                    Text(
                        if (viewModel.state.page.isFirstPage()) stringResource(R.string.cancel)
                        else stringResource(R.string.back),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Normal,
                    )
                }
                TextButton(
                    {
                        if (!viewModel.state.page.isFinalPage()) {
                            viewModel.updateForm(
                                viewModel.state.page.next(),
                                viewModel.state.values,
                            )
                        } else {
                            coroutineScope.launch {
                                viewModel.submitForm()
                                goBack()
                            }
                        }
                    },
                    Modifier
                        .fillMaxWidth()
                        .padding(start = dimensionResource(R.dimen.padding_small)),
                    viewModel.state.error == null,
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.secondary),
                ) {
                    Text(
                        if (viewModel.state.page.isFirstPage()) stringResource(R.string.skip)
                        else if (viewModel.state.page.isFinalPage()) {
                            if (viewModel.state.values.filterId == 0) stringResource(R.string.add)
                            else stringResource(R.string.save)
                        } else stringResource(R.string.next),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }
        }
    }
}

@Composable
private fun ZapperPage(viewModel: UpsertFilterViewModel) {
    if (viewModel.allPackages.isEmpty()) {
        Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }
    } else if (viewModel.activeNotifications.isEmpty()) {
        Box(Modifier.fillMaxSize(), Alignment.Center) {
            Text(
                stringResource(R.string.no_active_notifications),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    } else {
        Text(
            stringResource(R.string.zapper_page_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Normal,
        )
        LazyColumn(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = dimensionResource(R.dimen.padding_small)),
        ) {
            items(viewModel.activeNotifications, { it.id }) {
                val appName = it.appNameFrom(viewModel.allPackages)

                Tile(
                    it.title,
                    it.content,
                    appName.getFirst(30),
                    onClick = {
                        viewModel.updateForm(
                            FormPage.PATTERN,
                            viewModel.state.values.copy(
                                app = App(appName, it.origin),
                                notification = it,
                            ),
                        )
                    },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PackagePage(viewModel: UpsertFilterViewModel) {
    var searchString by remember {
        mutableStateOf(
            if (viewModel.state.values.app == Any) ""
            else viewModel.state.values.app.name,
        )
    }
    var visibleItemsCount by remember { mutableIntStateOf(10) }
    var showAdvancedOptions by remember { mutableStateOf(viewModel.state.values.app == Any) }
    var showSystemPackages by remember { mutableStateOf(false) }

    val (apps, searchFinished) = (if (showSystemPackages) viewModel.allPackages else viewModel.visibleApps)
        .filterFirst(visibleItemsCount) { it.toString().contains(searchString, true) }

    Text(
        stringResource(R.string.package_page_title),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Normal,
    )
    OutlinedTextField(
        searchString,
        { searchString = it },
        Modifier.fillMaxWidth(),
        label = { Text(stringResource(R.string.package_name)) },
        placeholder = { Text(stringResource(R.string.package_name_placeholder)) },
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
            unfocusedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
            disabledContainerColor = MaterialTheme.colorScheme.secondaryContainer,
        ),
        singleLine = true,
    )
    Column(
        Modifier.padding(dimensionResource(R.dimen.padding_medium)),
        Arrangement.spacedBy(dimensionResource(R.dimen.padding_medium)),
    ) {
        Row(
            Modifier.toggleable(showAdvancedOptions) { showAdvancedOptions = it },
            Arrangement.spacedBy(dimensionResource(R.dimen.padding_small)),
            Alignment.CenterVertically,
        ) {
            Checkbox(showAdvancedOptions, null)
            Text(
                stringResource(R.string.advanced_options),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Normal,
            )
        }
        if (showAdvancedOptions) {
            Row(
                Modifier
                    .padding(horizontal = dimensionResource(R.dimen.padding_small))
                    .toggleable(viewModel.state.values.app == Any) {
                        viewModel.updateForm(
                            FormPage.PACKAGE,
                            viewModel.state.values.copy(app = if (it) Any else None),
                        )
                    },
                Arrangement.spacedBy(dimensionResource(R.dimen.padding_small)),
                Alignment.CenterVertically,
            ) {
                Checkbox(viewModel.state.values.app == Any, null)
                Column {
                    Text(
                        stringResource(R.string.target_all_apps),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Normal,
                    )
                    Text(
                        stringResource(R.string.all_apps_warning),
                        Modifier.padding(top = dimensionResource(R.dimen.padding_small)),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Normal,
                    )
                }
            }
            Row(
                Modifier
                    .padding(horizontal = dimensionResource(R.dimen.padding_small))
                    .toggleable(showSystemPackages) { showSystemPackages = it },
                Arrangement.spacedBy(dimensionResource(R.dimen.padding_small)),
                Alignment.CenterVertically,
            ) {
                Checkbox(showSystemPackages, null)
                Column {
                    Text(
                        stringResource(R.string.show_system_packages),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Normal,
                    )
                    Text(
                        stringResource(R.string.system_packages_warning),
                        Modifier.padding(top = dimensionResource(R.dimen.padding_small)),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Normal,
                    )
                }
            }
        }
    }
    FlowRow(
        Modifier.verticalScroll(rememberScrollState()),
        Arrangement.spacedBy(dimensionResource(R.dimen.padding_small)),
        Arrangement.spacedBy(dimensionResource(R.dimen.padding_small)),
    ) {
        apps.forEach {
            FilterChip(
                it == viewModel.state.values.app,
                {
                    viewModel.updateForm(
                        FormPage.PATTERN,
                        viewModel.state.values.copy(app = it),
                    )
                },
                { Text(it.name) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                ),
            )
        }
        if (!searchFinished)
            FilterChip(
                false,
                { visibleItemsCount += 10 },
                { Text("...") },
                colors = FilterChipDefaults.filterChipColors(
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                ),
            )
    }
}

@Composable
private fun PatternPage(viewModel: UpsertFilterViewModel) {
    Text(
        stringResource(R.string.pattern_page_title),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Normal,
    )
    Text(
        stringResource(R.string.regex_target),
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Normal,
    )
    FlowRow(verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small))) {
        RegexTarget.entries.forEach {
            Row(
                Modifier
                    .fillMaxWidth(0.5f)
                    .selectable((it == viewModel.state.values.regexTarget)) {
                        viewModel.updateForm(
                            viewModel.state.page,
                            viewModel.state.values.copy(regexTarget = it),
                        )
                    },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RadioButton(
                    (it == viewModel.state.values.regexTarget),
                    null,
                    Modifier.padding(horizontal = dimensionResource(R.dimen.padding_small)),
                )
                Text(
                    stringResource(it.description),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Normal,
                )
            }
        }
    }
    OutlinedTextField(
        viewModel.state.values.queryPattern,
        {
            viewModel.updateForm(
                viewModel.state.page,
                viewModel.state.values.copy(queryPattern = it),
            )
        },
        Modifier.fillMaxWidth(),
        label = {
            Text(
                stringResource(
                    if (viewModel.state.values.regexTarget == RegexTarget.AND) R.string.title_pattern
                    else R.string.notification_pattern,
                ),
            )
        },
        placeholder = { Text(stringResource(R.string.pattern_placeholder)) },
        supportingText = { SupportingText(viewModel, true) },
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
            unfocusedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
            disabledContainerColor = MaterialTheme.colorScheme.secondaryContainer,
        ),
        singleLine = true,
    )
    AnimatedVisibility(viewModel.state.values.regexTarget == RegexTarget.AND) {
        OutlinedTextField(
            viewModel.state.values.secondaryQueryPattern,
            {
                viewModel.updateForm(
                    viewModel.state.page,
                    viewModel.state.values.copy(secondaryQueryPattern = it),
                )
            },
            Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.content_pattern)) },
            placeholder = { Text(stringResource(R.string.pattern_placeholder)) },
            supportingText = { SupportingText(viewModel, false) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                unfocusedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                disabledContainerColor = MaterialTheme.colorScheme.secondaryContainer,
            ),
            singleLine = true,
        )
    }
    Text(
        AnnotatedString.fromHtml(
            stringResource(R.string.pattern_advice),
            TextLinkStyles(
                SpanStyle(
                    MaterialTheme.colorScheme.primary,
                    textDecoration = TextDecoration.Underline,
                ),
            ),
        ),
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Normal,
    )
    if (viewModel.state.error == FormError.INVALID_NOTIFICATION_REGEX) ErrorText(R.string.invalid_regex)
    viewModel.state.warnings.forEach { WarningText(it.description) }
}

private val permissions = listOf(
    Permission.ACCESSIBILITY_SERVICE,
    Permission.POST_NOTIFICATIONS,
    Permission.NOTIFICATION_POLICY,
    Permission.SCHEDULE_EXACT_ALARM,
)

@Composable
private fun ColumnScope.ActionPage(viewModel: UpsertFilterViewModel) {
    val context = LocalContext.current
    val handler = remember { Handler(Looper.getMainLooper()) }
    var hasPermissions by remember { mutableStateOf(context.isGranted(permissions)) }
    val hasPinnedWidget by produceState(initialValue = false) { value = context.isWidgetUsed() }

    val watcher = object : Runnable {
        override fun run() {
            hasPermissions = context.isGranted(permissions)

            if (hasPermissions.getValue(Permission.POST_NOTIFICATIONS))
                NotificationListener.createAlertNotificationChannel()

            if (!hasPermissions.all { it.value })
                handler.postDelayed(this, 500)
        }
    }
    DisposableEffect(Unit) {
        handler.post(watcher)
        onDispose { handler.removeCallbacksAndMessages(null) }
    }

    Text(
        stringResource(R.string.action_page_title),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Normal,
    )
    Action.entries.forEach {
        Row(
            Modifier
                .fillMaxWidth()
                .selectable((it == viewModel.state.values.action)) {
                    viewModel.updateForm(
                        viewModel.state.page,
                        viewModel.state.values.copy(action = it),
                    )
                },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RadioButton(
                viewModel.state.values.action.isOfType(it),
                null,
                Modifier.padding(horizontal = dimensionResource(R.dimen.padding_small)),
            )
            Text(
                it.description(),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Normal,
            )
        }
        AnimatedVisibility(
            it is Action.TAP_NOTIFICATION
                    && viewModel.state.values.action is Action.TAP_NOTIFICATION
                    && !hasPermissions.getValue(Permission.ACCESSIBILITY_SERVICE),
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(start = dimensionResource(R.dimen.padding_medium)),
                Arrangement.spacedBy(dimensionResource(R.dimen.padding_medium)),
            ) {
                ErrorText(R.string.accessibility_service_description)
                Button(
                    { context.request(Permission.ACCESSIBILITY_SERVICE) },
                    Modifier.align(Alignment.CenterHorizontally),
                    colors = ButtonDefaults.buttonColors(contentColor = MaterialTheme.colorScheme.onPrimaryContainer),
                ) {
                    Text(
                        stringResource(R.string.enable_service),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Normal,
                    )
                }
            }
        }
        AnimatedVisibility(it is Action.TAP_BUTTON && viewModel.state.values.action is Action.TAP_BUTTON) {
            Column(
                Modifier.fillMaxWidth(),
                Arrangement.spacedBy(dimensionResource(R.dimen.padding_medium)),
            ) {
                OutlinedTextField(
                    (viewModel.state.values.action as? Action.TAP_BUTTON)?.buttonRegex ?: "",
                    { value ->
                        viewModel.updateForm(
                            viewModel.state.page,
                            viewModel.state.values.copy(action = Action.TAP_BUTTON(value)),
                        )
                    },
                    Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.button_pattern)) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                        unfocusedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                        disabledContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                    ),
                    singleLine = true,
                )
                if (viewModel.state.error == FormError.INVALID_BUTTON_REGEX) ErrorText(R.string.invalid_regex)
            }
        }
        AnimatedVisibility(it is Action.BATCH && viewModel.state.values.action is Action.BATCH) {
            IntegerInput(
                (viewModel.state.values.action as? Action.BATCH)?.batchLength ?: 3,
                1,
                12,
                R.string.batch_frequency,
            ) { value ->
                viewModel.updateForm(
                    viewModel.state.page,
                    viewModel.state.values.copy(action = Action.BATCH(value)),
                )
            }
        }
        AnimatedVisibility(it is Action.DEBOUNCE && viewModel.state.values.action is Action.DEBOUNCE) {
            Column(
                Modifier.fillMaxWidth(),
                Arrangement.spacedBy(dimensionResource(R.dimen.padding_medium)),
            ) {
                IntegerInput(
                    (viewModel.state.values.action as? Action.DEBOUNCE)?.cooldownLength ?: 2,
                    1,
                    30,
                    R.string.cooldown_length,
                ) { value ->
                    viewModel.updateForm(
                        viewModel.state.page,
                        viewModel.state.values.copy(action = Action.DEBOUNCE(value)),
                    )
                }
                Text(
                    stringResource(R.string.explain_debounce),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Normal,
                )
                if (viewModel.state.error == FormError.CANT_DEBOUNCE_ANY) ErrorText(R.string.cant_debounce_any)
            }
        }
        AnimatedVisibility(
            it is Action.ALERT
                    && viewModel.state.values.action is Action.ALERT
                    && !hasPermissions.getValue(Permission.POST_NOTIFICATIONS),
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(start = dimensionResource(R.dimen.padding_medium)),
                Arrangement.spacedBy(dimensionResource(R.dimen.padding_medium)),
            ) {
                ErrorText(R.string.alert_notifications_description)
                Button(
                    { context.request(Permission.POST_NOTIFICATIONS) },
                    Modifier.align(Alignment.CenterHorizontally),
                    colors = ButtonDefaults.buttonColors(contentColor = MaterialTheme.colorScheme.onPrimaryContainer),
                ) {
                    Text(
                        stringResource(R.string.grant_permission),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Normal,
                    )
                }
            }
        }
        AnimatedVisibility(it is Action.DISTURB && viewModel.state.values.action is Action.DISTURB) {
            Column(
                Modifier.fillMaxWidth(),
                Arrangement.spacedBy(dimensionResource(R.dimen.padding_medium)),
            ) {
                IntegerInput(
                    (viewModel.state.values.action as? Action.DISTURB)?.pauseLength ?: 5,
                    1,
                    30,
                    R.string.pause_length,
                ) { value ->
                    viewModel.updateForm(
                        viewModel.state.page,
                        viewModel.state.values.copy(action = Action.DISTURB(value)),
                    )
                }
                if (!hasPermissions.getValue(Permission.NOTIFICATION_POLICY)) {
                    ErrorText(R.string.notification_policy_permission_description)
                    Button(
                        {
                            context.request(Permission.NOTIFICATION_POLICY)
                        },
                        Modifier.align(Alignment.CenterHorizontally),
                        colors = ButtonDefaults.buttonColors(contentColor = MaterialTheme.colorScheme.onPrimaryContainer),
                    ) {
                        Text(
                            stringResource(R.string.grant_permission),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Normal,
                        )
                    }
                }
            }
        }
        AnimatedVisibility(it is Action.DISMISS_STALE && viewModel.state.values.action is Action.DISMISS_STALE) {
            Column(
                Modifier.fillMaxWidth(),
                Arrangement.spacedBy(dimensionResource(R.dimen.padding_medium)),
            ) {
                IntegerInput(
                    (viewModel.state.values.action as? Action.DISMISS_STALE)?.retentionLength ?: 15,
                    5,
                    300,
                    R.string.retention_length,
                    5,
                    30,
                    3,
                ) { value ->
                    viewModel.updateForm(
                        viewModel.state.page,
                        viewModel.state.values.copy(action = Action.DISMISS_STALE(value)),
                    )
                }
                if (!hasPermissions.getValue(Permission.SCHEDULE_EXACT_ALARM)) {
                    ErrorText(R.string.exact_alarm_permission_description)
                    Button(
                        { context.request(Permission.SCHEDULE_EXACT_ALARM) },
                        Modifier.align(Alignment.CenterHorizontally),
                        colors = ButtonDefaults.buttonColors(contentColor = MaterialTheme.colorScheme.onPrimaryContainer),
                    ) {
                        Text(
                            stringResource(R.string.disable_optimization),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Normal,
                        )
                    }
                }
            }
        }
    }
    HorizontalDivider()
    Text(
        stringResource(R.string.display_options),
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Normal,
    )
    Row(
        Modifier
            .padding(horizontal = dimensionResource(R.dimen.padding_small))
            .toggleable(viewModel.state.values.historyEnabled) {
                viewModel.updateForm(
                    FormPage.ACTION,
                    viewModel.state.values.copy(historyEnabled = it),
                )
            },
        Arrangement.spacedBy(dimensionResource(R.dimen.padding_small)),
        Alignment.CenterVertically,
    ) {
        Checkbox(viewModel.state.values.historyEnabled, null)
        Text(
            stringResource(R.string.describe_history_screen),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Normal,
        )
    }
    Row(
        Modifier
            .padding(horizontal = dimensionResource(R.dimen.padding_small))
            .toggleable(viewModel.state.values.widgetEnabled) {
                viewModel.updateForm(
                    FormPage.ACTION,
                    viewModel.state.values.copy(widgetEnabled = it),
                )
            },
        Arrangement.spacedBy(dimensionResource(R.dimen.padding_small)),
        Alignment.CenterVertically,
    ) {
        Checkbox(viewModel.state.values.widgetEnabled, null)
        Text(
            stringResource(R.string.describe_widget),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Normal,
        )
    }
    if (viewModel.state.values.widgetEnabled) {
        if (!hasPermissions.getValue(Permission.ACCESSIBILITY_SERVICE)) {
            Button(
                { context.request(Permission.ACCESSIBILITY_SERVICE) },
                Modifier.align(Alignment.CenterHorizontally),
                colors = ButtonDefaults.buttonColors(contentColor = MaterialTheme.colorScheme.onPrimaryContainer),
            ) {
                Text(
                    stringResource(R.string.make_tappable),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Normal,
                )
            }
        }
        if (!hasPinnedWidget) {
            Button(
                context::addWidgetToHomeScreen,
                Modifier.align(Alignment.CenterHorizontally),
                colors = ButtonDefaults.buttonColors(contentColor = MaterialTheme.colorScheme.onPrimaryContainer),
            ) {
                Text(
                    stringResource(R.string.prompt_widget),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Normal,
                )
            }
        }
    }
}

@Composable
private fun SchedulePage(viewModel: UpsertFilterViewModel) {
    val context = LocalContext.current
    val startTimePicker = TimePickerDialog(
        context,
        { _, hour: Int, minute: Int ->
            viewModel.updateForm(
                viewModel.state.page,
                viewModel.state.values.copy(schedule = viewModel.state.values.schedule.copy(start = hour * 60 + minute)),
            )
        },
        viewModel.state.values.schedule.start / 60,
        viewModel.state.values.schedule.start % 60,
        false,
    )
    val endTimePicker = TimePickerDialog(
        context,
        { _, hour: Int, minute: Int ->
            viewModel.updateForm(
                viewModel.state.page,
                viewModel.state.values.copy(schedule = viewModel.state.values.schedule.copy(end = hour * 60 + minute)),
            )
        },
        viewModel.state.values.schedule.end / 60,
        viewModel.state.values.schedule.end % 60,
        false,
    )

    Text(
        stringResource(R.string.schedule_page_title),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Normal,
    )
    Row(
        Modifier.fillMaxWidth(),
        Arrangement.Start,
        Alignment.CenterVertically,
    ) {
        Text(
            stringResource(R.string.run_from),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Normal,
        )
        Text(
            String.format(
                Locale.getDefault(),
                "%02d:%02d",
                viewModel.state.values.schedule.start / 60,
                viewModel.state.values.schedule.start % 60,
            ),
            Modifier.clickable { startTimePicker.show() },
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Normal,
            textDecoration = TextDecoration.Underline,
        )
        Text(
            stringResource(R.string.to),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Normal,
        )
        Text(
            String.format(
                Locale.getDefault(),
                "%02d:%02d",
                viewModel.state.values.schedule.end / 60,
                viewModel.state.values.schedule.end % 60,
            ),
            Modifier.clickable { endTimePicker.show() },
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Normal,
            textDecoration = TextDecoration.Underline,
        )
        Text(
            stringResource(R.string.on),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Normal,
        )
    }
    if (viewModel.state.error == FormError.INVALID_TIME_RANGE) ErrorText(R.string.invalid_time_range)
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = dimensionResource(R.dimen.padding_medium)),
        Arrangement.SpaceBetween,
    ) {
        stringArrayResource(R.array.days_initials).forEachIndexed { i, day ->
            val index = i + 1
            val selected = viewModel.state.values.schedule.days.contains(index)

            Box(
                Modifier
                    .background(
                        if (selected) MaterialTheme.colorScheme.primary
                        else Color.Transparent,
                        CircleShape,
                    )
                    .padding(dimensionResource(R.dimen.padding_small))
                    .selectable(selected) {
                        val newDays =
                            viewModel.state.values.schedule.days.toMutableSet()
                        if (newDays.contains(index)) newDays.remove(index)
                        else newDays.add(index)
                        viewModel.updateForm(
                            viewModel.state.page,
                            viewModel.state.values.copy(
                                schedule = viewModel.state.values.schedule.copy(days = newDays),
                            ),
                        )
                    },
            ) {
                Text(
                    day,
                    color = if (selected) MaterialTheme.colorScheme.onSurface
                    else MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
    }
    if (viewModel.state.error == FormError.BLANK_FIELDS) ErrorText(R.string.empty_active_days)
    if (viewModel.state.values.action == Action.DELAY)
        Text(
            stringResource(R.string.delay_action_reminder),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Normal,
        )
}
