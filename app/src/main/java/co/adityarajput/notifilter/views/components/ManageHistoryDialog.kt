package co.adityarajput.notifilter.views.components

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import co.adityarajput.notifilter.R
import co.adityarajput.notifilter.viewmodels.NotificationsViewModel

@Composable
fun ManageHistoryDialog(viewModel: NotificationsViewModel) {
    val hideDialog = { viewModel.dialogState = null }

    AlertDialog(
        hideDialog,
        title = { Text(stringResource(R.string.clear_history)) },
        text = { Text(stringResource(R.string.clear_history_confirmation)) },
        confirmButton = {
            TextButton(
                { viewModel.clearHistory(); hideDialog() },
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.tertiary),
            ) { Text(stringResource(R.string.clear_history)) }
        },
        dismissButton = {
            TextButton(hideDialog) {
                Text(stringResource(R.string.cancel), fontWeight = FontWeight.Normal)
            }
        },
    )
}
