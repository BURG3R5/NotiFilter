package co.adityarajput.notifilter.data

import android.content.Context
import co.adityarajput.notifilter.data.models.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json

class AppContainer(private val context: Context) {
    val repository: Repository by lazy {
        Repository(
            NotiFilterDatabase.getDatabase(context).filterDao(),
            NotiFilterDatabase.getDatabase(context).notificationDao(),
        )
    }

    suspend fun export() =
        Json.encodeToString<List<Filter>>(repository.filters().first())

    suspend fun import(json: String) {
        repository.deleteFilters()
        repository.upsert(*Json.decodeFromString<Array<Filter>>(json))
    }

    fun seedDemoData() {
        runBlocking {
            if (
                repository.filters().first().isEmpty() &&
                repository.notifications().first().isEmpty()
            ) {
                repository.upsert(
                    Filter(
                        App("Clock", "com.google.android.deskclock"),
                        "Upcoming alarm",
                        Action.DISMISS,
                        RegexTarget.TITLE,
                        hits = 87,
                    ),
                    Filter(
                        App("Gmail", "com.google.android.gm"),
                        "Verify your identity",
                        Action.TAP_NOTIFICATION,
                        RegexTarget.CONTENT,
                        enabled = false,
                    ),
                    Filter(
                        App("Software update", "com.wssyncmldm"),
                        "software update",
                        Action.TAP_BUTTON("Remind me"),
                        RegexTarget.CONTENT,
                        schedule = Schedule(days = setOf(2, 3, 4, 5, 6)),
                        hits = 23,
                    ),
                    Filter(
                        App("Gmail", "com.google.android.gm"),
                        "[Nn]ewsletter",
                        Action.BATCH(3),
                        RegexTarget.OR,
                        widgetEnabled = true,
                        hits = 1,
                    ),
                    Filter(
                        App("WhatsApp", "com.whatsapp"),
                        "Book Club",
                        Action.DELAY,
                        RegexTarget.AND,
                        "^Bob",
                        schedule = Schedule(start = 9 * 60, end = 17 * 60),
                        hits = 15,
                    ),
                    Filter(
                        App("WhatsApp", "com.whatsapp"),
                        "Photo",
                        Action.DEBOUNCE(1),
                        RegexTarget.CONTENT,
                        historyEnabled = false,
                    ),
                    Filter(
                        App("Tumblr", "com.tumblr"),
                        "poll",
                        Action.MUTE,
                        RegexTarget.CONTENT,
                        hits = 17,
                        widgetEnabled = true,
                    ),
                    Filter(
                        App("F-Droid", "org.fdroid"),
                        "Update available",
                        Action.ALERT,
                        RegexTarget.AND,
                        "NotiFilter|Alarmetrics|FileFlow",
                        historyEnabled = false,
                    ),
                    Filter(
                        Any,
                        "urgent",
                        Action.DISTURB(5),
                        RegexTarget.OR,
                        historyEnabled = false,
                    ),
                    Filter(
                        App("Twitch", "tv.twitch.android.app"),
                        "live",
                        Action.DISMISS_STALE(60),
                        RegexTarget.OR,
                        historyEnabled = false,
                    ),
                    Filter(
                        App("GitHub", "com.github.android"),
                        """titleMatches("approved your pull request")""",
                        Action.REPLACE("PR approved", $$"${content}"),
                        RegexTarget.EXPRESSION,
                        historyEnabled = false,
                    ),
                )
                repository.upsert(
                    Notification(
                        "Download paused",
                        "A software update is available.",
                        "Software update",
                        System.currentTimeMillis() - 2 * 24 * 60 * 60 * 1000,
                    ),
                    Notification(
                        "Upcoming alarm",
                        "Wed 8:30 AM - Wake up",
                        "Clock",
                        System.currentTimeMillis() - 28 * 60 * 60 * 1000,
                    ),
                    Notification(
                        "Upcoming alarm",
                        "Wed 11:30 AM - Exercise",
                        "Clock",
                        System.currentTimeMillis() - 25 * 60 * 60 * 1000,
                    ),
                    Notification(
                        "tom@newsletter.tomscott.com",
                        "The week: a microphone, a ropeway, and something very sour.\nHello!\nOver the last few days...",
                        "Gmail",
                        System.currentTimeMillis() - 3 * 60 * 60 * 1000,
                        showInWidget = true,
                    ),
                    Notification(
                        "Book Club",
                        "Bob: Please go for something lighter this time. I'm tired of tomes!",
                        "WhatsApp",
                        System.currentTimeMillis() - 37 * 60 * 1000,
                    ),
                    Notification(
                        "Poll finale alert",
                        "Check the poll results for \"Do you pronounce Z as Zed?\".",
                        "Tumblr",
                        System.currentTimeMillis() - 3 * 60 * 1000,
                    ),
                )
            }
        }
    }
}
