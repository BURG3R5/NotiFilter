package co.adityarajput.notifilter.services

import android.app.Notification.FLAG_GROUP_SUMMARY
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.pm.ApplicationInfo
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
import android.media.AudioManager
import android.os.Build
import android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE
import android.os.Build.VERSION_CODES.VANILLA_ICE_CREAM
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.core.app.NotificationCompat
import co.adityarajput.notifilter.Constants
import co.adityarajput.notifilter.R
import co.adityarajput.notifilter.data.AppContainer
import co.adityarajput.notifilter.data.Cache
import co.adityarajput.notifilter.data.models.*
import co.adityarajput.notifilter.utils.Logger
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit.MILLIS
import kotlin.math.min

class NotificationListener : NotificationListenerService() {
    companion object {
        @Volatile
        var instance: NotificationListener? = null

        const val NOTIFICATION_SOUND_DURATION = 3000L

        fun createAlertNotificationChannel() {
            val notificationManager = instance?.notificationManager ?: return
            if (notificationManager.getNotificationChannel(Constants.ALERT_NOTIFICATION_CHANNEL_ID) == null) {
                notificationManager.createNotificationChannel(
                    NotificationChannel(
                        Constants.ALERT_NOTIFICATION_CHANNEL_ID,
                        "NotiFilter Alert Service",
                        NotificationManager.IMPORTANCE_HIGH,
                    ).apply {
                        description = "Required for ALERT Actions"
                    },
                )
            }
        }

        fun updateForegroundStatus(runInForeground: Boolean) {
            if (runInForeground) {
                instance!!.startForeground()
            } else {
                instance!!.stopForeground(STOP_FOREGROUND_REMOVE)
            }
        }
    }

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.Default + serviceJob)

    private val repository by lazy { AppContainer(this).repository }
    private val sharedPreferences by lazy { getSharedPreferences(Constants.SETTINGS, MODE_PRIVATE) }

    private val audioManager by lazy { getSystemService(AUDIO_SERVICE) as AudioManager }
    private val notificationManager by lazy { getSystemService(NOTIFICATION_SERVICE) as NotificationManager }

    @Volatile
    private var filters: List<Filter> = emptyList()

    @Volatile
    private var notifications: List<Notification> = emptyList()

    @Volatile
    private var cooldowns: Map<Int, Long> = emptyMap()

    override fun onCreate() {
        super.onCreate()
        instance = this
        Logger.i("NotificationListener", "Service created")

        if (sharedPreferences.getBoolean(Constants.RUN_IN_FOREGROUND, false))
            startForeground()

        serviceScope.launch {
            repository.filters().collectLatest { newFilters ->
                filters = newFilters
                Logger.d("NotificationListener", "Filters updated: $filters")
            }
            notifications = repository.notifications().first()
        }
    }

    fun startForeground() {
        notificationManager.createNotificationChannel(
            NotificationChannel(
                Constants.FOREGROUND_NOTIFICATION_CHANNEL_ID,
                "NotiFilter Foreground Service",
                NotificationManager.IMPORTANCE_LOW,
            ).apply {
                description = "Required for foreground service"
                enableLights(false)
                enableVibration(false)
                setShowBadge(false)
                setSound(null, null)
            },
        )
        startForeground(
            Constants.FOREGROUND_NOTIFICATION_ID,
            NotificationCompat.Builder(this, Constants.FOREGROUND_NOTIFICATION_CHANNEL_ID)
                .setContentTitle(getString(R.string.app_name_launcher))
                .setContentText(getString(R.string.foreground_notification_content))
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setOngoing(true).setSilent(true).build(),
            if (Build.VERSION.SDK_INT >= UPSIDE_DOWN_CAKE) FOREGROUND_SERVICE_TYPE_SPECIAL_USE else 0,
        )
        Logger.i("NotificationListener", "Promoted to foreground")
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        Logger.i("NotificationListener", "Listener connected")
        requestListenerHints(0)
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        if (sbn.notification.flags and FLAG_GROUP_SUMMARY != 0) {
            Logger.d(
                "NotificationListener",
                "Ignoring group summary notification $sbn",
            )
            return
        }

        Logger.d(
            "NotificationListener",
            "Received $sbn with extras ${sbn.notification.extras}",
        )
        val notification = Notification(sbn)
        val intents = Intents(sbn)
        Logger.d("NotificationListener", "Received $notification")

        val filter = filters.filter {
            (notification.origin == it.app.packageName || it.app == Any)
                    && it.enabled
                    && it.schedule.includesNow()
                    && it.matchesTextOf(notification)
        }.minByOrNull { it.id } ?: return

        Logger.i("NotificationListener", "Matched $filter")

        when (filter.action) {
            is Action.DISMISS ->
                if (sbn.isClearable) {
                    try {
                        cancelNotification(sbn.key)
                    } catch (e: Throwable) {
                        Logger.e(
                            "NotificationListener",
                            "Failed to dismiss notification",
                            e,
                        )
                    }
                } else {
                    Logger.d("NotificationListener", "Is unclearable")
                    snoozeNotification(sbn.key, 5 * 60 * 60 * 1000L)
                }

            is Action.TAP_NOTIFICATION ->
                try {
                    intents.launchMain()
                } catch (e: Exception) {
                    Logger.e("NotificationListener", "Failed to tap notification", e)
                    return
                }

            is Action.TAP_BUTTON ->
                try {
                    intents.actions.entries.find {
                        Regex(filter.action.buttonRegex).containsMatchIn(it.key)
                    }?.value?.send()
                } catch (e: Exception) {
                    Logger.e("NotificationListener", "Failed to tap button", e)
                    return
                }

            is Action.BATCH -> {
                val zone = ZoneId.systemDefault()
                val now = ZonedDateTime.now(zone)
                val today = now.toLocalDate().atStartOfDay(zone)
                var batchLength = filter.action.batchLength * 60 * 60 * 1000L
                if (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0) {
                    // INFO: While debugging, batch for minutes instead of hours
                    batchLength /= 60
                }

                val untilNextBatch = batchLength - today.until(now, MILLIS) % batchLength

                if ((untilNextBatch < 1000L) || (batchLength - untilNextBatch < 1000L)) {
                    Logger.d("NotificationListener", "Less than 1 second to batch boundary")
                } else if (notifications.any { it.data == notification.data }) {
                    Logger.d("NotificationListener", "Already snoozed")
                } else {
                    snoozeNotification(
                        sbn.key,
                        min(untilNextBatch, now.until(today.plusDays(1), MILLIS)),
                    )
                }
            }

            is Action.DELAY -> {
                val zone = ZoneId.systemDefault()
                val now = ZonedDateTime.now(zone)
                val delay = now.until(
                    now.toLocalDate().atStartOfDay(zone)
                        .plusMinutes(filter.schedule.end.toLong()),
                    MILLIS,
                )

                if (delay < 1000L) {
                    Logger.d("NotificationListener", "Less than 1 second until filter deactivation")
                } else {
                    snoozeNotification(sbn.key, delay)
                }
            }

            is Action.DEBOUNCE -> {
                if (!cooldowns.containsKey(filter.id)) {
                    Logger.d("NotificationListener", "Setting cooldown")
                    cooldowns += filter.id to (System.currentTimeMillis() + filter.action.cooldownLength * 60 * 1000L)
                    muteNotificationsWhileCooldown(filter)
                } else {
                    Logger.i("NotificationListener", "Updating cooldown")
                    cooldowns += filter.id to (System.currentTimeMillis() + filter.action.cooldownLength * 60 * 1000L)
                }
            }

            is Action.MUTE -> serviceScope.launch {
                delay(300L)
                Logger.i("NotificationListener", "Muting")
                requestListenerHints(HINT_HOST_DISABLE_NOTIFICATION_EFFECTS)
                delay(NOTIFICATION_SOUND_DURATION)
                Logger.d("NotificationListener", "Unmuting")
                requestListenerHints(0)
            }

            is Action.ALERT -> {
                notificationManager.notify(
                    Constants.ALERT_NOTIFICATION_ID,
                    NotificationCompat.Builder(this, Constants.ALERT_NOTIFICATION_CHANNEL_ID)
                        .setContentTitle(getString(R.string.app_name_launcher))
                        .setSmallIcon(R.drawable.ic_launcher_foreground)
                        .setAutoCancel(true).build(),
                )
                serviceScope.launch {
                    delay(2000L)
                    notificationManager.cancel(Constants.ALERT_NOTIFICATION_ID)
                }
            }

            is Action.DISTURB -> {
                if (!cooldowns.containsKey(filter.id) && audioManager.ringerMode != AudioManager.RINGER_MODE_NORMAL) {
                    Logger.d("NotificationListener", "Disabling DND")
                    cooldowns += filter.id to (System.currentTimeMillis() + filter.action.pauseLength * 60 * 1000L)
                    snoozeNotification(sbn.key, 100)
                    disableDNDWhileCooldown(filter)
                } else {
                    Logger.i("NotificationListener", "Extending disturbance")
                    cooldowns += filter.id to (System.currentTimeMillis() + filter.action.pauseLength * 60 * 1000L)
                }
            }
        }

        serviceScope.launch {
            repository.registerHit(
                filter,
                notification.copy(
                    showInHistory = filter.historyEnabled,
                    showInWidget = filter.widgetEnabled,
                ),
            )
            Cache.intents[notification.data.hashCode()] = intents
            notifications = repository.notifications().first()
            Logger.d("NotificationListener", "Notifications updated: $notifications")
        }
    }

    private fun muteNotificationsWhileCooldown(filter: Filter) {
        serviceScope.launch {
            delay(NOTIFICATION_SOUND_DURATION) // INFO: Wait for original notification sound
            Logger.d("NotificationListener", "Applying cooldown")
            requestListenerHints(HINT_HOST_DISABLE_NOTIFICATION_EFFECTS)
            try {
                while (true) {
                    val cooldownEnd = cooldowns[filter.id] ?: break
                    if (System.currentTimeMillis() > cooldownEnd) break
                    delay(500L)
                }
            } finally {
                Logger.d("NotificationListener", "Cooldown ended")
                cooldowns -= filter.id
                requestListenerHints(0)
            }
        }
    }

    private fun disableDNDWhileCooldown(filter: Filter) {
        val originalInterruptionFilter = notificationManager.currentInterruptionFilter
        val originalRingerMode = audioManager.ringerMode

        audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL)
        if (Build.VERSION.SDK_INT <= VANILLA_ICE_CREAM) {
            notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
        }

        serviceScope.launch {
            try {
                while (true) {
                    val cooldownEnd = cooldowns[filter.id] ?: break
                    if (System.currentTimeMillis() > cooldownEnd) break
                    delay(500L)
                }
            } finally {
                Logger.d("NotificationListener", "Disturbance ended")
                cooldowns -= filter.id

                audioManager.ringerMode = originalRingerMode
                if (Build.VERSION.SDK_INT <= VANILLA_ICE_CREAM) {
                    notificationManager.setInterruptionFilter(originalInterruptionFilter)
                }
            }
        }
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        if (instance == this) instance = null
        Logger.i("NotificationListener", "Listener disconnected")
    }

    override fun onDestroy() {
        super.onDestroy()
        if (sharedPreferences.getBoolean(Constants.RUN_IN_FOREGROUND, false))
            stopForeground(STOP_FOREGROUND_REMOVE)
        serviceJob.cancel()
    }
}
