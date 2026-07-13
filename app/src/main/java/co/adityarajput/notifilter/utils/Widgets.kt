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
import co.adityarajput.notifilter.LogWidgetReceiver
import co.adityarajput.notifilter.PanelWidgetReceiver
import co.adityarajput.notifilter.data.AppContainer
import co.adityarajput.notifilter.views.LogWidget
import co.adityarajput.notifilter.views.PanelWidget
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

suspend fun Context.setWidgetPreviews() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
        val sharedPreferences = getSharedPreferences(STATE, MODE_PRIVATE)
        val now = System.currentTimeMillis()

        if (now - sharedPreferences.getLong(
                WIDGET_PREVIEW_SET_AT,
                0,
            ) < 2 * 60 * 60 * 1000
        ) return

        try {
            var result = GlanceAppWidgetManager(this@setWidgetPreviews)
                .setWidgetPreviews(LogWidgetReceiver::class)

            if (result != SET_WIDGET_PREVIEWS_RESULT_SUCCESS) {
                Logger.i("Widgets", "Log preview update skipped with result: $result")
                return
            }

            result = GlanceAppWidgetManager(this@setWidgetPreviews)
                .setWidgetPreviews(PanelWidgetReceiver::class)

            if (result != SET_WIDGET_PREVIEWS_RESULT_SUCCESS) {
                Logger.i("Widgets", "Panel preview update skipped with result: $result")
                return
            }

            sharedPreferences.edit { putLong(WIDGET_PREVIEW_SET_AT, now) }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Logger.e("Widgets", "Preview update failed", e)
        }
    }
}

suspend fun subscribeWidgetsToFlows(context: Context) {
    AppContainer(context).repository.log().collectLatest {
        withContext(Dispatchers.Main) {
            Mutex().withLock {
                Logger.d("Widgets", "Updating LogWidget")
                LogWidget().updateAll(context)
            }
        }
    }
    AppContainer(context).repository.filters().collectLatest {
        withContext(Dispatchers.Main) {
            Mutex().withLock {
                Logger.d("Widgets", "Updating PanelWidget")
                PanelWidget().updateAll(context)
            }
        }
    }
}

suspend fun Context.isLogWidgetUsed() =
    GlanceAppWidgetManager(this).getGlanceIds(LogWidget::class.java).isNotEmpty()

fun Context.addLogWidgetToHomeScreen() {
    CoroutineScope(Dispatchers.IO).launch {
        GlanceAppWidgetManager(this@addLogWidgetToHomeScreen)
            .requestPinGlanceAppWidget(LogWidgetReceiver::class.java, LogWidget(true))
    }
}
