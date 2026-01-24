package co.adityarajput.notifilter.data.models

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import co.adityarajput.notifilter.R
import kotlinx.serialization.Serializable

@Serializable
sealed class Action {
    @Serializable
    data object DISMISS : Action()

    @Serializable
    data class TAP(val buttonRegex: String) : Action()

    @Serializable
    data class BATCH(val batchLength: Int) : Action()

    @Serializable
    data object DELAY : Action()

    @Composable
    fun verb() = when (this) {
        is DISMISS -> stringResource(R.string.dismiss_short)
        is TAP -> stringResource(R.string.tap_short, buttonRegex)
        is DELAY -> stringResource(R.string.delay_short)
        is BATCH -> stringResource(
            R.string.batch_short,
            pluralStringResource(R.plurals.hour, batchLength, batchLength),
        )
    }

    @Composable
    fun description() = stringResource(
        when (this) {
            is DISMISS -> R.string.dismiss_long
            is TAP -> R.string.tap_long
            is BATCH -> R.string.batch_long
            is DELAY -> R.string.delay_long
        },
    )

    fun isOfType(it: Action) = this::class == it::class

    companion object {
        val entries = listOf(DISMISS, TAP(""), BATCH(3), DELAY)

        fun fromString(value: String) = when {
            value == "DISMISS" -> DISMISS

            value == "DELAY" -> DELAY

            value.startsWith("TAP") -> TAP(
                value.removePrefix("TAP(buttonRegex=").removeSuffix(")"),
            )

            value.startsWith("BATCH") -> BATCH(
                value.removePrefix("BATCH(batchLength=").removeSuffix(")").toInt(),
            )

            else -> throw IllegalArgumentException("Unknown Action: $value")
        }
    }
}
