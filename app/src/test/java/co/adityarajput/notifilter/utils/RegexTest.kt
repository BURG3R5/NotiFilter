package co.adityarajput.notifilter.utils

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

@Suppress("TestFunctionName")
class RegexTest {
    @Test
    fun Regex_containsMatchIn_normal() {
        assertTrue("sample".containsMatchIn("This is a sample string."))
        assertFalse("test".containsMatchIn("This is a sample string."))
        assertTrue("\\w{6}".containsMatchIn("This is a sample string."))
        assertFalse("\\w{7}".containsMatchIn("This is a sample string."))
    }

    @Test
    fun Regex_containsMatchIn_emoji() {
        assertTrue("🌍".containsMatchIn("Hello, 🌍"))
        assertFalse("👋".containsMatchIn("Hello, 🌍"))
        assertFalse("🌍".containsMatchIn("Hello, world"))

        assertTrue(EMOJI_PATTERN_DISPLAY.containsMatchIn("Hello, 🌍"))
        assertTrue("${EMOJI_PATTERN_DISPLAY}+".containsMatchIn("👋🌍"))
        assertFalse(EMOJI_PATTERN_DISPLAY.containsMatchIn("Hello, world"))
    }

    @Test
    fun Regex_generateRegex() {
        assertTrue("test".generateRegex() == "^test$")
        assertTrue("tom@newsletter.tomscott.com".generateRegex() == "^tom@newsletter\\.tomscott\\.com$")
        assertTrue("assertTrue(0 == 0)".generateRegex() == "^assertTrue\\(0 == 0\\)$")
    }
}
