package co.adityarajput.evaluator.functions

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class AndTest {
    @Test
    fun And_evaluatesCorrectly() {
        assertEquals("true", And.evaluate(listOf("true", "true")))
        assertEquals("false", And.evaluate(listOf("true", "false")))
        assertEquals("false", And.evaluate(listOf("false", "true")))
        assertEquals("false", And.evaluate(listOf("false", "false")))
    }

    @Test
    fun And_throwsIfNonBooleanArguments() {
        listOf(
            listOf("notABoolean", "true"),
            listOf("true", "notABoolean"),
        ).forEach {
            assertFailsWith<IllegalArgumentException> {
                And.evaluate(it)
            }
        }
    }
}
