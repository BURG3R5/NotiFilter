package co.adityarajput.notifilter.views.screens.filters

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import co.adityarajput.notifilter.utils.getLast
import co.adityarajput.notifilter.utils.withUnit
import co.adityarajput.notifilter.viewmodels.FiltersViewModel
import co.adityarajput.notifilter.viewmodels.Provider
import co.adityarajput.notifilter.views.components.AppBar
import co.adityarajput.notifilter.views.components.Tile
import co.adityarajput.notifilter.views.icons.History

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
                Modifier.padding(dimensionResource(R.dimen.padding_small)),
                MaterialTheme.shapes.medium,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            ) {
                Icon(
                    Icons.Default.Add,
                    stringResource(R.string.add_filter),
                    tint = MaterialTheme.colorScheme.onSurface,
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
                Modifier
                    .padding(dimensionResource(R.dimen.padding_small))
                    .fillMaxSize(),
                contentPadding = paddingValues,
            ) {
                items(filtersState.value.filters!!, { it.id }) {
                    Tile(
                        "/${it.queryPattern}/",
                        stringResource(it.action.displayString, it.buttonPattern ?: ' '),
                        it.packageName.getLast(27),
                        if (it.enabled) it.hits.withUnit(stringResource(R.string.hit))
                        else stringResource(R.string.disabled),
                        { viewModel.selectedFilter = it },
                        true,
                    )
                }
            }
        }
        if (viewModel.showAddDialog) AddFilterDialog(viewModel)
        if (viewModel.selectedFilter != null) EditFilterDialog(viewModel)
    }
}
