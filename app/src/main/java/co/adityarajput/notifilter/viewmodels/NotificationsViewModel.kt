package co.adityarajput.notifilter.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.content.Context
import co.adityarajput.notifilter.data.Repository
import co.adityarajput.notifilter.data.models.Notification
import co.adityarajput.notifilter.utils.Logger
import co.adityarajput.notifilter.utils.NotificationCache
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NotificationsViewModel(val repository: Repository) : ViewModel() {
    data class State(val notifications: List<Notification>? = null)

    val state: StateFlow<State> =
        repository.notifications()
            .map { State(it) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), State())

    var selectedNotification by mutableStateOf<Notification?>(null)

    var dialogState by mutableStateOf<NotificationDialogState?>(null)

    fun openNotification(context: Context, notification: Notification) {
        val intent = NotificationCache.get(notification)
        if (intent != null) {
            try {
                intent.send()
                Logger.d("NotificationsViewModel", "Triggered cached intent for $notification")
                return
            } catch (e: Exception) {
                Logger.e("NotificationsViewModel", "Failed to send cached intent", e)
            }
        }

        // Fallback: Launch app main activity
        try {
            val launchIntent = context.packageManager.getLaunchIntentForPackage(notification.packageName)
            if (launchIntent != null) {
                context.startActivity(launchIntent)
                Logger.d("NotificationsViewModel", "Launched app for $notification")
            } else {
                Logger.d("NotificationsViewModel", "No launch intent for ${notification.packageName}")
            }
        } catch (e: Exception) {
            Logger.e("NotificationsViewModel", "Failed to launch app", e)
        }
    }

    fun delete(notification: Notification) {
        viewModelScope.launch {
            Logger.d("NotificationsViewModel", "Deleting $notification")
            repository.delete(notification)
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            Logger.d("NotificationsViewModel", "Deleting all notifications")
            repository.deleteNotifications()
        }
    }
}

enum class NotificationDialogState { CLEAR_HISTORY }
