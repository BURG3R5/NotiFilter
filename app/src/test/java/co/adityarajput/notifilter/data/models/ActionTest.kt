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
        listOf(
            // Missing or wrong parameters
            "TAP_BUTTON()", "TAP_BUTTON(parameter=1)", "BATCH(parameter=2)",
            "DEBOUNCE(parameter=3)", "DISTURB(parameter=4)", "DISMISS_STALE(parameter=5)",
            "REPLACE()", "REPLACE(param=6)", "REPLACE(param=7, anotherParam=8)",
            "REPLACE(titleTemplate=title)", "REPLACE(titleTemplate=title, param=9)",
            "REPLACE(contentTemplate=content)",
            "REPLACE(contentTemplate=content, titleTemplate=title)",
            "REPLACE(param=10, contentTemplate=content)",

            // Non-integer parameters
            "BATCH(batchLength=a)", "DEBOUNCE(cooldownLength=b)", "DISTURB(pauseLength=c)",
            "DISMISS_STALE(retentionLength=d)",
        ).forEach {
            assertFailsWith<IllegalArgumentException> { Action.fromString(it) }
        }
    }
}
