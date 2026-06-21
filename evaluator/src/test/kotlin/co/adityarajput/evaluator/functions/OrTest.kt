package co.adityarajput.evaluator.functions

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

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
            assertThrows(IllegalArgumentException::class.java) {
                Or.evaluate(it)
            }
        }
    }
}
