package co.adityarajput.notificationhelper

import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent.*
import android.content.Intent
import android.os.Bundle
import androidx.core.app.NotificationCompat

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        getSystemService(NotificationManager::class.java).apply {
            createNotificationChannel(
                NotificationChannel(
                    "default_channel",
                    "Default",
                    NotificationManager.IMPORTANCE_DEFAULT,
                ),
            )
            notify(
                intent.action.hashCode(),
                NotificationCompat.Builder(this@MainActivity, "default_channel")
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setAutoCancel(true)
                    .setContentTitle(intent.action?.last().toString())
                    .setContentIntent(
                        getActivity(
                            this@MainActivity,
                            0,
                            Intent(this@MainActivity, MainActivity::class.java),
                            FLAG_UPDATE_CURRENT or FLAG_IMMUTABLE,
                        ),
                    ).build(),
            )
        }

        finish()
    }
}
