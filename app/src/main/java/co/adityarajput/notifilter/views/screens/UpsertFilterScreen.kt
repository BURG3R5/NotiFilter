package co.adityarajput.notifilter.views.screens

import android.app.TimePickerDialog
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
import androidx.compose.ui.res.pluralStringResource
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
import co.adityarajput.notifilter.data.models.Action
import co.adityarajput.notifilter.data.models.App
import co.adityarajput.notifilter.data.models.RegexTarget
import co.adityarajput.notifilter.utils.filterFirst
import co.adityarajput.notifilter.utils.getFirst
import co.adityarajput.notifilter.viewmodels.FormError
import co.adityarajput.notifilter.viewmodels.FormPage
import co.adityarajput.notifilter.viewmodels.Provider
import co.adityarajput.notifilter.viewmodels.UpsertFilterViewModel
import co.adityarajput.notifilter.views.components.AppBar
import co.adityarajput.notifilter.views.components.ErrorText
import co.adityarajput.notifilter.views.components.Tile
import co.adityarajput.notifilter.views.components.WarningText
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
                    Modifier.fillMaxWidth(),
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
                        else if (viewModel.state.page.isFinalPage()) stringResource(R.string.add)
                        else stringResource(R.string.next),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }
        }
    }
}

@Composable
private fun ZapperPage(viewModel: UpsertFilterViewModel) {
    if (viewModel.activeNotifications.isEmpty()) {
        Box(
            Modifier
                .fillMaxWidth()
                .fillMaxHeight(),
            Alignment.Center,
        ) {
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
                val appName =
                    viewModel.allPackages.find { app -> app.packageName == it.origin }?.name
                        ?: it.origin

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
    var searchString by remember { mutableStateOf("") }
    var visibleItemsCount by remember { mutableIntStateOf(10) }
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
    Row(
        Modifier
            .padding(vertical = dimensionResource(R.dimen.padding_medium))
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
        supportingText = {
            if (viewModel.state.values.notification != null)
                Text(
                    stringResource(
                        R.string.pattern_supporting,
                        buildString {
                            if (viewModel.state.values.regexTarget == RegexTarget.CONTENT) {
                                append(viewModel.state.values.notification!!.content.getFirst(20))
                            } else {
                                append(viewModel.state.values.notification!!.title.getFirst(20))
                                if (viewModel.state.values.regexTarget == RegexTarget.OR) {
                                    append("' ")
                                    append(stringResource(R.string.or))
                                    append(" '")
                                    append(
                                        viewModel.state.values.notification!!.content.getFirst(
                                            20,
                                        ),
                                    )
                                }
                            }
                        },
                    ),
                )
        },
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
            supportingText = {
                if (viewModel.state.values.notification != null)
                    Text(
                        stringResource(
                            R.string.pattern_supporting,
                            viewModel.state.values.notification!!.content.getFirst(20),
                        ),
                    )
            },
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

@Composable
private fun ActionPage(viewModel: UpsertFilterViewModel) {
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
            Column(
                Modifier.fillMaxWidth(),
                Arrangement.spacedBy(dimensionResource(R.dimen.padding_medium)),
            ) {
                Slider(
                    (viewModel.state.values.action as? Action.BATCH)?.batchLength?.toFloat() ?: 3F,
                    { value ->
                        viewModel.updateForm(
                            viewModel.state.page,
                            viewModel.state.values.copy(action = Action.BATCH(value.toInt())),
                        )
                    },
                    Modifier.fillMaxWidth(),
                    valueRange = 1F..12F,
                    steps = 10,
                )
                Text(
                    stringResource(
                        R.string.batch_frequency,
                        pluralStringResource(
                            R.plurals.hour,
                            (viewModel.state.values.action as? Action.BATCH)?.batchLength ?: 3,
                            (viewModel.state.values.action as? Action.BATCH)?.batchLength ?: 3,
                        ),
                    ),
                    Modifier.padding(start = dimensionResource(R.dimen.padding_medium)),
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
