package co.adityarajput.evaluator

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class EvaluatorTest {
    private val basicEvaluator = Evaluator()

    private val extendedEvaluator = Evaluator(
        object : Function("customFunction", 2) {
            override fun evaluate(arguments: List<String>): String =
                (!arguments[0].toBooleanStrict() && arguments[1] == "string").toString()
        },
    )

    @Test
    fun Evaluator_evaluatesValidExpressions() {
        // Basic function calls
        assertEquals("false", basicEvaluator.evaluate("""and("true", "false")"""))
        assertEquals("true", basicEvaluator.evaluate("""or("true", "false")"""))
        assertEquals("false", basicEvaluator.evaluate("""not("true")"""))
        assertEquals("false", basicEvaluator.evaluate("""equal("string1", "string2")"""))

        // Strings containing spaces, numbers, symbols
        assertEquals("false", basicEvaluator.evaluate("""equal("white space", "comma,inside")"""))
        assertEquals("false", basicEvaluator.evaluate("""equal("123!@#", "line1\nline2")"""))
        assertEquals("false", basicEvaluator.evaluate("""equal("", "(parentheses)")"""))

        // Spaces are ignored
        assertEquals("false", basicEvaluator.evaluate("""  and( "true" , "false" )  """))
        assertEquals("true", basicEvaluator.evaluate("or(\n\"true\",\t\"false\")"))
        assertEquals("false", basicEvaluator.evaluate("equal(\"/\'\",\t\"'\\\")"))

        // Nested expressions
        assertEquals(
            "false",
            extendedEvaluator.evaluate("""and(or("true", equal("42", "42")), not(customFunction("false", "string")))"""),
        )
        assertEquals(
            "true",
            basicEvaluator.evaluate("""not(not(not(not(and("true", "true")))))"""),
        )
        assertEquals(
            "true",
            basicEvaluator.evaluate("""or(and("false", "true"), and("true", or("false", "true")))"""),
        )
    }

    @Test
    fun Evaluator_throwsIfExpressionIsInvalid() {
        listOf(
            // Empty or blank
            """""",
            """   """,

            // Missing or additional elements
            """("true")""",
            """and("true",)""",
            """and "true", "false")""",
            """and("true", "false"""",
            """and("true", false)""",
            """and("true", "false"))""",
            """"justAString"""",
            """and(and("true", "false")""",

            // Too few or too many arguments
            """and()""",
            """and("true")""",
            """not("true", "false")""",

            // Incorrect function names
            """Not("true")""",
            """EQUAL("true", "false")""",
            """unknownFunction("true")""",
            """42("true", "false")""",

            // Two expressions at root
            """and("true", "false") or("true", "false")""",
        ).forEach {
            assertFailsWith<IllegalArgumentException> {
                basicEvaluator.evaluate(it)
            }
        }
    }
}
