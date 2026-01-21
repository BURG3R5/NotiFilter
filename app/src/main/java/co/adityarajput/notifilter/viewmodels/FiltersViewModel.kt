package co.adityarajput.notifilter.viewmodels

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.adityarajput.notifilter.data.Repository
import co.adityarajput.notifilter.data.filter.Filter
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
            Log.d("FiltersViewModel", "Toggling history for $selectedFilter")
            repository.toggleHistory(selectedFilter!!)
        }
    }

    fun toggleFilter() {
        viewModelScope.launch {
            Log.d("FiltersViewModel", "Toggling enabled state of $selectedFilter")
            repository.toggleEnabled(selectedFilter!!)
        }
    }

    fun deleteFilter() {
        viewModelScope.launch {
            Log.d("FiltersViewModel", "Deleting $selectedFilter")
            repository.delete(selectedFilter!!)
        }
    }
}

enum class DialogState { TOGGLE_HISTORY, TOGGLE_FILTER, DELETE }
