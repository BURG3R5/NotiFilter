package co.adityarajput.notifilter.views

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.NavOptions
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import co.adityarajput.notifilter.utils.hasNotificationListenerPermission
import co.adityarajput.notifilter.views.screens.about.AboutScreen
import co.adityarajput.notifilter.views.screens.filters.FiltersScreen
import co.adityarajput.notifilter.views.screens.notifications.NotificationsScreen
import co.adityarajput.notifilter.views.screens.onboarding.OnboardingScreen
import co.adityarajput.notifilter.views.screens.settings.SettingsScreen

@Composable
fun Navigator(controller: NavHostController) {
    val hasPermission = remember { controller.context.hasNotificationListenerPermission() }

    NavHost(
        controller,
        when {
            hasPermission -> Routes.FILTERS.name
            else -> Routes.ONBOARDING.name
        },
    ) {
        composable(Routes.ONBOARDING.name) {
            OnboardingScreen {
                controller.navigate(
                    Routes.FILTERS.name,
                    NavOptions.Builder().setPopUpTo(Routes.ONBOARDING.name, true).build(),
                )
            }
        }
        composable(Routes.FILTERS.name) {
            FiltersScreen(
                { controller.navigate(Routes.NOTIFICATIONS.name) },
                { controller.navigate(Routes.SETTINGS.name) },
            )
        }
        composable(Routes.NOTIFICATIONS.name) { NotificationsScreen(controller::popBackStack) }
        composable(Routes.SETTINGS.name) {
            SettingsScreen(
                { controller.navigate(Routes.ABOUT.name) },
                controller::popBackStack,
            )
        }
        composable(Routes.ABOUT.name) { AboutScreen(controller::popBackStack) }
    }
}

enum class Routes {
    ONBOARDING,
    FILTERS,
    NOTIFICATIONS,
    SETTINGS,
    ABOUT,
}
