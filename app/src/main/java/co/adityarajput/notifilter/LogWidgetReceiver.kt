package co.adityarajput.notifilter

import androidx.glance.appwidget.GlanceAppWidgetReceiver
import co.adityarajput.notifilter.views.LogWidget

class LogWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget = LogWidget()
}
