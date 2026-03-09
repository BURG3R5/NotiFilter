package co.adityarajput.notifilter.views.screens

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import co.adityarajput.notifilter.R
import kotlin.math.max
import kotlin.math.min

@Composable
fun IntegerInput(
    value: Int,
    minValue: Int,
    maxValue: Int,
    stringResource: Int,
    delta: Int = 1,
    largeDelta: Int = delta * 5,
    padLength: Int = 2,
    onValueChange: (Int) -> Unit,
) {
    Row(
        Modifier.fillMaxWidth(),
        Arrangement.Center,
        Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .padding(dimensionResource(R.dimen.padding_small))
                .combinedClickable(
                    onClick = { onValueChange(max(minValue, value - delta)) },
                    onLongClick = { onValueChange(max(minValue, value - largeDelta)) },
                ),
        ) {
            Icon(
                painterResource(R.drawable.remove),
                contentDescription = stringResource(R.string.alttext_subtract),
            )
        }
        Text(
            stringResource(
                stringResource,
                value.toString().padStart(padLength, '0'),
            ),
        )
        Box(
            Modifier
                .padding(dimensionResource(R.dimen.padding_small))
                .combinedClickable(
                    onClick = { onValueChange(min(maxValue, value + delta)) },
                    onLongClick = { onValueChange(min(maxValue, value + largeDelta)) },
                ),
        ) {
            Icon(
                painterResource(R.drawable.add),
                contentDescription = stringResource(R.string.alttext_add),
            )
        }
    }
}
