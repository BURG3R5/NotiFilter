package co.adityarajput.notifilter.views

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.*
import androidx.glance.action.clickable
import androidx.glance.appwidget.CheckBox
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.components.Scaffold
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.*
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import co.adityarajput.notifilter.R
import co.adityarajput.notifilter.data.AppContainer
import co.adityarajput.notifilter.data.models.*
import co.adityarajput.notifilter.utils.Logger
import kotlinx.coroutines.launch

class PanelWidget(val isPreview: Boolean = false) : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            if (isPreview) {
                Content(sampleFilters)
            } else {
                Content(
                    AppContainer(context).repository.filters().collectAsState(emptyList()).value,
                )
            }
        }
    }

    override suspend fun providePreview(context: Context, widgetCategory: Int) =
        provideContent { Content(sampleFilters) }
}

@Composable
@GlanceComposable
private fun Content(filters: List<Filter>) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        GlanceModifier.padding(vertical = 16.dp),
        if (filters.isEmpty()) null else { ->
            Text(
                context.getString(R.string.panel_widget_title),
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
        if (filters.isEmpty()) {
            Box(GlanceModifier.fillMaxSize(), Alignment.Center) {
                Text(
                    context.getString(R.string.no_filters_short),
                    style = TextStyle(GlanceTheme.colors.onSurface),
                )
            }
        } else {
            LazyColumn(GlanceModifier.fillMaxSize()) {
                items(filters, { it.id.toLong() }) {
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
                                    coroutineScope.launch {
                                        try {
                                            Logger.d("PanelWidget", "Toggling $it")
                                            AppContainer(context).repository.toggleEnabled(it)
                                        } catch (e: Exception) {
                                            Logger.e("PanelWidget", "Error toggling $it", e)
                                        }
                                    }
                                },
                        ) {
                            Row(
                                GlanceModifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    it.app.name,
                                    style = TextStyle(
                                        GlanceTheme.colors.onPrimaryContainer,
                                        11.sp,
                                    ),
                                )
                                Spacer(GlanceModifier.defaultWeight())
                                CheckBox(it.enabled, {})
                            }
                            Text(
                                it.title,
                                style = TextStyle(
                                    GlanceTheme.colors.onPrimaryContainer,
                                    16.sp,
                                ),
                            )
                            Text(
                                it.action.verb(true, context),
                                style = TextStyle(
                                    GlanceTheme.colors.onPrimaryContainer,
                                    12.sp,
                                ),
                                maxLines = 2,
                            )
                        }
                    }
                }
            }
        }
    }
}

private val sampleFilters = listOf(
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
        App("WhatsApp", "com.whatsapp"),
        "Book Club",
        Action.DELAY(),
        RegexTarget.AND,
        "^Bob",
        schedule = Schedule(start = 9 * 60, end = 17 * 60),
        hits = 15,
    ),
    Filter(
        App("Tumblr", "com.tumblr"),
        "poll",
        Action.MUTE,
        RegexTarget.CONTENT,
        hits = 17,
        widgetEnabled = true,
    ),
)
