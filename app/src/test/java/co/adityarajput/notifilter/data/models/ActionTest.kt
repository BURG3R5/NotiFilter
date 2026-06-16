package co.adityarajput.notifilter.data.models

import co.adityarajput.notifilter.data.models.Action.*
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

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
            assertTrue(it == Action.fromString(it.toString()))
        }
    }

    @Test
    fun Action_deserialization_incorrectBase() {
        val strings = listOf(
            // INFO: 🤷
            "UNKNOWN_ACTION", "UNKNOWN_ACTION()", "UNKNOWN_ACTION(parameter=1)",

            // INFO: These should be singletons, not instances
            "DISMISS()", "TAP_NOTIFICATION()", "DELAY()", "MUTE()", "ALERT()",

            // INFO: These should be instances, not singletons
            "TAP_BUTTON", "BATCH", "DEBOUNCE", "DISTURB", "DISMISS_STALE", "REPLACE",
        )

        strings.forEach {
            assertThrows(IllegalArgumentException::class.java) {
                Action.fromString(it)
            }
        }
    }

    @Test
    fun Action_deserialization_incorrectParams() {
        val strings1 = listOf("TAP_BUTTON()", "TAP_BUTTON(parameter=1)")

        // INFO: If exact prefix and suffix are not found, weird stuff happens. Hopefully this error can be manually resolved if it ever happens.
        strings1.forEach {
            assertTrue(Action.fromString(it) == TAP_BUTTON(it))
        }

        val strings2 = listOf(
            // INFO: Wrong parameters
            "BATCH(parameter=2)", "DEBOUNCE(parameter=3)", "DISTURB(parameter=4)",
            "DISMISS_STALE(parameter=5)",

            // INFO: Non-integer parameters
            "BATCH(batchLength=a)", "DEBOUNCE(cooldownLength=b)", "DISTURB(pauseLength=c)",
            "DISMISS_STALE(retentionLength=d)",
        )

        strings2.forEach {
            assertThrows(NumberFormatException::class.java) {
                Action.fromString(it)
            }
        }

        val strings3 = listOf(
            "REPLACE()", "REPLACE(param=6)", "REPLACE(param=7, anotherParam=8)",
            "REPLACE(titleTemplate=title)", "REPLACE(titleTemplate=title, param=9)",
            "REPLACE(contentTemplate=content)",
            "REPLACE(contentTemplate=content, titleTemplate=title)",
        )

        strings3.forEach {
            assertThrows(IndexOutOfBoundsException::class.java) { Action.fromString(it) }
        }

        // INFO: If exact prefix and suffix are not found, weird stuff happens. Hopefully this error can be manually resolved if it ever happens.
        assertTrue(
            Action.fromString("REPLACE(param=10, contentTemplate=content)") ==
                    REPLACE("REPLACE(param=10", "content)"),
        )
    }
}
