package co.adityarajput.notifilter.views.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

@Composable
fun ErrorText(text: Int) = Text(
    stringResource(text),
    color = MaterialTheme.colorScheme.tertiary,
    style = MaterialTheme.typography.labelLarge,
)
