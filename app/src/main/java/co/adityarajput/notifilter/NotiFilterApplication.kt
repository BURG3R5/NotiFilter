package co.adityarajput.notifilter

import android.app.Application
import co.adityarajput.notifilter.data.AppContainer

class NotiFilterApplication : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()

        container = AppContainer(this)

        // INFO: To be used when taking screenshots for store metadata
        // if (0 != applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) {
        //     container.seedDemoData()
        // }
    }
}
