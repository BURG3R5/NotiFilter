package co.adityarajput.notifilter.data.models

import android.content.Context
import android.icu.text.MessageFormat
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import co.adityarajput.notifilter.R
import co.adityarajput.notifilter.utils.Logger
import kotlinx.serialization.Serializable
import java.util.Locale

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
    data class BATCH(
        /**
         * In hours
         */
        val batchLength: Double,
    ) : Action()

    @Serializable
    data class DELAY(
        /**
         * In minutes
         */
        val delayLength: Double? = null,
    ) : Action()

    @Serializable
    data class DEBOUNCE(
        /**
         * In minutes
         */
        val cooldownLength: Double,
    ) : Action()

    @Serializable
    data object MUTE : Action()

    @Serializable
    data class ALERT(
        /**
         * In seconds
         */
        val alertLifespan: Double,
    ) : Action()

    @Serializable
    data class DISTURB(
        /**
         * In minutes
         */
        val pauseLength: Double,
    ) : Action()

    @Serializable
    data class DISMISS_STALE(
        /**
         * In minutes
         */
        val retentionLength: Double,
    ) : Action()

    @Serializable
    data class REPLACE(val titleTemplate: String, val contentTemplate: String) : Action()

    @Serializable
    data class READ(val speechTemplate: String) : Action()

    @Composable
    fun verb(isGlance: Boolean = false, context: Context? = null): String {
        @Composable
        fun getString(@StringRes id: Int, vararg formatArgs: Any) =
            if (isGlance) context!!.getString(id, *formatArgs)
            else stringResource(id, *formatArgs)

        return when (this) {
            is DISMISS -> getString(R.string.dismiss_short)
            is TAP_NOTIFICATION -> getString(R.string.tap_notification_short)
            is TAP_BUTTON -> getString(R.string.tap_button_short, buttonRegex)
            is MUTE -> getString(R.string.mute_short)
            is ALERT -> getString(R.string.alert_short)

            is DELAY -> if (delayLength == null) getString(R.string.delay_short) else getString(
                R.string.delay_by_short,
                MessageFormat(getString(R.string.minute), Locale.getDefault()).format(
                    arrayOf(delayLength),
                ),
            )

            is BATCH -> getString(
                R.string.batch_short,
                MessageFormat(getString(R.string.hour), Locale.getDefault()).format(
                    arrayOf(batchLength),
                ),
            )

            is DEBOUNCE -> getString(
                R.string.debounce_short,
                MessageFormat(getString(R.string.minute), Locale.getDefault()).format(
                    arrayOf(cooldownLength),
                ),
            )

            is DISTURB -> getString(
                R.string.disturb_short,
                MessageFormat(getString(R.string.minute), Locale.getDefault()).format(
                    arrayOf(pauseLength),
                ),
            )

            is DISMISS_STALE -> getString(
                R.string.dismiss_stale_short,
                MessageFormat(getString(R.string.minute), Locale.getDefault()).format(
                    arrayOf(retentionLength),
                ),
            )

            is REPLACE -> getString(R.string.replace_short, titleTemplate, contentTemplate)

            is READ -> getString(R.string.read_short, speechTemplate)
        }
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
            is REPLACE -> R.string.replace_long
            is READ -> R.string.read_long
        },
    )

    fun isOfType(it: Action) = this::class == it::class

    companion object {
        val entries by lazy {
            listOf(
                DISMISS, TAP_NOTIFICATION, TAP_BUTTON(""), BATCH(3.0),
                DELAY(), DEBOUNCE(2.0), MUTE, ALERT(2.0),
                DISTURB(5.0), DISMISS_STALE(15.0),
                REPLACE($$"${app} - ${title}", $$"${content}"), READ($$"${title}: ${content}"),
            )
        }

        fun fromString(value: String): Action = when (value) {
            "DISMISS" -> DISMISS

            "TAP_NOTIFICATION" -> TAP_NOTIFICATION

            "DELAY" -> DELAY()

            "MUTE" -> MUTE

            "ALERT" -> ALERT(2.0)

            else -> {
                TAP_BUTTON_REGEX.matchEntire(value)?.groupValues?.let {
                    return TAP_BUTTON(it[1])
                }

                BATCH_REGEX.matchEntire(value)?.groupValues?.let {
                    return BATCH(it[1].toDouble())
                }

                DELAY_REGEX.matchEntire(value)?.groupValues?.let {
                    return if (it[1] == "null") DELAY() else DELAY(it[1].toDouble())
                }

                DEBOUNCE_REGEX.matchEntire(value)?.groupValues?.let {
                    return DEBOUNCE(it[1].toDouble())
                }

                ALERT_REGEX.matchEntire(value)?.groupValues?.let {
                    return ALERT(it[1].toDouble())
                }

                DISTURB_REGEX.matchEntire(value)?.groupValues?.let {
                    return DISTURB(it[1].toDouble())
                }

                DISMISS_STALE_REGEX.matchEntire(value)?.groupValues?.let {
                    return DISMISS_STALE(it[1].toDouble())
                }

                REPLACE_REGEX.matchEntire(value)?.groupValues?.let {
                    return REPLACE(it[1], it[2])
                }

                READ_REGEX.matchEntire(value)?.groupValues?.let {
                    return READ(it[1])
                }

                Logger.e("Action.fromString", value)
                throw IllegalArgumentException("Can't convert $value into an Action")
            }
        }

        private const val UNSIGNED_DOUBLE_REGEX = "(?:[0-9]*\\.)?[0-9]+"

        private val TAP_BUTTON_REGEX = Regex("^TAP_BUTTON\\(buttonRegex=(.*)\\)$")
        private val BATCH_REGEX = Regex("^BATCH\\(batchLength=($UNSIGNED_DOUBLE_REGEX)\\)$")
        private val DELAY_REGEX = Regex("^DELAY\\(delayLength=(null|$UNSIGNED_DOUBLE_REGEX)\\)$")
        private val DEBOUNCE_REGEX =
            Regex("^DEBOUNCE\\(cooldownLength=($UNSIGNED_DOUBLE_REGEX)\\)$")
        private val ALERT_REGEX = Regex("^ALERT\\(alertLifespan=($UNSIGNED_DOUBLE_REGEX)\\)$")
        private val DISTURB_REGEX = Regex("^DISTURB\\(pauseLength=($UNSIGNED_DOUBLE_REGEX)\\)$")
        private val DISMISS_STALE_REGEX =
            Regex("^DISMISS_STALE\\(retentionLength=($UNSIGNED_DOUBLE_REGEX)\\)$")
        private val REPLACE_REGEX =
            Regex("^REPLACE\\(titleTemplate=(.*?), contentTemplate=(.*)\\)$")
        private val READ_REGEX = Regex("^READ\\(speechTemplate=(.*?)\\)$")
    }
}
