package co.adityarajput.notifilter.utils

import android.content.Context
import android.os.Build
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.GlanceAppWidgetManager.Companion.SET_WIDGET_PREVIEWS_RESULT_RATE_LIMITED
import androidx.glance.appwidget.updateAll
import co.adityarajput.notifilter.WidgetReceiver
import co.adityarajput.notifilter.views.Widget
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

suspend fun Context.pushWidgetPreview() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
        if (
            GlanceAppWidgetManager(this)
                .setWidgetPreviews(WidgetReceiver::class)
            == SET_WIDGET_PREVIEWS_RESULT_RATE_LIMITED
        ) {
            Logger.e("Widgets", "Failed to set widget previews due to rate limiting")
        }
    }
}

suspend fun subscribeWidgetToFlow(context: Context, flow: Flow<Any>) =
    flow.collectLatest {
        withContext(Dispatchers.Main) {
            Mutex().withLock {
                Logger.d("Widgets", "Updating widget")
                Widget().updateAll(context)
            }
        }
    }

suspend fun Context.isWidgetUsed() =
    GlanceAppWidgetManager(this).getGlanceIds(Widget::class.java).isNotEmpty()

fun Context.addWidgetToHomeScreen() {
    CoroutineScope(Dispatchers.IO).launch {
        GlanceAppWidgetManager(this@addWidgetToHomeScreen)
            .requestPinGlanceAppWidget(WidgetReceiver::class.java, Widget(true))
    }
}
