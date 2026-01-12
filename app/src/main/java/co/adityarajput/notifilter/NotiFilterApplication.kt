package co.adityarajput.notifilter

import android.app.Application
import android.content.pm.ApplicationInfo
import co.adityarajput.notifilter.data.AppContainer

class NotiFilterApplication : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()

        container = AppContainer(this)

        if (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0) {
            // INFO: While debugging, populate database with demo data for screenshots
            container.seedDemoData()
        }
    }
}
