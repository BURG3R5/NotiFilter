package co.adityarajput.notifilter.viewmodels

import android.content.pm.PackageManager
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.adityarajput.notifilter.data.Cache
import co.adityarajput.notifilter.data.Repository
import co.adityarajput.notifilter.data.models.App
import co.adityarajput.notifilter.data.models.Notification
import co.adityarajput.notifilter.utils.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NotificationsViewModel(
    val repository: Repository,
    packageManager: PackageManager,
) : ViewModel() {
    data class State(val notifications: List<Notification>? = null)

    val state: StateFlow<State> =
        repository.history()
            .map { State(it) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), State())

    var allPackages by mutableStateOf<List<App>>(emptyList())

    var dialogState by mutableStateOf<NotificationDialogState?>(null)

    init {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                allPackages = Cache.getAllPackages(packageManager)
            }
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
