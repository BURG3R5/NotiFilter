package co.adityarajput.notifilter.data.models

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import co.adityarajput.notifilter.R
import co.adityarajput.notifilter.utils.Logger
import kotlinx.serialization.Serializable

@Suppress("ClassName")
@Serializable
sealed class Action {
    @Serializable
    data object DISMISS : Action()

    @Serializable
    data object TAP_NOTIFICATION : Action()

    @Serializable
    data class TAP_BUTTON(val buttonRegex: String) : Action()

    @Serializable
    data class BATCH(val batchLength: Int) : Action()

    @Serializable
    data object DELAY : Action()

    @Composable
    fun verb() = when (this) {
        is DISMISS -> stringResource(R.string.dismiss_short)
        is TAP_NOTIFICATION -> stringResource(R.string.tap_notification_short)
        is TAP_BUTTON -> stringResource(R.string.tap_button_short, buttonRegex)
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
            is TAP_NOTIFICATION -> R.string.tap_notification_long
            is TAP_BUTTON -> R.string.tap_button_long
            is BATCH -> R.string.batch_long
            is DELAY -> R.string.delay_long
        },
    )

    fun isOfType(it: Action) = this::class == it::class

    companion object {
        val entries = listOf(DISMISS, TAP_NOTIFICATION, TAP_BUTTON(""), BATCH(3), DELAY)

        fun fromString(value: String) = when {
            value == "DISMISS" -> DISMISS

            value == "TAP_NOTIFICATION" -> TAP_NOTIFICATION

            value == "DELAY" -> DELAY

            value.startsWith("TAP_BUTTON") -> TAP_BUTTON(
                value.removePrefix("TAP_BUTTON(buttonRegex=").removeSuffix(")"),
            )

            value.startsWith("BATCH") -> BATCH(
                value.removePrefix("BATCH(batchLength=").removeSuffix(")").toInt(),
            )

            else -> {
                Logger.e("Action.fromString", value)
                throw IllegalArgumentException("Can't convert value to Action, unknown value: $value")
            }
        }
    }
}
