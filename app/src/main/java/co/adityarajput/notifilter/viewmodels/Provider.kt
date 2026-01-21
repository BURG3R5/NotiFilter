package co.adityarajput.notifilter.viewmodels

import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import co.adityarajput.notifilter.NotiFilterApplication
import co.adityarajput.notifilter.data.filter.Filter
import kotlinx.serialization.json.Json

object Provider {
    val Factory = viewModelFactory {
        initializer { FiltersViewModel(notifilterApplication().container.repository) }
        initializer { NotificationsViewModel(notifilterApplication().container.repository) }
    }

    fun createUFVM(filterString: String) = viewModelFactory {
        initializer {
            UpsertFilterViewModel(
                Json.decodeFromString<Filter?>(filterString),
                notifilterApplication().container.repository,
                notifilterApplication().packageManager,
            )
        }
    }
}

fun CreationExtras.notifilterApplication(): NotiFilterApplication =
    (this[AndroidViewModelFactory.APPLICATION_KEY] as NotiFilterApplication)
