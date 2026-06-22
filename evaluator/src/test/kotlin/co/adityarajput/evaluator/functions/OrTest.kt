package co.adityarajput.evaluator.functions

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class OrTest {
    @Test
    fun Or_evaluatesCorrectly() {
        assertEquals("true", Or.evaluate(listOf("true", "true")))
        assertEquals("true", Or.evaluate(listOf("true", "false")))
        assertEquals("true", Or.evaluate(listOf("false", "true")))
        assertEquals("false", Or.evaluate(listOf("false", "false")))
    }

    @Test
    fun Or_throwsIfNonBooleanArguments() {
        listOf(
            listOf("notABoolean", "true"),
            listOf("true", "notABoolean"),
        ).forEach {
            assertFailsWith<IllegalArgumentException> {
                Or.evaluate(it)
            }
        }
    }
}
