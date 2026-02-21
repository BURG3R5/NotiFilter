package co.adityarajput.notifilter

import android.app.Application
import android.content.pm.ApplicationInfo
import co.adityarajput.notifilter.data.AppContainer
import co.adityarajput.notifilter.utils.pushWidgetPreview
import co.adityarajput.notifilter.utils.subscribeWidgetToFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class NotiFilterApplication : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()

        container = AppContainer(this)

        if (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0) {
            // INFO: While debugging, populate database with demo data for screenshots
            container.seedDemoData()
        }

        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            this@NotiFilterApplication.pushWidgetPreview()
            subscribeWidgetToFlow(this@NotiFilterApplication, container.repository.log())
        }
    }
}
