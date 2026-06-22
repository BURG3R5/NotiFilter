package co.adityarajput.notifilter.data.models

import co.adityarajput.notifilter.data.models.Action.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ActionTest {
    @Test
    fun Action_serialization_roundtrip() {
        val actions = Action.entries + listOf(
            // INFO: Non-default values for instantiable actions
            TAP_BUTTON("^Sno{2}z(e)$"), BATCH(6), DEBOUNCE(4),
            DISTURB(10), DISMISS_STALE(30),
            REPLACE(
                $$"${app} said ${title} at ${postTime}.",
                $$"The description was \"${content}\".",
            ),
        )

        actions.forEach {
            assertEquals(it, Action.fromString(it.toString()))
        }
    }

    @Test
    fun Action_deserialization_incorrectBase() {
        listOf(
            // INFO: 🤷
            "UNKNOWN_ACTION", "UNKNOWN_ACTION()", "UNKNOWN_ACTION(parameter=1)",

            // INFO: These should be singletons, not instances
            "DISMISS()", "TAP_NOTIFICATION()", "DELAY()", "MUTE()", "ALERT()",

            // INFO: These should be instances, not singletons
            "TAP_BUTTON", "BATCH", "DEBOUNCE", "DISTURB", "DISMISS_STALE", "REPLACE",
        ).forEach {
            assertFailsWith<IllegalArgumentException> {
                Action.fromString(it)
            }
        }
    }

    @Test
    fun Action_deserialization_incorrectParams() {
        // INFO: If exact prefix and suffix are not found, weird stuff happens. Hopefully this error can be manually resolved if it ever happens.
        listOf("TAP_BUTTON()", "TAP_BUTTON(parameter=1)").forEach {
            assertEquals(Action.fromString(it), TAP_BUTTON(it))
        }

        listOf(
            // INFO: Wrong parameters
            "BATCH(parameter=2)", "DEBOUNCE(parameter=3)", "DISTURB(parameter=4)",
            "DISMISS_STALE(parameter=5)",

            // INFO: Non-integer parameters
            "BATCH(batchLength=a)", "DEBOUNCE(cooldownLength=b)", "DISTURB(pauseLength=c)",
            "DISMISS_STALE(retentionLength=d)",
        ).forEach {
            assertFailsWith<NumberFormatException> {
                Action.fromString(it)
            }
        }

        listOf(
            "REPLACE()", "REPLACE(param=6)", "REPLACE(param=7, anotherParam=8)",
            "REPLACE(titleTemplate=title)", "REPLACE(titleTemplate=title, param=9)",
            "REPLACE(contentTemplate=content)",
            "REPLACE(contentTemplate=content, titleTemplate=title)",
        ).forEach {
            assertFailsWith<IndexOutOfBoundsException> { Action.fromString(it) }
        }

        // INFO: If exact prefix and suffix are not found, weird stuff happens. Hopefully this error can be manually resolved if it ever happens.
        assertEquals(
            REPLACE("REPLACE(param=10", "content)"),
            Action.fromString("REPLACE(param=10, contentTemplate=content)"),
        )
    }
}
