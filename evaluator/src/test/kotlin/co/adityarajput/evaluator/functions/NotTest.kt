package co.adityarajput.evaluator.functions

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class NotTest {
    @Test
    fun Not_evaluatesCorrectly() {
        assertEquals("false", Not.evaluate(listOf("true")))
        assertEquals("true", Not.evaluate(listOf("false")))
    }

    @Test
    fun Not_throwsIfNonBooleanArgument() {
        listOf(
            listOf("notABoolean"),
        ).forEach {
            assertThrows(IllegalArgumentException::class.java) {
                Not.evaluate(it)
            }
        }
    }
}
