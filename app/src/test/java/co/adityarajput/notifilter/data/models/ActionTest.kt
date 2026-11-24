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
            TAP_BUTTON("^Sno{2}z(e)$"), BATCH(6.0), DELAY(5.0),
            DEBOUNCE(4.0), DISTURB(10.0), DISMISS_STALE(30.0),
            REPLACE(
                $$"${app} said ${title} at ${postTime}.",
                $$"The description was \"${content}\".",
            ),
            READ($$"${app} said ${title} and added ${content}"),
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
            "DISMISS()", "TAP_NOTIFICATION()", "MUTE()", "ALERT()",

            // INFO: These should be instances, not singletons
            "TAP_BUTTON", "BATCH", "DEBOUNCE", "DISTURB", "DISMISS_STALE", "REPLACE", "READ",
        ).forEach {
            assertFailsWith<IllegalArgumentException> {
                Action.fromString(it)
            }
        }
    }

    @Test
    fun Action_deserialization_legacy_params() {
        // INFO: The (outdated) object form of "DELAY" should also be supported
        assertEquals(DELAY(), Action.fromString("DELAY"))

        // INFO: 4 and 4.0 should be treated the same, for actions containing numeric (previously Int, now Double) parameters
        val legacyIntParams = listOf(
            "BATCH(batchLength=4)",
            "DELAY(delayLength=4)",
            "DEBOUNCE(cooldownLength=4)",
            "DISTURB(pauseLength=4)",
            "DISMISS_STALE(retentionLength=4)",
        )

        legacyIntParams.forEach {
            assertEquals(Action.fromString(it.replace("4", "4.0")), Action.fromString(it))
        }
    }

    @Test
    fun Action_deserialization_incorrectParams() {
        listOf(
            // INFO: Missing or wrong parameters
            "TAP_BUTTON()", "TAP_BUTTON(parameter=1)", "BATCH(parameter=2)", "DELAY(parameter=11)",
            "DEBOUNCE(parameter=3)", "DISTURB(parameter=4)", "DISMISS_STALE(parameter=5)",
            "REPLACE()", "REPLACE(param=6)", "REPLACE(param=7, anotherParam=8)",
            "REPLACE(titleTemplate=title)", "REPLACE(titleTemplate=title, param=9)",
            "REPLACE(contentTemplate=content)",
            "REPLACE(contentTemplate=content, titleTemplate=title)",
            "REPLACE(param=10, contentTemplate=content)",
            "READ()", "READ(param=11)",

            // INFO: Non-numeric parameters
            "BATCH(batchLength=a)", "DELAY(delayLength=e)", "DEBOUNCE(cooldownLength=b)",
            "DISTURB(pauseLength=c)", "DISMISS_STALE(retentionLength=d)",
        ).forEach {
            assertFailsWith<IllegalArgumentException> { Action.fromString(it) }
        }
    }
}
