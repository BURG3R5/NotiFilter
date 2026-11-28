package co.adityarajput.notifilter.services

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import androidx.core.content.edit

object Preferences {
    const val SETTINGS = "settings"
    private lateinit var settings: SharedPreferences

    const val STATE = "state"
    private lateinit var state: SharedPreferences

    fun init(context: Context) {
        settings = context.getSharedPreferences(SETTINGS, MODE_PRIVATE)
        state = context.getSharedPreferences(STATE, MODE_PRIVATE)
    }

    private const val RUN_IN_FOREGROUND = "run_in_foreground"
    var runInForeground
        get() = settings.getBoolean(RUN_IN_FOREGROUND, false)
        set(value) = settings.edit { putBoolean(RUN_IN_FOREGROUND, value) }

    private const val SHOW_MISSING_PERMISSIONS_DIALOG = "show_missing_permissions_dialog"
    var showMissingPermissionsDialog
        get() = settings.getBoolean(SHOW_MISSING_PERMISSIONS_DIALOG, true)
        set(value) = settings.edit { putBoolean(SHOW_MISSING_PERMISSIONS_DIALOG, value) }

    private const val WIDGET_PREVIEW_SET_AT = "widget_preview_set_at"
    var widgetPreviewSetAt
        get() = state.getLong(WIDGET_PREVIEW_SET_AT, 0)
        set(value) = state.edit { putLong(WIDGET_PREVIEW_SET_AT, value) }

    private const val BRIGHTNESS = "brightness"
    var brightness
        get() = settings.getInt(BRIGHTNESS, 1)
        set(value) = settings.edit { putInt(BRIGHTNESS, value) }

    private const val LANGUAGE = "language"
    /** 空字符串表示跟随系统；其余为 BCP-47 语言标签，例如 "zh-CN" */
    var language
        get() = settings.getString(LANGUAGE, "") ?: ""
        set(value) = settings.edit { putString(LANGUAGE, value) }
}
