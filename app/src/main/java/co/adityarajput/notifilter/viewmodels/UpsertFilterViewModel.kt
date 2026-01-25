package co.adityarajput.notifilter.viewmodels

import android.app.Notification.FLAG_GROUP_SUMMARY
import android.content.Intent
import android.content.pm.PackageManager
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.adityarajput.notifilter.R
import co.adityarajput.notifilter.data.Repository
import co.adityarajput.notifilter.data.models.*
import co.adityarajput.notifilter.services.NotificationListener
import co.adityarajput.notifilter.utils.Logger
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class UpsertFilterViewModel(
    filter: Filter?,
    private val repository: Repository,
    packageManager: PackageManager,
) : ViewModel() {
    data class State(
        val page: FormPage = FormPage.ZAPPER,
        val values: Values = Values(),
        val error: FormError? = null,
        val warnings: List<FormWarning> = listOf(),
    )

    data class Values(
        val filterId: Int = 0,
        val notification: Notification? = null,
        val app: App = App("", ""),
        val regexTarget: RegexTarget = RegexTarget.OR,
        val queryPattern: String = "",
        val secondaryQueryPattern: String = "",
        val action: Action = Action.DISMISS,
        val schedule: Schedule = Schedule(),
    ) {
        constructor(filter: Filter) : this(
            filter.id,
            null,
            filter.app,
            filter.regexTarget,
            filter.regexPattern,
            filter.secondaryRegexPattern ?: "",
            filter.action,
            filter.schedule,
        )

        fun toFilter() = Filter(
            app, queryPattern, action, regexTarget,
            if (regexTarget == RegexTarget.AND) secondaryQueryPattern else null, schedule,
            id = filterId,
        )
    }

    var state by mutableStateOf(
        if (filter == null) State()
        else State(FormPage.PACKAGE, Values(filter), null),
    )

    val visibleApps: List<App> by lazy {
        packageManager
            .queryIntentActivities(
                Intent(
                    Intent.ACTION_MAIN,
                    null,
                ).addCategory(Intent.CATEGORY_LAUNCHER),
                0,
            ).map {
                App(
                    it.activityInfo.applicationInfo.loadLabel(packageManager).toString(),
                    it.activityInfo.packageName,
                )
            }.sortedBy { it.name }
    }

    val allPackages: List<App> by lazy {
        packageManager.getInstalledApplications(0)
            .map { App(it.loadLabel(packageManager).toString(), it.packageName) }
            .sortedBy { it.name }
    }

    var activeNotifications by mutableStateOf(listOf<Notification>())

    init {
        viewModelScope.launch {
            while (state.page.isFirstPage()) {
                activeNotifications = NotificationListener.instance
                    ?.activeNotifications
                    ?.filter { it.notification.flags and FLAG_GROUP_SUMMARY == 0 }
                    ?.mapIndexed { i, sbn -> Notification(sbn, sbn.packageName, i) }
                    ?: listOf()
                delay(500)
            }
        }
    }

    fun updateForm(page: FormPage, values: Values) {
        state = State(page, values, getError(page, values), getWarnings(page, values))
    }

    private fun getError(
        page: FormPage = state.page,
        values: Values = state.values,
    ): FormError? {
        when (page) {
            FormPage.ZAPPER -> return null

            FormPage.PACKAGE -> if (values.app.packageName.isBlank()) return FormError.BLANK_FIELDS

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

            FormPage.ACTION -> if (values.action is Action.TAP_BUTTON)
                try {
                    if (values.action.buttonRegex.isBlank()) return FormError.BLANK_FIELDS
                    Regex(values.action.buttonRegex).pattern == values.action.buttonRegex
                } catch (_: Exception) {
                    Logger.d("FiltersViewModel.getError", "Button pattern regex invalid")
                    return FormError.INVALID_BUTTON_REGEX
                }

            FormPage.SCHEDULE -> {
                if (!values.schedule.isRangeValid()) return FormError.INVALID_TIME_RANGE
                if (values.schedule.days.isEmpty()) return FormError.BLANK_FIELDS
            }
        }
        return null
    }

    private fun getWarnings(
        page: FormPage = state.page,
        values: Values = state.values,
    ): List<FormWarning> {
        if (page != FormPage.PATTERN || values.notification == null) return listOf()

        try {
            val regexTarget = values.regexTarget
            val notification = values.notification
            val warnings = mutableListOf<FormWarning>()

            if (
                regexTarget != RegexTarget.CONTENT
                && !Regex(values.queryPattern).containsMatchIn(notification.title)
            ) warnings.add(FormWarning.REGEX_DOESNT_MATCH_TITLE)
            if (
                (regexTarget == RegexTarget.CONTENT || regexTarget == RegexTarget.OR)
                && !Regex(values.queryPattern).containsMatchIn(notification.content)
            ) warnings.add(FormWarning.REGEX_DOESNT_MATCH_CONTENT)
            if (
                regexTarget == RegexTarget.AND &&
                !Regex(values.secondaryQueryPattern).containsMatchIn(notification.content)
            ) warnings.add(FormWarning.REGEX_DOESNT_MATCH_CONTENT)

            return warnings
        } catch (_: Exception) {
            return listOf()
        }
    }

    suspend fun submitForm() {
        if (getError() == null) {
            val filter = state.values.toFilter()
            Logger.d(
                "FiltersViewModel.submitForm",
                "${if (state.values.filterId == 0) "Adding" else "Updating"} $filter",
            )
            repository.upsert(filter)
        }
    }
}

enum class FormPage {
    ZAPPER, PACKAGE, PATTERN, ACTION, SCHEDULE;

    fun isFirstPage() = this == ZAPPER

    fun isFinalPage() = this == SCHEDULE

    fun next() = entries[ordinal + 1]

    fun previous() = entries[ordinal - 1]
}

enum class FormError { BLANK_FIELDS, INVALID_NOTIFICATION_REGEX, INVALID_BUTTON_REGEX, INVALID_TIME_RANGE }

enum class FormWarning(val description: Int) {
    REGEX_DOESNT_MATCH_TITLE(R.string.pattern_doesnt_match_title),
    REGEX_DOESNT_MATCH_CONTENT(R.string.pattern_doesnt_match_content)
}
