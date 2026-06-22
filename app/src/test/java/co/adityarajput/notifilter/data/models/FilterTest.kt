package co.adityarajput.notifilter.data.models

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class FilterTest {
    private val pollResultNotification = Notification(
        "Poll finale alert",
        """Check the poll results for "Do you pronounce Z as Zed?".""",
        "Tumblr",
        0,
    )

    private val withoutContent = pollResultNotification.copy(content = "...")

    private val withoutTitle = pollResultNotification.copy(title = "...")

    private val emptyNotification = pollResultNotification.copy(title = "...", content = "...")

    private val titleFilter = Filter(Any, "Poll", Action.MUTE, RegexTarget.TITLE)

    private val contentFilter = Filter(Any, "poll", Action.MUTE, RegexTarget.CONTENT)

    private val orFilter = Filter(Any, "Poll", Action.MUTE, RegexTarget.OR)

    private val andFilter = Filter(Any, "Poll", Action.MUTE, RegexTarget.AND, "poll")

    private val expressionFilter = Filter(
        Any,
        """and(titleMatches("Poll"), contentMatches("poll"))""",
        Action.MUTE,
        RegexTarget.EXPRESSION,
    )

    @Test
    fun Filter_matchesTextOf_correct() {
        assertTrue(titleFilter.matchesTextOf(pollResultNotification))
        assertTrue(titleFilter.matchesTextOf(withoutContent))

        assertTrue(contentFilter.matchesTextOf(pollResultNotification))
        assertTrue(contentFilter.matchesTextOf(withoutTitle))

        assertTrue(orFilter.matchesTextOf(pollResultNotification))
        assertTrue(orFilter.matchesTextOf(withoutContent))

        assertTrue(andFilter.matchesTextOf(pollResultNotification))

        assertTrue(expressionFilter.matchesTextOf(pollResultNotification))
    }

    @Test
    fun Filter_matchesTextOf_incorrect() {
        assertFalse(titleFilter.matchesTextOf(withoutTitle))
        assertFalse(titleFilter.matchesTextOf(emptyNotification))

        assertFalse(contentFilter.matchesTextOf(withoutContent))
        assertFalse(contentFilter.matchesTextOf(emptyNotification))

        assertFalse(orFilter.matchesTextOf(withoutTitle))
        assertFalse(orFilter.matchesTextOf(emptyNotification))

        assertFalse(andFilter.matchesTextOf(withoutContent))
        assertFalse(andFilter.matchesTextOf(withoutTitle))
        assertFalse(andFilter.matchesTextOf(emptyNotification))

        assertFalse(expressionFilter.matchesTextOf(withoutContent))
        assertFalse(expressionFilter.matchesTextOf(withoutTitle))
        assertFalse(expressionFilter.matchesTextOf(emptyNotification))
    }
}
