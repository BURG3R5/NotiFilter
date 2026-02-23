package co.adityarajput.notifilter.utils

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.os.Build
import androidx.core.content.edit
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.GlanceAppWidgetManager.Companion.SET_WIDGET_PREVIEWS_RESULT_SUCCESS
import androidx.glance.appwidget.updateAll
import co.adityarajput.notifilter.Constants.STATE
import co.adityarajput.notifilter.Constants.WIDGET_PREVIEW_SET_AT
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

suspend fun Context.setWidgetPreview() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
        val sharedPreferences = getSharedPreferences(STATE, MODE_PRIVATE)
        val now = System.currentTimeMillis()

        if (now - sharedPreferences.getLong(
                WIDGET_PREVIEW_SET_AT,
                0,
            ) < 2 * 60 * 60 * 1000
        ) return

        val result = GlanceAppWidgetManager(this@setWidgetPreview)
            .setWidgetPreviews(WidgetReceiver::class)

        if (result != SET_WIDGET_PREVIEWS_RESULT_SUCCESS) return

        sharedPreferences.edit { putLong(WIDGET_PREVIEW_SET_AT, now) }
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
