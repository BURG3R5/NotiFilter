package co.adityarajput.evaluator.functions

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

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
            assertFailsWith<IllegalArgumentException> {
                Not.evaluate(it)
            }
        }
    }
}
