package co.adityarajput.notifilter.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.adityarajput.notifilter.data.notification.Notification
import co.adityarajput.notifilter.data.notification.NotificationsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class NotificationsState(val notifications: List<Notification>? = null)

class NotificationsViewModel(private val notificationsRepository: NotificationsRepository) :
    ViewModel() {
    val notificationsState: StateFlow<NotificationsState> =
        notificationsRepository.list()
            .map { NotificationsState(it) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), NotificationsState())

}
