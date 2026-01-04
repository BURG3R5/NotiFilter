package co.adityarajput.notifilter.viewmodels

import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.adityarajput.notifilter.Constants
import co.adityarajput.notifilter.data.active_notification.ActiveNotification
import co.adityarajput.notifilter.data.active_notification.ActiveNotificationsRepository
import co.adityarajput.notifilter.data.filter.Action
import co.adityarajput.notifilter.data.filter.Filter
import co.adityarajput.notifilter.data.filter.FiltersRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class FiltersState(val filters: List<Filter>? = null)

data class ActiveNotificationsState(val value: List<ActiveNotification>? = null)

class FiltersViewModel : ViewModel {
    private val filtersRepository: FiltersRepository

    private val activeNotificationsRepository: ActiveNotificationsRepository

    private val sharedPreferences: SharedPreferences

    val filtersState: StateFlow<FiltersState>

    val activeNotificationsState: StateFlow<ActiveNotificationsState>

    var showAddDialog by mutableStateOf(false)

    var visibleApps: List<Pair<String, String>> = emptyList()
        private set

    var allPackages: List<Pair<String, String>> = emptyList()
        private set

    var formState by mutableStateOf(FormState())

    var isDoneWithZapper by mutableStateOf(false)

    var dialogState by mutableStateOf<DialogState?>(null)

    var selectedFilter by mutableStateOf<Filter?>(null)

    constructor(
        filtersRepository: FiltersRepository,
        activeNotificationsRepository: ActiveNotificationsRepository,
        packageManager: PackageManager,
        sharedPreferences: SharedPreferences,
    ) : super() {
        this.filtersRepository = filtersRepository
        this.activeNotificationsRepository = activeNotificationsRepository
        this.sharedPreferences = sharedPreferences

        filtersState = filtersRepository.list()
            .map { FiltersState(it) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FiltersState())

        activeNotificationsState = activeNotificationsRepository.list()
            .map { ActiveNotificationsState(it) }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                ActiveNotificationsState(),
            )

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

    fun ensureCorrectInitialFormPage() {
        if (isDoneWithZapper) return

        val isStoringActiveNotifications =
            sharedPreferences.getBoolean(Constants.STORE_ACTIVE_NOTIFICATIONS, false)

        if (isStoringActiveNotifications && formState.page != FormPage.ZAPPER) {
            formState = formState.copy(page = FormPage.ZAPPER)
        } else if (!isStoringActiveNotifications && formState.page != FormPage.PACKAGE) {
            formState = formState.copy(page = FormPage.PACKAGE)
        }
    }

    fun updateForm(page: FormPage, values: FormValues) {
        formState = FormState(page, values, getError(page, values))
    }

    suspend fun submitForm() {
        if (getError() == null) {
            val filter = formState.values.toFilter()
            Log.d("FiltersViewModel", "Adding $filter")
            filtersRepository.create(filter)
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
            filtersRepository.toggleHistory(selectedFilter!!)
        }
    }

    fun toggleFilter() {
        viewModelScope.launch {
            Log.d("FiltersViewModel", "Toggling enabled state of $selectedFilter")
            filtersRepository.toggleEnabled(selectedFilter!!)
        }
    }

    fun deleteFilter() {
        viewModelScope.launch {
            Log.d("FiltersViewModel", "Deleting $selectedFilter")
            filtersRepository.delete(selectedFilter!!)
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
    val packageName: String = "",
    val queryPattern: String = "",
    val action: Action = Action.DISMISS,
    val buttonPattern: String = "",
    val activeTime: Pair<Int, Int> = 0 to 1439,
    val activeDays: Set<Int> = setOf(1, 2, 3, 4, 5, 6, 7),
)

fun FormValues.toFilter() =
    Filter(packageName, queryPattern, action, buttonPattern, activeTime, activeDays)

enum class FormError { BLANK_FIELDS, INVALID_NOTIFICATION_REGEX, INVALID_BUTTON_REGEX, INVALID_TIME_RANGE }

enum class DialogState { TOGGLE_HISTORY, TOGGLE_FILTER, DELETE }
