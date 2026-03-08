package co.adityarajput.notifilter.utils

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import co.adityarajput.notifilter.AlarmReceiver

@SuppressLint("MissingPermission")
fun AlarmManager.sendIntent(
    context: Context,
    delay: Long,
    requestCode: Int,
    intentDetails: Intent.() -> Unit,
) {
    Logger.d("Alarms", "Setting exact alarm in ${delay}ms")
    setExactAndAllowWhileIdle(
        AlarmManager.ELAPSED_REALTIME,
        SystemClock.elapsedRealtime() + delay,
        PendingIntent.getBroadcast(
            context, requestCode,
            Intent(context, AlarmReceiver::class.java).apply(intentDetails),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        ),
    )
}
