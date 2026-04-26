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

    var dialogState by mutableStateOf<FilterDialogState?>(null)

    var selectedFilter by mutableStateOf<Filter?>(null)

    fun normalizePriorities() {
        viewModelScope.launch {
            Logger.d("FiltersViewModel", "Normalizing filter priorities")
            state.value.filters!!.forEachIndexed { index, filter ->
                repository.upsert(filter.copy(priority = index))
            }
        }
    }

    fun updatePriority(filter: Filter, increase: Boolean) {
        viewModelScope.launch {
            Logger.d("FiltersViewModel", "Updating priority of $selectedFilter")
            if (increase && filter.priority > 0) {
                val filterAbove = state.value.filters!!.first { it.priority == filter.priority - 1 }
                repository.upsert(
                    filterAbove.copy(priority = filter.priority),
                    filter.copy(priority = filter.priority - 1),
                )
            } else if (!increase && filter.priority < state.value.filters!!.size - 1) {
                val filterBelow = state.value.filters!!.first { it.priority == filter.priority + 1 }
                repository.upsert(
                    filterBelow.copy(priority = filter.priority),
                    filter.copy(priority = filter.priority + 1),
                )
            }
        }
    }

    fun toggleHistory() {
        viewModelScope.launch {
            Logger.d("FiltersViewModel", "Toggling history for $selectedFilter")
            repository.toggleHistory(selectedFilter!!)
        }
    }

    fun toggleFilter() {
        viewModelScope.launch {
            Logger.d("FiltersViewModel", "Toggling enabled state of $selectedFilter")
            repository.toggleEnabled(selectedFilter!!)
        }
    }

    fun deleteFilter() {
        viewModelScope.launch {
            Logger.d("FiltersViewModel", "Deleting $selectedFilter")
            repository.delete(selectedFilter!!)
        }
    }
}

enum class FilterDialogState { TOGGLE_HISTORY, TOGGLE_FILTER, DELETE }
