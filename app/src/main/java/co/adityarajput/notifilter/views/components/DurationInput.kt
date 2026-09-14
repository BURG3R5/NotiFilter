package co.adityarajput.notifilter.views.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import co.adityarajput.notifilter.R
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DurationInput(
    value: Long,
    onValueChange: (Long) -> Unit,
    modifier: Modifier = Modifier,
    label: String,
    units: List<TimeUnit>,
) {
    var unit by remember(units) { mutableStateOf(units.first()) }
    var dropdownExpanded by remember { mutableStateOf(false) }

    val displayValue = (value / unit.inMillis).toInt()

    OutlinedTextField(
        displayValue.toString(),
        { onValueChange((it.toIntOrNull() ?: 0) * unit.inMillis) },
        modifier,
        label = { Text(label) },
        trailingIcon = {
            ExposedDropdownMenuBox(dropdownExpanded, { dropdownExpanded = !dropdownExpanded }) {
                Row(
                    Modifier.clickable { dropdownExpanded = true },
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        unit.text(displayValue),
                        Modifier.padding(start = dimensionResource(R.dimen.padding_medium)),
                    )

                    IconButton(
                        onClick = { dropdownExpanded = true },
                    ) {
                        Icon(
                            painterResource(R.drawable.arrow_drop_down),
                            stringResource(R.string.arrow_drop_down),
                        )
                    }
                }

                ExposedDropdownMenu(dropdownExpanded, { dropdownExpanded = false }) {
                    units.forEach { candidate ->
                        DropdownMenuItem(
                            { Text(candidate.text(displayValue)) },
                            {
                                unit = candidate
                                onValueChange(displayValue * candidate.inMillis)
                                dropdownExpanded = false
                            },
                        )
                    }
                }
            }
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
            unfocusedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
            disabledContainerColor = MaterialTheme.colorScheme.secondaryContainer,
        ),
    )
}

private val TimeUnit.inMillis: Long
    get() = when (this) {
        TimeUnit.SECONDS -> 1_000
        TimeUnit.MINUTES -> 60_000
        TimeUnit.HOURS -> 3_600_000
        else -> throw IllegalArgumentException("Unsupported TimeUnit: $this")
    }

@Composable
private fun TimeUnit.text(value: Int) =
    pluralStringResource(
        when (this) {
            TimeUnit.SECONDS -> R.plurals.second
            TimeUnit.MINUTES -> R.plurals.minute
            TimeUnit.HOURS -> R.plurals.hour
            else -> throw IllegalArgumentException("Unsupported TimeUnit: $this")
        },
        value, 0,
    ).substringAfter(' ')
