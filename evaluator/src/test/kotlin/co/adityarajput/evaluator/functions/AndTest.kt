package co.adityarajput.evaluator.functions

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

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
            assertThrows(IllegalArgumentException::class.java) {
                And.evaluate(it)
            }
        }
    }
}
