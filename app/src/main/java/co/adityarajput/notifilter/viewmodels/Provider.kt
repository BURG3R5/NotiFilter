package co.adityarajput.notifilter.viewmodels

import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import co.adityarajput.notifilter.NotiFilterApplication

object Provider {
    val Factory = viewModelFactory {
        initializer {
            FiltersViewModel(
                notifilterApplication().container.repository,
                notifilterApplication().packageManager,
            )
        }
        initializer {
            NotificationsViewModel(notifilterApplication().container.repository)
        }
    }
}

fun CreationExtras.notifilterApplication(): NotiFilterApplication =
    (this[AndroidViewModelFactory.APPLICATION_KEY] as NotiFilterApplication)
