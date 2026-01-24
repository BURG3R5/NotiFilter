package co.adityarajput.notifilter.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.adityarajput.notifilter.data.Repository
import co.adityarajput.notifilter.data.models.Filter
import co.adityarajput.notifilter.utils.Logger
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FiltersViewModel(private val repository: Repository) : ViewModel() {
    data class State(val filters: List<Filter>? = null)

    val state: StateFlow<State> = repository.filters()
        .map { State(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), State())

    var dialogState by mutableStateOf<DialogState?>(null)

    var selectedFilter by mutableStateOf<Filter?>(null)

    fun toggleHistory() {
        viewModelScope.launch {
            Logger.d("FiltersViewModel.toggleHistory", "Toggling history for $selectedFilter")
            repository.toggleHistory(selectedFilter!!)
        }
    }

    fun toggleFilter() {
        viewModelScope.launch {
            Logger.d("FiltersViewModel.toggleFilter", "Toggling enabled state of $selectedFilter")
            repository.toggleEnabled(selectedFilter!!)
        }
    }

    fun deleteFilter() {
        viewModelScope.launch {
            Logger.d("FiltersViewModel.deleteFilter", "Deleting $selectedFilter")
            repository.delete(selectedFilter!!)
        }
    }
}

enum class DialogState { TOGGLE_HISTORY, TOGGLE_FILTER, DELETE }
