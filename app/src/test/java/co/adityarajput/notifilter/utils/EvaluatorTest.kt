package co.adityarajput.notifilter.utils

import co.adityarajput.notifilter.data.models.Notification
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class EvaluatorTest {
    private val pollResultNotification = Notification(
        "Poll finale alert",
        """Check the poll results for "Do you pronounce Z as Zed?".""",
        "Tumblr",
        0,
    )

    private val correctTitle = """titleMatches("Poll finale alert")"""
    private val incorrectTitle = """titleMatches("Voted\? Results are here!")"""
    private val correctContent = """contentMatches("Do you pronounce Z as Zed\?")"""
    private val incorrectContent = """contentMatches("Do you pronounce Z as Zee\?")"""

    @Test
    fun Evaluator_matchesNotification() {
        assertTrue(correctTitle.evaluateAgainst(pollResultNotification))
        assertTrue(correctContent.evaluateAgainst(pollResultNotification))
        assertFalse(incorrectTitle.evaluateAgainst(pollResultNotification))
        assertFalse(incorrectContent.evaluateAgainst(pollResultNotification))

        assertTrue("and($correctTitle, $correctContent)".evaluateAgainst(pollResultNotification))
        assertTrue("or($correctTitle, $incorrectContent)".evaluateAgainst(pollResultNotification))
        assertTrue("or($incorrectTitle, $correctContent)".evaluateAgainst(pollResultNotification))
        assertFalse("and($incorrectTitle, $incorrectContent)".evaluateAgainst(pollResultNotification))
        assertFalse("and($incorrectTitle, $incorrectContent)".evaluateAgainst(pollResultNotification))
        assertTrue(
            "and(and($correctTitle, not($incorrectTitle)), or($correctContent, $incorrectContent))".evaluateAgainst(
                pollResultNotification,
            ),
        )
    }

    @Test
    fun Evaluator_throwsIfExpressionIsInvalid() {
        listOf(
            "and(", "and()", "and($correctTitle, $correctContent", "and($correctTitle)",

            "and($correctTitle, 1)", "not($correctTitle, $correctContent)",

            """titleMatches(""",
            """titleMatches()""",
            """titleMatches(Poll finale alert)""",
            """titleMatches("Poll finale alert)""",
            """titleMatches("Poll finale alert", 2)""",

            """contentMatches(""",
            """contentMatches()""",
            """contentMatches(Do you pronounce Z as Zed?)""",
            """contentMatches("Do you pronounce Z as Zed\?)""",
            """contentMatches("Do you pronounce Z as Zed\?", 2)""",
        ).forEach {
            assertFailsWith<IllegalArgumentException> {
                it.evaluateAgainst(null)
            }
        }
    }
}
