package co.adityarajput.notifilter.views

import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.*
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.components.Scaffold
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.*
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import co.adityarajput.notifilter.R
import co.adityarajput.notifilter.data.AppContainer
import co.adityarajput.notifilter.data.Cache
import co.adityarajput.notifilter.data.models.App
import co.adityarajput.notifilter.data.models.Notification
import co.adityarajput.notifilter.utils.Logger
import co.adityarajput.notifilter.utils.Permission
import co.adityarajput.notifilter.utils.isGranted
import co.adityarajput.notifilter.utils.toReadableTime

class Widget(val isPreview: Boolean = false) : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            if (isPreview) {
                Content(sampleNotifications)
            } else {
                Content(
                    AppContainer(context).repository.log().collectAsState(emptyList()).value,
                    Cache.getAllPackages(context.packageManager),
                )
            }
        }
    }

    override suspend fun providePreview(context: Context, widgetCategory: Int) =
        provideContent { Content(sampleNotifications) }
}

@Composable
@GlanceComposable
private fun Content(notifications: List<Notification>, allPackages: List<App> = emptyList()) {
    val context = LocalContext.current
    val canLaunchMainIntents = remember { context.isGranted(Permission.ACCESSIBILITY_SERVICE) }

    Scaffold(
        GlanceModifier.padding(vertical = 16.dp),
        if (notifications.isEmpty()) null else { ->
            Text(
                context.getString(R.string.widget_title),
                GlanceModifier
                    .fillMaxWidth()
                    .padding(16.dp, 0.dp, 16.dp, 8.dp),
                style = TextStyle(
                    GlanceTheme.colors.onSurface,
                    20.sp,
                ),
            )
        },
    ) {
        if (notifications.isEmpty()) {
            Box(GlanceModifier.fillMaxSize(), Alignment.Center) {
                Text(
                    context.getString(R.string.no_notifications_logged),
                    style = TextStyle(GlanceTheme.colors.onSurface),
                )
            }
        } else {
            LazyColumn(GlanceModifier.fillMaxSize()) {
                items(notifications, { it.id.toLong() }) {
                    val intents = Cache.intents[it.data.hashCode()]

                    Box(
                        modifier = GlanceModifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                    ) {
                        Column(
                            GlanceModifier
                                .fillMaxWidth()
                                .cornerRadius(16.dp)
                                .padding(16.dp)
                                .background(GlanceTheme.colors.primaryContainer)
                                .wrapContentHeight()
                                .clickable {
                                    if (!canLaunchMainIntents) {
                                        Toast.makeText(
                                            context,
                                            context.getString(R.string.accessibility_service_description),
                                            Toast.LENGTH_SHORT,
                                        ).show()
                                    } else {
                                        try {
                                            if (intents?.main != null) intents.launchMain() else {
                                                context.startActivity(
                                                    context.packageManager.getLaunchIntentForPackage(
                                                        it.origin,
                                                    ),
                                                )
                                            }
                                        } catch (e: Exception) {
                                            Logger.e("Widget", "Error clicking $it", e)
                                        }
                                    }
                                },
                        ) {
                            Row(
                                GlanceModifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    it.appNameFrom(allPackages),
                                    style = TextStyle(
                                        GlanceTheme.colors.onPrimaryContainer,
                                        11.sp,
                                    ),
                                )
                                Spacer(GlanceModifier.defaultWeight())
                                Text(
                                    it.timestamp.toReadableTime(),
                                    style = TextStyle(
                                        GlanceTheme.colors.onPrimaryContainer,
                                        8.sp,
                                    ),
                                )
                            }
                            if (it.title.isNotBlank()) {
                                Text(
                                    it.title,
                                    style = TextStyle(
                                        GlanceTheme.colors.onPrimaryContainer,
                                        16.sp,
                                    ),
                                )
                            }
                            if (it.content.isNotBlank()) {
                                Text(
                                    it.content,
                                    style = TextStyle(
                                        GlanceTheme.colors.onPrimaryContainer,
                                        12.sp,
                                    ),
                                    maxLines = 2,
                                )
                            }
                            if (!intents?.actions.isNullOrEmpty()) {
                                Row(GlanceModifier.fillMaxWidth()) {
                                    intents.actions.forEach { (title, intent) ->
                                        Text(
                                            title,
                                            GlanceModifier
                                                .padding(horizontal = 8.dp)
                                                .padding(top = 8.dp)
                                                .defaultWeight()
                                                .clickable { intent?.send() },
                                            style = TextStyle(
                                                GlanceTheme.colors.onPrimaryContainer,
                                                14.sp,
                                                textAlign = TextAlign.Center,
                                            ),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private val sampleNotifications = listOf(
    Notification(
        "Book Club",
        "Bob: Please go for something lighter this time. I'm tired of tomes!",
        "WhatsApp",
        System.currentTimeMillis() - 37 * 60 * 1000,
        id = 5,
    ),
    Notification(
        "tom@newsletter.tomscott.com",
        "The week: a microphone, a ropeway, and something very sour.\nHello!\nOver the last few days...",
        "Gmail",
        System.currentTimeMillis() - 3 * 60 * 60 * 1000,
        id = 4,
    ),
    Notification(
        "Upcoming alarm",
        "Wed 11:30 AM - Exercise",
        "Clock",
        System.currentTimeMillis() - 25 * 60 * 60 * 1000,
        id = 3,
    ),
    Notification(
        "Upcoming alarm",
        "Wed 8:30 AM - Wake up",
        "Clock",
        System.currentTimeMillis() - 28 * 60 * 60 * 1000,
        id = 2,
    ),
    Notification(
        "Download paused",
        "A software update is available.",
        "Software update",
        System.currentTimeMillis() - 2 * 24 * 60 * 60 * 1000,
        id = 1,
    ),
)
