package co.adityarajput.notifilter.views.screens

import android.annotation.SuppressLint
import android.os.Handler
import android.os.Looper
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import co.adityarajput.notifilter.R
import co.adityarajput.notifilter.utils.Permission
import co.adityarajput.notifilter.utils.isGranted
import co.adityarajput.notifilter.utils.request
import co.adityarajput.notifilter.views.Theme
import co.adityarajput.notifilter.views.components.AppBar

private val permissions = listOf(
    Permission.NOTIFICATION_LISTENER,
    Permission.UNRESTRICTED_BACKGROUND_USAGE,
)

@SuppressLint("BatteryLife")
@Composable
fun OnboardingScreen(goToFiltersScreen: () -> Unit = {}) {
    val context = LocalContext.current
    val handler = remember { Handler(Looper.getMainLooper()) }

    var hasPermissions by remember { mutableStateOf(context.isGranted(permissions)) }

    val watcher = object : Runnable {
        override fun run() {
            hasPermissions = context.isGranted(permissions)
            if (hasPermissions.all { it.value }) goToFiltersScreen()
            else handler.postDelayed(this, 500)
        }
    }
    DisposableEffect(Unit) {
        handler.post(watcher)
        onDispose { handler.removeCallbacksAndMessages(null) }
    }

    Scaffold(topBar = { AppBar(stringResource(R.string.app_name), false) }) { paddingValues ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(dimensionResource(R.dimen.padding_extra_large)),
                Arrangement.Center,
                Alignment.CenterHorizontally,
            ) {
                if (!hasPermissions.getValue(Permission.NOTIFICATION_LISTENER)) {
                    Text(stringResource(R.string.onboarding_info_1))
                    Button(
                        { context.request(Permission.NOTIFICATION_LISTENER) },
                        Modifier.padding(dimensionResource(R.dimen.padding_large)),
                        colors = ButtonDefaults.buttonColors(contentColor = MaterialTheme.colorScheme.onPrimaryContainer),
                    ) { Text(stringResource(R.string.grant_permission)) }
                } else if (!hasPermissions.getValue(Permission.UNRESTRICTED_BACKGROUND_USAGE)) {
                    Text(stringResource(R.string.onboarding_info_2))
                    Button(
                        { context.request(Permission.UNRESTRICTED_BACKGROUND_USAGE) },
                        Modifier.padding(top = dimensionResource(R.dimen.padding_large)),
                        colors = ButtonDefaults.buttonColors(contentColor = MaterialTheme.colorScheme.onPrimaryContainer),
                    ) { Text(stringResource(R.string.disable_optimization)) }
                    TextButton(goToFiltersScreen) {
                        Text(stringResource(R.string.skip))
                    }
                } else {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

@Preview
@Composable
private fun OnboardingScreenPreview() = Theme { OnboardingScreen() }
