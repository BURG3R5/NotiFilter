package co.adityarajput.notifilter.views.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import co.adityarajput.notifilter.R
import co.adityarajput.notifilter.data.filter.Filter
import co.adityarajput.notifilter.viewmodels.*
import co.adityarajput.notifilter.views.Theme
import co.adityarajput.notifilter.views.components.AppBar
import co.adityarajput.notifilter.views.icons.History
import kotlinx.coroutines.launch
import java.lang.Integer.min

@Composable
fun FiltersScreen(
    goToNotificationsScreen: () -> Unit,
    goToAboutScreen: () -> Unit,
    viewModel: FiltersViewModel = viewModel(factory = Provider.Factory),
) {
    val filtersState = viewModel.filtersState.collectAsState()

    val context = LocalContext.current
    val packages = remember { viewModel.getPackages(context.packageManager) }

    var filterToDelete by remember { mutableStateOf<Filter?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            AppBar(
                stringResource(R.string.app_name),
                false,
                goToAboutScreen,
                {
                    IconButton(goToNotificationsScreen) {
                        Icon(
                            History,
                            stringResource(R.string.block_history),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                { showAddDialog = true },
                Modifier.padding(dimensionResource(id = R.dimen.padding_small)),
                MaterialTheme.shapes.medium,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            ) {
                Icon(
                    Icons.Default.Add,
                    stringResource(R.string.add_filter),
                )
            }
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
                modifier = Modifier
                    .padding(dimensionResource(id = R.dimen.padding_small))
                    .fillMaxSize(),
                contentPadding = paddingValues
            ) {
                items(
                    filtersState.value.filters!!,
                    { it.id },
                ) { filter -> FilterCard(filter) { filterToDelete = it } }
            }
            if (filterToDelete != null)
                DeleteFilterDialog(
                    { viewModel.deleteFilter(filterToDelete!!) },
                    { filterToDelete = null },
                )
        }
        if (showAddDialog)
            AddFilterDialog(
                viewModel.formState,
                viewModel::onFormUpdate,
                viewModel::onFormSubmit,
                { showAddDialog = false },
                packages,
            )
    }
}

@Composable
private fun FilterCard(filter: Filter, onClick: (Filter) -> Unit) {
    Card(
        { onClick(filter) },
        Modifier
            .fillMaxWidth()
            .padding(dimensionResource(R.dimen.padding_small)),
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(dimensionResource(R.dimen.padding_large)),
            Arrangement.spacedBy(dimensionResource(R.dimen.padding_small))
        ) {
            Text(
                "/${filter.queryPattern}/",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                AnnotatedString.fromHtml(
                    stringResource(
                        R.string.display_hits_and_app,
                        filter.hits,
                        filter.packageName
                    )
                ),
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddFilterDialog(
    formState: FormState,
    onUpdate: (InputValues) -> Unit,
    onSubmit: suspend () -> Unit,
    hideDialog: () -> Unit,
    packages: List<Pair<String, String>>,
) {
    val coroutineScope = rememberCoroutineScope()

    var expanded by remember { mutableStateOf(false) }
    var suggestions by remember { mutableStateOf(listOf<Pair<String, String>>()) }

    AlertDialog(
        hideDialog,
        title = { Text(stringResource(R.string.add_filter)) },
        text = {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(dimensionResource(R.dimen.padding_small)),
                Arrangement.spacedBy(dimensionResource(R.dimen.padding_medium))
            ) {
                ExposedDropdownMenuBox(expanded, { expanded = it }) {
                    OutlinedTextField(
                        formState.inputValues.packageName,
                        { input ->
                            onUpdate(formState.inputValues.copy(packageName = input))
                            suggestions =
                                packages.filter {
                                    it.toString().contains(input, ignoreCase = true)
                                }
                            expanded = suggestions.isNotEmpty()
                        },
                        Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                        label = { Text(stringResource(R.string.package_name)) },
                        supportingText = { Text(stringResource(R.string.search_apps)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                            unfocusedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                            disabledContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                        ),
                        singleLine = true
                    )
                    ExposedDropdownMenu(expanded, { expanded = false }) {
                        suggestions.subList(0, min(5, suggestions.size)).forEach {
                            DropdownMenuItem(
                                { Text(it.second) },
                                {
                                    onUpdate(formState.inputValues.copy(packageName = it.first))
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                OutlinedTextField(
                    formState.inputValues.queryPattern,
                    { onUpdate(formState.inputValues.copy(queryPattern = it)) },
                    Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.query_pattern)) },
                    supportingText = {
                        Text(
                            AnnotatedString.fromHtml(
                                stringResource(R.string.regexr_link),
                                TextLinkStyles(
                                    SpanStyle(
                                        MaterialTheme.colorScheme.primary,
                                        textDecoration = TextDecoration.Underline,
                                    )
                                )
                            )
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                        unfocusedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                        disabledContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                    ),
                    singleLine = true
                )
                if (formState.error == FormError.INVALID_REGEX) {
                    Text(
                        stringResource(R.string.invalid_regex),
                        Modifier.padding(start = dimensionResource(R.dimen.padding_medium)),
                        color = MaterialTheme.colorScheme.tertiary,
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                {
                    coroutineScope.launch {
                        onSubmit()
                        hideDialog()
                    }
                },
                enabled = formState.error == null,
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.secondary),
            ) {
                Text(stringResource(R.string.add))
            }
        },
        dismissButton = {
            TextButton(hideDialog) {
                Text(stringResource(R.string.cancel), fontWeight = FontWeight.Normal)
            }
        },
    )
}

@Composable
private fun DeleteFilterDialog(
    onConfirm: () -> Unit,
    hideDialog: () -> Unit,
) {
    AlertDialog(
        hideDialog,
        title = { Text(stringResource(R.string.delete_filter)) },
        text = { Text(stringResource(R.string.delete_confirmation)) },
        confirmButton = {
            TextButton(
                {
                    onConfirm()
                    hideDialog()
                },
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.tertiary),
            ) { Text(stringResource(R.string.delete)) }
        },
        dismissButton = {
            TextButton(hideDialog) {
                Text(stringResource(R.string.cancel), fontWeight = FontWeight.Normal)
            }
        }
    )
}

@Preview
@Composable
private fun FilterCardPreview() =
    Theme { FilterCard(Filter(1, "com.example.app", ".*", 3), {}) }

@Preview
@Composable
private fun AddFilterDialogPreview() =
    Theme { AddFilterDialog(FormState(), {}, {}, {}, listOf()) }

@Preview
@Composable
private fun DeleteFilterDialogPreview() =
    Theme { DeleteFilterDialog({}, {}) }
