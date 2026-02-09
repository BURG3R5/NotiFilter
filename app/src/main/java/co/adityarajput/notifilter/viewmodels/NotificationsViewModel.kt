package co.adityarajput.notifilter.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.adityarajput.notifilter.data.Repository
import co.adityarajput.notifilter.data.models.Notification
import co.adityarajput.notifilter.utils.Logger
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
