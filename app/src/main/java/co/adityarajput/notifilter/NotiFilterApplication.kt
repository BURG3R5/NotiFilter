package co.adityarajput.notifilter

import android.app.Application
import co.adityarajput.notifilter.data.AppContainer

class NotiFilterApplication : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
