package co.adityarajput.notifilter.views.screens.filters

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import co.adityarajput.notifilter.R
import co.adityarajput.notifilter.viewmodels.FiltersViewModel

@Composable
fun EditFilterDialog(viewModel: FiltersViewModel) {
    val hideDialog = { viewModel.selectedFilter = null }

    AlertDialog(
        hideDialog,
        title = { Text(stringResource(R.string.edit_filter)) },
        text = {
            Text(
                stringResource(
                    R.string.delete_warning,
                    if (viewModel.selectedFilter!!.enabled) stringResource(R.string.disable_suggestion) else "",
                ),
            )
        },
        confirmButton = {
            Row {
                TextButton(
                    {
                        viewModel.toggleFilter()
                        hideDialog()
                    },
                ) {
                    Text(stringResource(if (viewModel.selectedFilter!!.enabled) R.string.disable else R.string.enable))
                }
                TextButton(
                    {
                        viewModel.deleteFilter()
                        hideDialog()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.tertiary),
                ) { Text(stringResource(R.string.delete)) }
            }
        },
        dismissButton = {
            TextButton(hideDialog) {
                Text(stringResource(R.string.cancel), fontWeight = FontWeight.Normal)
            }
        },
    )
}
