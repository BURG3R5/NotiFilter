package co.adityarajput.notifilter.views.screens.filters

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import co.adityarajput.notifilter.R
import co.adityarajput.notifilter.utils.getToggleString
import co.adityarajput.notifilter.viewmodels.DialogState
import co.adityarajput.notifilter.viewmodels.FiltersViewModel

@Composable
fun EditFilterDialog(viewModel: FiltersViewModel) {
    val hideDialog = { viewModel.dialogState = null }
    val dialogState = viewModel.dialogState!!
    val filter = viewModel.selectedFilter!!

    AlertDialog(
        hideDialog,
        title = {
            Text(
                when (dialogState) {
                    DialogState.TOGGLE_HISTORY -> stringResource(
                        R.string.toggle_history,
                        filter.historyEnabled.getToggleString(),
                    )

                    DialogState.TOGGLE_FILTER -> stringResource(
                        R.string.toggle_filter,
                        filter.enabled.getToggleString(),
                    )

                    DialogState.DELETE -> stringResource(R.string.delete_filter)
                },
            )
        },
        text = {
            Text(
                when (dialogState) {
                    DialogState.TOGGLE_HISTORY -> stringResource(
                        if (filter.historyEnabled) R.string.disable_history_confirmation else R.string.enable_history_confirmation,
                    )

                    DialogState.TOGGLE_FILTER -> stringResource(
                        R.string.toggle_filter_confirmation,
                        filter.enabled.getToggleString(),
                    )

                    DialogState.DELETE -> stringResource(
                        R.string.delete_confirmation,
                        if (filter.enabled) stringResource(R.string.disable_suggestion) else "",
                    )
                },
            )
        },
        confirmButton = {
            Row {
                TextButton(
                    {
                        when (dialogState) {
                            DialogState.TOGGLE_HISTORY -> viewModel.toggleHistory()
                            DialogState.TOGGLE_FILTER -> viewModel.toggleFilter()
                            DialogState.DELETE -> viewModel.deleteFilter()
                        }
                        hideDialog()
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = if (dialogState == DialogState.DELETE) MaterialTheme.colorScheme.tertiary
                        else Color.Unspecified,
                    ),
                ) {
                    Text(
                        when (dialogState) {
                            DialogState.TOGGLE_HISTORY -> filter.historyEnabled.getToggleString()
                            DialogState.TOGGLE_FILTER -> filter.enabled.getToggleString()
                            DialogState.DELETE -> stringResource(R.string.delete)
                        },
                    )
                }
            }
        },
        dismissButton = {
            TextButton(hideDialog) {
                Text(stringResource(R.string.cancel), fontWeight = FontWeight.Normal)
            }
        },
    )
}
