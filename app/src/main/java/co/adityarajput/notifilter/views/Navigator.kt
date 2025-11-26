package co.adityarajput.notifilter.views

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import co.adityarajput.notifilter.utils.hasNotificationListenerPermission
import co.adityarajput.notifilter.views.screens.FiltersScreen
import co.adityarajput.notifilter.views.screens.NotificationsScreen
import co.adityarajput.notifilter.views.screens.PermissionScreen

@Composable
fun Navigator(controller: NavHostController) {
    val hasPermission = remember { controller.context.hasNotificationListenerPermission() }

    NavHost(
        controller,
        when {
            hasPermission -> Routes.FILTERS.name
            else -> Routes.PERMISSION.name
        }
    ) {
        composable(Routes.PERMISSION.name) { PermissionScreen { controller.navigate(Routes.FILTERS.name) } }
        composable(Routes.FILTERS.name) { FiltersScreen({ controller.navigate(Routes.NOTIFICATIONS.name) }) }
        composable(Routes.NOTIFICATIONS.name) { NotificationsScreen({ controller.popBackStack() }) }
    }
}

enum class Routes() {
    PERMISSION,
    FILTERS,
    NOTIFICATIONS,
}
