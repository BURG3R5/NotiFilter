package co.adityarajput.notifilter.utils

import co.adityarajput.notifilter.data.models.App
import co.adityarajput.notifilter.data.models.Notification
import org.junit.Assert.*
import org.junit.Test

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
        assertTrue("$EMOJI_PATTERN_DISPLAY, $EMOJI_PATTERN_DISPLAY".containsMatchIn("👋, 🌍"))
        assertFalse(EMOJI_PATTERN_DISPLAY.containsMatchIn("Hello, world"))
    }

    @Test
    fun Regex_replaceWithNotificationData() {
        val notification = Notification("MyTitle", "MyContent", "com.example.app", 0, id = 1)
        val allPackages = listOf(App("MyApp", "com.example.app"))

        assertEquals(
            $$"${app} (${package}) sent ${title} - ${content} and ${unknown}"
                .replaceWithNotificationData(notification, allPackages),
            $$"MyApp (com.example.app) sent MyTitle - MyContent and ${unknown}",
        )
    }

    @Test
    fun Regex_generateRegex() {
        assertTrue("test".generateRegex() == "^test$")
        assertTrue("tom@newsletter.tomscott.com".generateRegex() == "^tom@newsletter\\.tomscott\\.com$")
        assertTrue("assertTrue(0 == 0)".generateRegex() == "^assertTrue\\(0 == 0\\)$")
    }
}
