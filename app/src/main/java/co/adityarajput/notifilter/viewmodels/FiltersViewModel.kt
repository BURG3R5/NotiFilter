package co.adityarajput.notifilter.viewmodels

import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.adityarajput.notifilter.data.filter.Filter
import co.adityarajput.notifilter.data.filter.FiltersRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class FiltersState(val filters: List<Filter>? = null)

class FiltersViewModel(private val filtersRepository: FiltersRepository) : ViewModel() {
    val filtersState: StateFlow<FiltersState> =
        filtersRepository.list()
            .map { FiltersState(it) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FiltersState())

    var formState by mutableStateOf(FormState())
        private set

    fun getPackages(packageManager: PackageManager): List<Pair<String, String>> {
        val packages = packageManager.queryIntentActivities(
            Intent(Intent.ACTION_MAIN, null).addCategory(Intent.CATEGORY_LAUNCHER),
            0,
        ).map {
            Pair(
                it.activityInfo.packageName,
                it.activityInfo.applicationInfo.loadLabel(packageManager).toString(),
            )
        }
        return packages.sortedBy { it.second }
    }

    fun onFormUpdate(inputValues: InputValues) {
        formState =
            FormState(inputValues, getError(inputValues))
    }

    suspend fun onFormSubmit() {
        if (getError() == null) {
            val filter = formState.inputValues.toFilter()
            Log.d("FiltersViewModel", "Adding $filter")
            filtersRepository.create(filter)
            formState = FormState()
        }
    }

    private fun getError(uiState: InputValues = formState.inputValues): FormError? {
        try {
            if (uiState.packageName.isBlank() || uiState.queryPattern.isBlank())
                return FormError.BLANK_FIELDS
            Regex(uiState.queryPattern).pattern == uiState.queryPattern
        } catch (_: Exception) {
            return FormError.INVALID_REGEX
        }
        return null
    }

    fun deleteFilter(filter: Filter) {
        viewModelScope.launch {
            Log.d("FiltersViewModel", "Deleting $filter")
            filtersRepository.delete(filter)
        }
    }
}

data class FormState(
    val inputValues: InputValues = InputValues(),
    val error: FormError? = FormError.BLANK_FIELDS,
)

data class InputValues(
    val packageName: String = "",
    val queryPattern: String = "",
)

enum class FormError { BLANK_FIELDS, INVALID_REGEX }

fun InputValues.toFilter() = Filter(0, packageName, queryPattern)
