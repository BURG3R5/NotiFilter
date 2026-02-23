package co.adityarajput.notifilter.data.models

import android.app.ActivityOptions
import android.app.PendingIntent
import android.os.Build
import android.os.Build.VERSION_CODES.BAKLAVA
import android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE
import android.service.notification.StatusBarNotification

data class Intents(
    val main: PendingIntent?,
    val actions: Map<String, PendingIntent?>,
) {
    constructor(sbn: StatusBarNotification) : this(
        sbn.notification.contentIntent,
        (sbn.notification.actions ?: emptyArray())
            .filter { it.remoteInputs == null || it.remoteInputs.isEmpty() }
            .associate { it.title.toString() to it.actionIntent },
    )

    fun launchMain() {
        main?.run {
            if (Build.VERSION.SDK_INT >= BAKLAVA) {
                send(
                    ActivityOptions.makeBasic()
                        .setPendingIntentBackgroundActivityStartMode(
                            ActivityOptions.MODE_BACKGROUND_ACTIVITY_START_ALLOW_ALWAYS,
                        )
                        .toBundle(),
                )
            } else if (Build.VERSION.SDK_INT >= UPSIDE_DOWN_CAKE) {
                @Suppress("DEPRECATION")
                send(
                    ActivityOptions.makeBasic()
                        .setPendingIntentBackgroundActivityStartMode(
                            ActivityOptions.MODE_BACKGROUND_ACTIVITY_START_ALLOWED,
                        )
                        .toBundle(),
                )
            } else {
                send()
            }
        }
    }
}
