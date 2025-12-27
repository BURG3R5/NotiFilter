package co.adityarajput.notifilter.viewmodels

import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.adityarajput.notifilter.data.filter.Action
import co.adityarajput.notifilter.data.filter.Filter
import co.adityarajput.notifilter.data.filter.FiltersRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class FiltersState(val filters: List<Filter>? = null)

class FiltersViewModel : ViewModel {
    private val repository: FiltersRepository

    val filtersState: StateFlow<FiltersState>

    var showAddDialog by mutableStateOf(false)

    var visibleApps: List<Pair<String, String>> = emptyList()
        private set

    var allPackages: List<Pair<String, String>> = emptyList()
        private set

    var formState by mutableStateOf(FormState())

    var selectedFilter by mutableStateOf<Filter?>(null)

    constructor(filtersRepository: FiltersRepository, packageManager: PackageManager) : super() {
        repository = filtersRepository

        filtersState = filtersRepository.list()
            .map { FiltersState(it) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FiltersState())

        viewModelScope.launch {
            visibleApps = packageManager.queryIntentActivities(
                Intent(Intent.ACTION_MAIN, null).addCategory(Intent.CATEGORY_LAUNCHER),
                0,
            ).map {
                Pair(
                    it.activityInfo.packageName,
                    it.activityInfo.applicationInfo.loadLabel(packageManager).toString(),
                )
            }.sortedBy { it.second }
            allPackages = packageManager.getInstalledApplications(0).map {
                Pair(
                    it.packageName,
                    it.loadLabel(packageManager).toString(),
                )
            }.sortedBy { it.second }
        }
    }

    fun updateForm(page: FormPage, values: FormValues) {
        formState = FormState(page, values, getError(page, values))
    }

    suspend fun submitForm() {
        if (getError() == null) {
            val filter = formState.values.toFilter()
            Log.d("FiltersViewModel", "Adding $filter")
            repository.create(filter)
            formState = FormState()
        }
    }

    private fun getError(
        page: FormPage = formState.page,
        values: FormValues = formState.values,
    ): FormError? {
        when (page) {
            FormPage.PACKAGE -> if (values.packageName.isBlank()) return FormError.BLANK_FIELDS

            FormPage.PATTERN -> {
                if (values.queryPattern.isBlank()) return FormError.BLANK_FIELDS
                try {
                    Regex(values.queryPattern).pattern == values.queryPattern
                } catch (_: Exception) {
                    return FormError.INVALID_NOTIFICATION_REGEX
                }
            }

            FormPage.ACTION -> {
                if (values.action == Action.DISMISS) return null
                try {
                    Regex(values.buttonPattern).pattern == values.buttonPattern
                } catch (_: Exception) {
                    Log.d("FiltersViewModel", "Button pattern regex invalid")
                    return FormError.INVALID_BUTTON_REGEX
                }
            }
        }
        return null
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

data class FormState(
    val page: FormPage = FormPage.PACKAGE,
    val values: FormValues = FormValues(),
    val error: FormError? = FormError.BLANK_FIELDS,
)

enum class FormPage {
    PACKAGE, PATTERN, ACTION;

    fun isFinalPage() = this == ACTION
}

data class FormValues(
    val packageName: String = "",
    val queryPattern: String = "",
    val action: Action = Action.DISMISS,
    val buttonPattern: String = "",
)

fun FormValues.toFilter() = Filter(packageName, queryPattern, action, buttonPattern)

enum class FormError { BLANK_FIELDS, INVALID_NOTIFICATION_REGEX, INVALID_BUTTON_REGEX }
