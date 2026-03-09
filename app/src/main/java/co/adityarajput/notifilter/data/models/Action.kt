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

    @Serializable
    data class DEBOUNCE(val cooldownLength: Int) : Action()

    @Serializable
    data object MUTE : Action()

    @Serializable
    data object ALERT : Action()

    @Serializable
    data class DISTURB(val pauseLength: Int) : Action()

    @Serializable
    data class DISMISS_STALE(val retentionLength: Int) : Action()

    @Composable
    fun verb() = when (this) {
        is DISMISS -> stringResource(R.string.dismiss_short)
        is TAP_NOTIFICATION -> stringResource(R.string.tap_notification_short)
        is TAP_BUTTON -> stringResource(R.string.tap_button_short, buttonRegex)
        is DELAY -> stringResource(R.string.delay_short)
        is MUTE -> stringResource(R.string.mute_short)
        is ALERT -> stringResource(R.string.alert_short)

        is BATCH -> stringResource(
            R.string.batch_short,
            pluralStringResource(R.plurals.hour, batchLength, batchLength),
        )

        is DEBOUNCE -> stringResource(
            R.string.debounce_short,
            pluralStringResource(R.plurals.minute, cooldownLength, cooldownLength),
        )

        is DISTURB -> stringResource(
            R.string.disturb_short,
            pluralStringResource(R.plurals.minute, pauseLength, pauseLength),
        )

        is DISMISS_STALE -> stringResource(
            R.string.dismiss_stale_short,
            pluralStringResource(R.plurals.minute, retentionLength, retentionLength),
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
            is DEBOUNCE -> R.string.debounce_long
            is MUTE -> R.string.mute_long
            is ALERT -> R.string.alert_long
            is DISTURB -> R.string.disturb_long
            is DISMISS_STALE -> R.string.dismiss_stale_long
        },
    )

    fun isOfType(it: Action) = this::class == it::class

    companion object {
        val entries by lazy {
            listOf(
                DISMISS, TAP_NOTIFICATION, TAP_BUTTON(""), BATCH(3),
                DELAY, DEBOUNCE(2), MUTE, ALERT, DISTURB(5), DISMISS_STALE(15),
            )
        }

        fun fromString(value: String) = when {
            value == "DISMISS" -> DISMISS

            value == "TAP_NOTIFICATION" -> TAP_NOTIFICATION

            value == "DELAY" -> DELAY

            value == "MUTE" -> MUTE

            value == "ALERT" -> ALERT

            value.startsWith("TAP_BUTTON") -> TAP_BUTTON(
                value.removePrefix("TAP_BUTTON(buttonRegex=").removeSuffix(")"),
            )

            value.startsWith("BATCH") -> BATCH(
                value.removePrefix("BATCH(batchLength=").removeSuffix(")").toInt(),
            )

            value.startsWith("DEBOUNCE") -> DEBOUNCE(
                value.removePrefix("DEBOUNCE(cooldownLength=").removeSuffix(")").toInt(),
            )

            value.startsWith("DISTURB") -> DISTURB(
                value.removePrefix("DISTURB(pauseLength=").removeSuffix(")").toInt(),
            )

            value.startsWith("DISMISS_STALE") -> DISMISS_STALE(
                value.removePrefix("DISMISS_STALE(retentionLength=").removeSuffix(")").toInt(),
            )

            else -> {
                Logger.e("Action.fromString", value)
                throw IllegalArgumentException("Can't convert value to Action, unknown value: $value")
            }
        }
    }
}
