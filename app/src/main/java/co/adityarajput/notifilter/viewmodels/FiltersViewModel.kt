package co.adityarajput.notifilter.viewmodels

import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.adityarajput.notifilter.data.Repository
import co.adityarajput.notifilter.data.filter.Action
import co.adityarajput.notifilter.data.filter.Filter
import co.adityarajput.notifilter.data.filter.RegexTarget
import co.adityarajput.notifilter.data.notification.Notification
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FiltersViewModel(
    private val repository: Repository,
    packageManager: PackageManager,
) : ViewModel() {
    data class State(val filters: List<Filter>? = null)

    val state: StateFlow<State> = repository.filters()
        .map { State(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), State())

    val visibleApps: List<Pair<String, String>> by lazy {
        packageManager
            .queryIntentActivities(
                Intent(
                    Intent.ACTION_MAIN,
                    null,
                ).addCategory(Intent.CATEGORY_LAUNCHER),
                0,
            )
            .map {
                Pair(
                    it.activityInfo.packageName,
                    it.activityInfo.applicationInfo.loadLabel(packageManager).toString(),
                )
            }
            .sortedBy { it.second }
    }

    val allPackages: List<Pair<String, String>> by lazy {
        packageManager.getInstalledApplications(0)
            .map { Pair(it.packageName, it.loadLabel(packageManager).toString()) }
            .sortedBy { it.second }
    }

    var showAddDialog by mutableStateOf(false)

    var formState by mutableStateOf(FormState())

    var dialogState by mutableStateOf<DialogState?>(null)

    var selectedFilter by mutableStateOf<Filter?>(null)

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
            FormPage.ZAPPER -> return null

            FormPage.PACKAGE -> if (values.packageName.isBlank()) return FormError.BLANK_FIELDS

            FormPage.PATTERN -> {
                if (values.queryPattern.isBlank()) return FormError.BLANK_FIELDS
                try {
                    Regex(values.queryPattern).pattern == values.queryPattern
                    if (values.regexTarget == RegexTarget.AND) {
                        if (values.secondaryQueryPattern.isBlank()) return FormError.BLANK_FIELDS
                        Regex(values.secondaryQueryPattern).pattern == values.secondaryQueryPattern
                    }
                } catch (_: Exception) {
                    return FormError.INVALID_NOTIFICATION_REGEX
                }
            }

            FormPage.ACTION -> {
                when (values.action) {
                    Action.TAP -> try {
                        Regex(values.buttonPattern).pattern == values.buttonPattern
                    } catch (_: Exception) {
                        Log.d("FiltersViewModel", "Button pattern regex invalid")
                        return FormError.INVALID_BUTTON_REGEX
                    }

                    else -> {}
                }
            }

            FormPage.TIME -> {
                if (values.activeDays.isEmpty()) return FormError.BLANK_FIELDS
                if (values.activeTime.first < 0 || values.activeTime.second > 1439 ||
                    values.activeTime.first >= values.activeTime.second
                ) {
                    return FormError.INVALID_TIME_RANGE
                }
            }
        }
        return null
    }

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

data class FormState(
    val page: FormPage = FormPage.ZAPPER,
    val values: FormValues = FormValues(),
    val error: FormError? = null,
)

enum class FormPage {
    ZAPPER, PACKAGE, PATTERN, ACTION, TIME;

    fun isFinalPage() = this == TIME

    fun nextPage() = when (this) {
        ZAPPER -> PACKAGE
        PACKAGE -> PATTERN
        PATTERN -> ACTION
        ACTION -> TIME
        TIME -> TIME
    }
}

data class FormValues(
    val notification: Notification? = null,
    val packageName: String = "",
    val queryPattern: String = "",
    val secondaryQueryPattern: String = "",
    val regexTarget: RegexTarget = RegexTarget.OR,
    val action: Action = Action.DISMISS,
    val buttonPattern: String = "",
    val batchLengthInHours: Int = 3,
    val activeTime: Pair<Int, Int> = 0 to 1439,
    val activeDays: Set<Int> = setOf(1, 2, 3, 4, 5, 6, 7),
)

fun FormValues.toFilter() =
    Filter(
        packageName,
        queryPattern,
        if (regexTarget == RegexTarget.AND) secondaryQueryPattern else null,
        regexTarget,
        action,
        if (action == Action.TAP) buttonPattern else null,
        if (action == Action.BATCH) batchLengthInHours else null,
        activeTime,
        activeDays,
    )

enum class FormError { BLANK_FIELDS, INVALID_NOTIFICATION_REGEX, INVALID_BUTTON_REGEX, INVALID_TIME_RANGE }

enum class DialogState { TOGGLE_HISTORY, TOGGLE_FILTER, DELETE }
