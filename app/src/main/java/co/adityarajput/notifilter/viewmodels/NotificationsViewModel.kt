package co.adityarajput.notifilter.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.adityarajput.notifilter.data.Repository
import co.adityarajput.notifilter.data.notification.Notification
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class NotificationsViewModel(repository: Repository) : ViewModel() {
    data class State(val notifications: List<Notification>? = null)

    val state: StateFlow<State> =
        repository.notifications()
            .map { State(it) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), State())
}
