package co.adityarajput.notifilter

import androidx.glance.appwidget.GlanceAppWidgetReceiver
import co.adityarajput.notifilter.views.Widget

class WidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget = Widget()
}
