package co.adityarajput.notifilter.views.screens.filters

import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.text.style.TextDecoration
import co.adityarajput.notifilter.R
import co.adityarajput.notifilter.data.filter.Action
import co.adityarajput.notifilter.viewmodels.FiltersViewModel
import co.adityarajput.notifilter.viewmodels.FormError
import co.adityarajput.notifilter.viewmodels.FormPage
import co.adityarajput.notifilter.viewmodels.FormState
import kotlinx.coroutines.launch
import java.lang.Integer.min
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFilterDialog(viewModel: FiltersViewModel) {
    val coroutineScope = rememberCoroutineScope()

    AlertDialog(
        { viewModel.showAddDialog = false },
        title = { Text(stringResource(R.string.add_filter)) },
        text = { Form(viewModel) },
        confirmButton = {
            TextButton(
                {
                    if (!viewModel.formState.page.isFinalPage()) {
                        viewModel.updateForm(
                            when (viewModel.formState.page) {
                                FormPage.PACKAGE -> FormPage.PATTERN
                                FormPage.PATTERN -> FormPage.ACTION
                                else -> FormPage.TIME
                            },
                            viewModel.formState.values,
                        )
                    } else {
                        coroutineScope.launch {
                            viewModel.submitForm()
                            viewModel.showAddDialog = false
                        }
                    }
                },
                enabled = viewModel.formState.error == null,
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.secondary),
            ) {
                Text(
                    if (viewModel.formState.page.isFinalPage()) stringResource(R.string.add)
                    else stringResource(R.string.next),
                )
            }
        },
        dismissButton = {
            TextButton(
                {
                    viewModel.formState = FormState()
                    viewModel.showAddDialog = false
                },
            ) {
                Text(stringResource(R.string.cancel), fontWeight = FontWeight.Normal)
            }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Form(viewModel: FiltersViewModel) {
    val context = LocalContext.current
    val formState = viewModel.formState

    var showSystemPackages by remember { mutableStateOf(false) }

    var dropdownExpanded by remember { mutableStateOf(false) }
    var suggestions by remember { mutableStateOf(listOf<Pair<String, String>>()) }

    Column(
        Modifier
            .fillMaxWidth()
            .padding(dimensionResource(R.dimen.padding_small)),
        Arrangement.spacedBy(dimensionResource(R.dimen.padding_medium)),
    ) {
        when (formState.page) {
            FormPage.PACKAGE -> {
                ExposedDropdownMenuBox(dropdownExpanded, { dropdownExpanded = it }) {
                    OutlinedTextField(
                        formState.values.packageName,
                        { input ->
                            viewModel.updateForm(
                                formState.page,
                                formState.values.copy(packageName = input),
                            )
                            suggestions =
                                (if (showSystemPackages) viewModel.allPackages else viewModel.visibleApps).filter {
                                    it.toString().contains(input, ignoreCase = true)
                                }
                            dropdownExpanded = suggestions.isNotEmpty()
                        },
                        Modifier
                            .fillMaxWidth()
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable),
                        label = { Text(stringResource(R.string.package_name)) },
                        supportingText = { Text(stringResource(R.string.search_apps)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                            unfocusedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                            disabledContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                        ),
                        singleLine = true,
                    )
                    ExposedDropdownMenu(dropdownExpanded, { dropdownExpanded = false }) {
                        suggestions.subList(0, min(5, suggestions.size)).forEach {
                            DropdownMenuItem(
                                { Text(it.second) },
                                {
                                    viewModel.updateForm(
                                        formState.page,
                                        formState.values.copy(packageName = it.first),
                                    )
                                    dropdownExpanded = false
                                },
                            )
                        }
                    }
                }
                Row(
                    Modifier.fillMaxWidth(),
                    Arrangement.Start,
                    if (!showSystemPackages) Alignment.CenterVertically else Alignment.Top,
                ) {
                    Checkbox(showSystemPackages, { showSystemPackages = it })
                    Column {
                        Text(
                            stringResource(R.string.show_system_packages),
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Normal),
                        )
                        if (showSystemPackages)
                            Text(
                                stringResource(R.string.system_packages_warning),
                                Modifier.padding(top = dimensionResource(R.dimen.padding_small)),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Normal),
                            )
                    }
                }
            }

            FormPage.PATTERN -> {
                OutlinedTextField(
                    formState.values.queryPattern,
                    {
                        viewModel.updateForm(
                            formState.page,
                            formState.values.copy(queryPattern = it),
                        )
                    },
                    Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.notification_pattern)) },
                    supportingText = {
                        Text(
                            AnnotatedString.fromHtml(
                                stringResource(R.string.regexr_link),
                                TextLinkStyles(
                                    SpanStyle(
                                        MaterialTheme.colorScheme.primary,
                                        textDecoration = TextDecoration.Underline,
                                    ),
                                ),
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
                if (formState.error == FormError.INVALID_NOTIFICATION_REGEX) {
                    Text(
                        stringResource(R.string.invalid_regex),
                        Modifier.padding(start = dimensionResource(R.dimen.padding_medium)),
                        MaterialTheme.colorScheme.tertiary,
                    )
                }
            }

            FormPage.ACTION -> {
                Text(stringResource(R.string.action))
                Action.entries.forEach {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .selectable((it == viewModel.formState.values.action)) {
                                viewModel.updateForm(
                                    formState.page,
                                    formState.values.copy(action = it),
                                )
                            },
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(
                            (it == viewModel.formState.values.action),
                            null,
                            Modifier.padding(horizontal = dimensionResource(R.dimen.padding_small)),
                        )
                        Text(
                            stringResource(it.descriptionString),
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
                if (viewModel.formState.values.action == Action.TAP) {
                    OutlinedTextField(
                        formState.values.buttonPattern,
                        {
                            viewModel.updateForm(
                                formState.page,
                                formState.values.copy(buttonPattern = it),
                            )
                        },
                        Modifier.fillMaxWidth(),
                        label = { Text(stringResource(R.string.button_pattern)) },
                        supportingText = {
                            Text(
                                AnnotatedString.fromHtml(
                                    stringResource(R.string.regexr_link),
                                    TextLinkStyles(
                                        SpanStyle(
                                            MaterialTheme.colorScheme.primary,
                                            textDecoration = TextDecoration.Underline,
                                        ),
                                    ),
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
                    if (formState.error == FormError.INVALID_BUTTON_REGEX) {
                        Text(
                            stringResource(R.string.invalid_regex),
                            Modifier.padding(start = dimensionResource(R.dimen.padding_medium)),
                            MaterialTheme.colorScheme.tertiary,
                        )
                    }
                }
            }

            FormPage.TIME -> {
                Text(stringResource(R.string.schedule))
                Row(
                    Modifier.fillMaxWidth(),
                    Arrangement.SpaceBetween,
                ) {
                    daysOfTheWeek.forEach { day ->
                        val selected = formState.values.activeDays.contains(day.first)
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
                                        viewModel.formState.values.activeDays.toMutableSet()
                                    if (newDays.contains(day.first)) newDays.remove(day.first)
                                    else newDays.add(day.first)
                                    viewModel.updateForm(
                                        formState.page,
                                        formState.values.copy(activeDays = newDays),
                                    )
                                },
                        ) {
                            Text(
                                day.second,
                                color = if (selected) MaterialTheme.colorScheme.onSurface
                                else MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.labelLarge,
                            )
                        }
                    }
                }
                val startTimePicker = TimePickerDialog(
                    context,
                    { _, hour: Int, minute: Int ->
                        val newStart = hour * 60 + minute
                        viewModel.updateForm(
                            formState.page,
                            formState.values.copy(activeTime = newStart to formState.values.activeTime.second),
                        )
                    },
                    formState.values.activeTime.first / 60,
                    formState.values.activeTime.first % 60,
                    false,
                )
                val endTimePicker = TimePickerDialog(
                    context,
                    { _, hour: Int, minute: Int ->
                        val newEnd = hour * 60 + minute
                        viewModel.updateForm(
                            formState.page,
                            formState.values.copy(activeTime = formState.values.activeTime.first to newEnd),
                        )
                    },
                    formState.values.activeTime.second / 60,
                    formState.values.activeTime.second % 60,
                    false,
                )
                Row(
                    Modifier.fillMaxWidth(),
                    Arrangement.Center,
                    Alignment.CenterVertically,
                ) {
                    Text(
                        String.format(
                            Locale.getDefault(),
                            "%02d:%02d",
                            formState.values.activeTime.first / 60,
                            formState.values.activeTime.first % 60,
                        ),
                        Modifier.clickable { startTimePicker.show() },
                    )
                    Text(
                        "-",
                        Modifier.padding(horizontal = dimensionResource(R.dimen.padding_medium)),
                    )
                    Text(
                        String.format(
                            Locale.getDefault(),
                            "%02d:%02d",
                            formState.values.activeTime.second / 60,
                            formState.values.activeTime.second % 60,
                        ),
                        Modifier.clickable { endTimePicker.show() },
                    )
                }
                if (formState.error == FormError.BLANK_FIELDS) {
                    Text(
                        stringResource(R.string.empty_active_days),
                        Modifier.padding(start = dimensionResource(R.dimen.padding_medium)),
                        MaterialTheme.colorScheme.tertiary,
                    )
                }
                if (formState.error == FormError.INVALID_TIME_RANGE) {
                    Text(
                        stringResource(R.string.invalid_time_range),
                        Modifier.padding(start = dimensionResource(R.dimen.padding_medium)),
                        MaterialTheme.colorScheme.tertiary,
                    )
                }
            }
        }
    }
}

private val daysOfTheWeek =
    listOf(1 to "M", 2 to "T", 3 to "W", 4 to "T", 5 to "F", 6 to "S", 7 to "S")
