package co.adityarajput.notifilter.views.screens.filters

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.core.net.toUri
import co.adityarajput.notifilter.R
import co.adityarajput.notifilter.utils.hasUnrestrictedBackgroundUsagePermission
import co.adityarajput.notifilter.viewmodels.FiltersViewModel

@SuppressLint("BatteryLife")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsDialog(viewModel: FiltersViewModel) {
    val context = LocalContext.current
    val handler = remember { Handler(Looper.getMainLooper()) }
    val isLogging = viewModel.isStoringActiveNotifications
    var isRestricted by remember { mutableStateOf(false) }

    val watcher = object : Runnable {
        override fun run() {
            isRestricted = !context.hasUnrestrictedBackgroundUsagePermission()
            handler.postDelayed(this, 1000)
        }
    }
    DisposableEffect(Unit) {
        handler.post(watcher)
        onDispose { handler.removeCallbacksAndMessages(null) }
    }

    AlertDialog(
        { viewModel.showSettingsDialog = false },
        title = { Text(stringResource(R.string.app_settings)) },
        text = {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(dimensionResource(R.dimen.padding_small)),
                Arrangement.spacedBy(dimensionResource(R.dimen.padding_medium)),
            ) {
                Text(stringResource(R.string.justify_ignoring_battery_optimization))
                Button(
                    {
                        if (isRestricted) {
                            val intent = Intent(
                                Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                                "package:${context.packageName}".toUri(),
                            )
                            context.startActivity(intent)
                        } else {
                            val intent =
                                Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                            context.startActivity(intent)
                        }
                    },
                    Modifier.align(Alignment.CenterHorizontally),
                    colors = ButtonDefaults.buttonColors(
                        contentColor =
                            MaterialTheme.colorScheme.onPrimaryContainer,
                    ),
                ) { Text(stringResource(if (isRestricted) R.string.disable_optimization else R.string.enable_optimization)) }
                Text(stringResource(R.string.justify_storing_active_notifications))
                Button(
                    viewModel::toggleStoringActiveNotifications,
                    Modifier.align(Alignment.CenterHorizontally),
                    colors = ButtonDefaults.buttonColors(contentColor = MaterialTheme.colorScheme.onPrimaryContainer),
                ) { Text(stringResource(if (isLogging) R.string.disable_storage else R.string.enable_storage)) }
            }
        },
        confirmButton = {
            TextButton({ viewModel.showSettingsDialog = false }) {
                Text(stringResource(R.string.done))
            }
        },
    )
}
