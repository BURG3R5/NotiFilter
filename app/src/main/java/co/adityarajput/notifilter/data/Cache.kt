package co.adityarajput.notifilter.data

import android.content.Intent
import android.content.pm.PackageManager
import co.adityarajput.notifilter.data.models.App
import co.adityarajput.notifilter.data.models.Intents
import co.adityarajput.notifilter.utils.Logger
import java.util.Collections.synchronizedMap

object Cache {
    private var _allPackages: List<App>? = null
    private var _visibleApps: List<App>? = null

    val intents: MutableMap<Int, Intents> = synchronizedMap(mutableMapOf())

    private var _cachedAt = 0L
    private const val APPS_CACHE_TIMEOUT = 10 * 60 * 1000L

    fun getAllPackages(packageManager: PackageManager): List<App> {
        if (_allPackages == null || System.currentTimeMillis() - _cachedAt > APPS_CACHE_TIMEOUT) {
            update(packageManager)
        }

        return _allPackages!!
    }

    fun getVisibleApps(packageManager: PackageManager): List<App> {
        if (_visibleApps == null || System.currentTimeMillis() - _cachedAt > APPS_CACHE_TIMEOUT) {
            update(packageManager)
        }

        return _visibleApps!!
    }

    fun update(packageManager: PackageManager) {
        Logger.d("Cache", "Updating apps")

        _allPackages = packageManager.getInstalledApplications(0)
            .map { App(it.loadLabel(packageManager).toString(), it.packageName) }
            .sortedBy { it.name }

        _visibleApps = packageManager.queryIntentActivities(
            Intent(Intent.ACTION_MAIN, null).addCategory(Intent.CATEGORY_LAUNCHER),
            0,
        ).map {
            App(
                it.activityInfo.applicationInfo.loadLabel(packageManager).toString(),
                it.activityInfo.packageName,
            )
        }.sortedBy { it.name }

        _cachedAt = System.currentTimeMillis()
        Logger.d("Cache", "Updated cache")
    }
}
