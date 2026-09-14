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

    @Test
    fun Filter_isNotOnCooldown() {
        val oldAndEmpty = Notification("", "", "", 0)
        val oldFilteredByThis = oldAndEmpty.copy(filterId = 0)
        val newAndEmpty = oldAndEmpty.copy(timestamp = System.currentTimeMillis())
        val newFilteredByThis = newAndEmpty.copy(filterId = 0)
        val newFilteredByOther = newAndEmpty.copy(filterId = 1)

        val withoutCooldown = Filter(Any, "", Action.DISMISS)

        assertTrue(withoutCooldown.isNotOnCooldown(listOf()))
        assertTrue(withoutCooldown.isNotOnCooldown(listOf(oldAndEmpty)))
        assertTrue(withoutCooldown.isNotOnCooldown(listOf(newAndEmpty)))
        assertTrue(withoutCooldown.isNotOnCooldown(listOf(oldFilteredByThis)))
        assertTrue(withoutCooldown.isNotOnCooldown(listOf(newFilteredByThis)))
        assertTrue(withoutCooldown.isNotOnCooldown(listOf(newFilteredByOther)))
        assertTrue(
            withoutCooldown.isNotOnCooldown(
                listOf(
                    oldAndEmpty,
                    oldFilteredByThis,
                    newAndEmpty,
                    newFilteredByThis,
                    newFilteredByOther,
                ),
            ),
        )

        val withCooldown = withoutCooldown.copy(cooldown = 86_400_000)

        assertTrue(withCooldown.isNotOnCooldown(listOf()))
        assertTrue(withCooldown.isNotOnCooldown(listOf(oldAndEmpty)))
        assertTrue(withCooldown.isNotOnCooldown(listOf(newAndEmpty)))
        assertTrue(withCooldown.isNotOnCooldown(listOf(oldFilteredByThis)))
        assertFalse(withCooldown.isNotOnCooldown(listOf(newFilteredByThis)))
        assertTrue(withCooldown.isNotOnCooldown(listOf(newFilteredByOther)))
        assertTrue(withCooldown.isNotOnCooldown(listOf(oldAndEmpty, newAndEmpty)))
        assertTrue(withCooldown.isNotOnCooldown(listOf(newAndEmpty, oldFilteredByThis)))
        assertTrue(withCooldown.isNotOnCooldown(listOf(oldFilteredByThis, newFilteredByOther)))
        assertFalse(withCooldown.isNotOnCooldown(listOf(newFilteredByOther, newFilteredByThis)))
        assertTrue(
            withCooldown.isNotOnCooldown(
                listOf(
                    oldAndEmpty,
                    oldFilteredByThis,
                    newAndEmpty,
                    newFilteredByOther,
                ),
            ),
        )
        assertFalse(
            withCooldown.isNotOnCooldown(
                listOf(
                    oldAndEmpty,
                    oldFilteredByThis,
                    newAndEmpty,
                    newFilteredByThis,
                    newFilteredByOther,
                ),
            ),
        )
    }
}
