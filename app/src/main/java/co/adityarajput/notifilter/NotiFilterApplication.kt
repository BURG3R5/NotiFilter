package co.adityarajput.notifilter

import android.app.Application
import co.adityarajput.notifilter.data.AppContainer
import co.adityarajput.notifilter.utils.setWidgetPreview
import co.adityarajput.notifilter.utils.subscribeWidgetToFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.acra.ACRA
import org.acra.config.dialog
import org.acra.config.mailSender
import org.acra.data.StringFormat
import org.acra.ktx.initAcra

class NotiFilterApplication : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()

        if (ACRA.isACRASenderServiceProcess())
            return

        initAcra {
            buildConfigClass = BuildConfig::class.java
            reportFormat = StringFormat.JSON

            mailSender {
                mailTo = Constants.CRASH_REPORT_EMAIL
                subject = "NotiFilter Crash Report"
                additionalSharedPreferences = listOf(Constants.STATE, Constants.SETTINGS)
            }

            dialog {
                title = "App Crashed"
                text =
                    "NotiFilter has encountered an unexpected error and crashed. Please report this incident to the developers using the following form."
                commentPrompt = "Your comments:"
                positiveButtonText = "Send email"
            }
        }

        container = AppContainer(this)

        if (BuildConfig.DEBUG) {
            // INFO: While debugging, populate database with demo data for screenshots
            container.seedDemoData()
        }

        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            this@NotiFilterApplication.setWidgetPreview()
            subscribeWidgetToFlow(this@NotiFilterApplication, container.repository.log())
        }
    }
}
