package co.adityarajput.evaluator.functions

import org.junit.Assert.assertEquals
import org.junit.Test

class EqualTest {
    @Test
    fun Equal_evaluatesCorrectly() {
        assertEquals("true", Equal.evaluate(listOf("randomString", "randomString")))
        assertEquals("false", Equal.evaluate(listOf("randomString", "anotherString")))
    }
}
