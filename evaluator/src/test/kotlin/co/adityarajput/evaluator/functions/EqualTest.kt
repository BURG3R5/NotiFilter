package co.adityarajput.evaluator.functions

import kotlin.test.Test
import kotlin.test.assertEquals

class EqualTest {
    @Test
    fun Equal_evaluatesCorrectly() {
        assertEquals("true", Equal.evaluate(listOf("randomString", "randomString")))
        assertEquals("false", Equal.evaluate(listOf("randomString", "anotherString")))
    }
}
