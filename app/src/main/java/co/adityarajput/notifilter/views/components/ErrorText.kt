package co.adityarajput.notifilter.views.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import co.adityarajput.notifilter.views.Orange

@Composable
fun ErrorText(text: Int) = Text(
    stringResource(text),
    color = MaterialTheme.colorScheme.tertiary,
    style = MaterialTheme.typography.labelLarge,
)

@Composable
fun WarningText(text: Int) = Text(
    stringResource(text),
    color = Orange,
    style = MaterialTheme.typography.labelLarge,
)
