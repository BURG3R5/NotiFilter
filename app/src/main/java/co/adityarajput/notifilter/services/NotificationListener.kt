package co.adityarajput.notifilter.services

import android.app.AlarmManager
import android.app.Notification.FLAG_GROUP_SUMMARY
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.media.AudioManager
import android.os.Build
import android.os.Build.VERSION_CODES.VANILLA_ICE_CREAM
import android.provider.Settings
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.core.app.NotificationCompat
import co.adityarajput.notifilter.Constants
import co.adityarajput.notifilter.R
import co.adityarajput.notifilter.data.AppContainer
import co.adityarajput.notifilter.data.Cache
import co.adityarajput.notifilter.data.models.*
import co.adityarajput.notifilter.utils.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit.MILLIS
import kotlin.math.min
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class NotificationListener : NotificationListenerService() {
    companion object {
        @Volatile
        private var _instance: NotificationListener? = null

        var instance: NotificationListener
            get() = _instance ?: throw IllegalStateException("NotificationListener not initialized")
            private set(value) {
                _instance = value
            }

        val isServiceInitialized get() = _instance != null

        const val NOTIFICATION_SOUND_DURATION = 3000L

        fun createAlertNotificationChannel() {
            if (instance.notificationManager.getNotificationChannel(Constants.ALERT_NOTIFICATION_CHANNEL_ID) == null) {
                instance.notificationManager.createNotificationChannel(
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

        fun createReplaceNotificationChannel(filterId: Int, openSettings: Boolean = false) {
            val channelId = Constants.getReplaceNotificationChannelId(filterId)
            if (instance.notificationManager.getNotificationChannel(channelId) == null) {
                instance.notificationManager.createNotificationChannel(
                    NotificationChannel(
                        channelId,
                        "NotiFilter Replace Notifications for Filter #$filterId",
                        NotificationManager.IMPORTANCE_HIGH,
                    ).apply {
                        description = "Required for REPLACE Actions"
                    },
                )
            }
            if (openSettings) {
                instance.startActivity(
                    Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS)
                        .putExtra(Settings.EXTRA_APP_PACKAGE, instance.packageName)
                        .putExtra(Settings.EXTRA_CHANNEL_ID, channelId)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                )
            }
        }

        fun updateForegroundStatus(runInForeground: Boolean): Boolean {
            if (!isServiceInitialized) {
                Logger.i(
                    "NotificationListener",
                    "Skipping foreground toggle because service is not initialized",
                )
                return false
            }

            if (runInForeground) {
                instance.startForeground()
            } else {
                instance.stopForeground(STOP_FOREGROUND_REMOVE)
            }

            return true
        }
    }

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.Default + serviceJob)

    private val repository by lazy { AppContainer(this).repository }
    private val sharedPreferences by lazy { getSharedPreferences(Constants.SETTINGS, MODE_PRIVATE) }

    private val audioManager by lazy { getSystemService(AUDIO_SERVICE) as AudioManager }
    private val notificationManager by lazy { getSystemService(NOTIFICATION_SERVICE) as NotificationManager }
    private val alarmManager by lazy { getSystemService(ALARM_SERVICE) as AlarmManager }

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
            "Received $sbn with extras ${sbn.notification.extras.printable}",
        )
        val notification = Notification(sbn)
        val intents = Intents(sbn)
        Logger.d("NotificationListener", "Received $notification")

        val filter = filters.filter {
            (notification.origin == it.app.packageName || it.app == Any)
                    && it.enabled
                    && it.schedule.includesNow()
                    && it.matchesTextOf(notification)
        }.minByOrNull { it.priority } ?: return

        Logger.i("NotificationListener", "Matched $filter")

        when (filter.action) {
            is Action.DISMISS -> dismissNotification(sbn.key, sbn.isClearable)

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
                        filter.action.buttonRegex.containsMatchIn(it.key)
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
                    now.toLocalDate()
                        .plusDays(if (filter.schedule.end < filter.schedule.start) 1 else 0)
                        .atStartOfDay(zone)
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
                delay(300.milliseconds)
                Logger.i("NotificationListener", "Muting")
                requestListenerHints(HINT_HOST_DISABLE_NOTIFICATION_EFFECTS)
                delay(NOTIFICATION_SOUND_DURATION.milliseconds)
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
                    delay(2.seconds)
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

            is Action.DISMISS_STALE -> {
                serviceScope.launch {
                    var retentionLength = filter.action.retentionLength * 60 * 1000L
                    if (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0) {
                        // INFO: While debugging, retain for shorter intervals
                        retentionLength /= 5
                    }

                    alarmManager.sendIntent(
                        this@NotificationListener,
                        retentionLength,
                        sbn.key.hashCode(),
                    ) {
                        action = Constants.ACTION_DISMISS_STALE
                        putExtra(Constants.EXTRA_SBN_KEY, sbn.key)
                        putExtra(Constants.EXTRA_SBN_IS_CLEARABLE, sbn.isClearable)
                    }
                }
            }

            is Action.REPLACE -> {
                createReplaceNotificationChannel(filter.id)
                val allPackages = Cache.getAllPackages(packageManager)

                cancelNotification(sbn.key)
                notificationManager.notify(
                    Constants.getReplaceNotificationId(filter.id),
                    NotificationCompat
                        .Builder(this, Constants.getReplaceNotificationChannelId(filter.id))
                        .setContentTitle(
                            filter.action.titleTemplate.replaceWithNotificationData(
                                notification, allPackages,
                            ),
                        )
                        .setContentText(
                            filter.action.contentTemplate.replaceWithNotificationData(
                                notification, allPackages,
                            ),
                        )
                        .setSmallIcon(R.drawable.ic_launcher_foreground)
                        .setContentIntent(sbn.notification.contentIntent)
                        .build(),
                )
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

    fun dismissNotification(key: String, isClearable: Boolean) {
        if (isClearable) {
            try {
                Logger.d("NotificationListener", "Canceling")
                cancelNotification(key)
            } catch (e: Throwable) {
                Logger.e("NotificationListener", "Failed to dismiss notification", e)
            }
        } else {
            Logger.d("NotificationListener", "Snoozing")
            snoozeNotification(key, 5 * 60 * 60 * 1000L)
        }
    }

    private fun muteNotificationsWhileCooldown(filter: Filter) {
        serviceScope.launch {
            delay(NOTIFICATION_SOUND_DURATION.milliseconds) // INFO: Wait for original notification sound
            Logger.d("NotificationListener", "Applying cooldown")
            requestListenerHints(HINT_HOST_DISABLE_NOTIFICATION_EFFECTS)
            try {
                while (true) {
                    val cooldownEnd = cooldowns[filter.id] ?: break
                    if (System.currentTimeMillis() > cooldownEnd) break
                    delay(500.milliseconds)
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
                    delay(500.milliseconds)
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
        if (instance == this) _instance = null
        Logger.i("NotificationListener", "Listener disconnected")
    }

    override fun onDestroy() {
        super.onDestroy()
        if (sharedPreferences.getBoolean(Constants.RUN_IN_FOREGROUND, false))
            stopForeground(STOP_FOREGROUND_REMOVE)
        serviceJob.cancel()
    }
}
