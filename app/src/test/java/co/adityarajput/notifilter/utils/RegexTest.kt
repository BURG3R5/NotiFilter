package co.adityarajput.notifilter.utils

import co.adityarajput.notifilter.data.models.App
import co.adityarajput.notifilter.data.models.Notification
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.ZonedDateTime

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
        assertTrue("$EMOJI_PATTERN_DISPLAY, $EMOJI_PATTERN_DISPLAY".containsMatchIn("👋, 🌍"))
        assertFalse(EMOJI_PATTERN_DISPLAY.containsMatchIn("Hello, world"))
    }

    @Test
    fun Regex_replaceWithNotificationData() {
        val notification = Notification(
            "MyTitle", "MyContent", "com.example.app",
            ZonedDateTime.now().withHour(1).withMinute(0).withSecond(0).withNano(0)
                .toInstant()
                .toEpochMilli(),
            id = 1,
        )
        val allPackages = listOf(App("MyApp", "com.example.app"))

        assertTrue(
            $$"At ${postTime}, ${app} (${package}) sent ${title} - ${content} and ${unknown}"
                .replaceWithNotificationData(notification, allPackages)
                    == $$"At 1:00 am, MyApp (com.example.app) sent MyTitle - MyContent and ${unknown}",
        )
    }

    @Test
    fun Regex_generateRegex() {
        assertTrue("test".generateRegex() == "^test$")
        assertTrue("tom@newsletter.tomscott.com".generateRegex() == "^tom@newsletter\\.tomscott\\.com$")
        assertTrue("assertTrue(0 == 0)".generateRegex() == "^assertTrue\\(0 == 0\\)$")
    }
}
