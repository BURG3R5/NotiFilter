package co.adityarajput.notifilter

import androidx.glance.appwidget.GlanceAppWidgetReceiver
import co.adityarajput.notifilter.views.PanelWidget

class PanelWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget = PanelWidget()
}
