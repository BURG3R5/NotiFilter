package co.adityarajput.notificationhelper

import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
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
                    .setContentTitle(intent.action?.last().toString()).build(),
            )
        }

        finish()
    }
}
