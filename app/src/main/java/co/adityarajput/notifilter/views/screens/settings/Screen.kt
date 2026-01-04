package co.adityarajput.notifilter.views.screens.settings

import android.annotation.SuppressLint
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.edit
import androidx.core.net.toUri
import co.adityarajput.notifilter.Constants
import co.adityarajput.notifilter.R
import co.adityarajput.notifilter.data.AppContainer
import co.adityarajput.notifilter.utils.hasUnrestrictedBackgroundUsagePermission
import co.adityarajput.notifilter.views.Theme
import co.adityarajput.notifilter.views.components.AppBar
import co.adityarajput.notifilter.views.icons.Info
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@SuppressLint("BatteryLife")
@Composable
fun SettingsScreen(
    goToAboutScreen: () -> Unit = {},
    goBack: () -> Unit = {},
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val handler = remember { Handler(Looper.getMainLooper()) }
    val appContainer = remember { AppContainer(context) }
    val sharedPreferences =
        remember { context.getSharedPreferences(Constants.SETTINGS, MODE_PRIVATE) }

    var isInvincible by remember { mutableStateOf(true) }
    var isStoringActiveNotifications by remember {
        mutableStateOf(sharedPreferences.getBoolean(Constants.STORE_ACTIVE_NOTIFICATIONS, false))
    }

    val watcher = object : Runnable {
        override fun run() {
            isInvincible = context.hasUnrestrictedBackgroundUsagePermission()
            isStoringActiveNotifications =
                sharedPreferences.getBoolean(Constants.STORE_ACTIVE_NOTIFICATIONS, false)
            handler.postDelayed(this, 1000)
        }
    }
    DisposableEffect(Unit) {
        handler.post(watcher)
        onDispose { handler.removeCallbacksAndMessages(null) }
    }

    Scaffold(
        topBar = {
            AppBar(
                stringResource(R.string.settings),
                true,
                goBack,
            )
        },
    ) { paddingValues ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            Column(
                Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(dimensionResource(R.dimen.padding_small)),
                Arrangement.Top,
                Alignment.CenterHorizontally,
            ) {
                Card(
                    Modifier
                        .fillMaxWidth()
                        .padding(dimensionResource(R.dimen.padding_small)),
                ) {
                    Box(Modifier.height(dimensionResource(R.dimen.padding_medium)))
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = dimensionResource(R.dimen.padding_large)),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(stringResource(R.string.disable_battery_optimization))
                            Text(
                                stringResource(R.string.justify_disabling_battery_optimization),
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                        Switch(
                            isInvincible,
                            {
                                if (it) {
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
                        )
                    }
                    Box(Modifier.height(dimensionResource(R.dimen.padding_medium)))
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = dimensionResource(R.dimen.padding_large)),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(stringResource(R.string.store_active_notifications))
                            Text(
                                stringResource(R.string.justify_storing_active_notifications),
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                        Switch(
                            isStoringActiveNotifications,
                            {
                                isStoringActiveNotifications = it
                                sharedPreferences.edit {
                                    putBoolean(
                                        Constants.STORE_ACTIVE_NOTIFICATIONS,
                                        it,
                                    )
                                }
                            },
                        )
                    }
                    Box(Modifier.height(dimensionResource(R.dimen.padding_medium)))
                }
                Card(
                    Modifier
                        .fillMaxWidth()
                        .padding(dimensionResource(R.dimen.padding_small)),
                ) {
                    Box(Modifier.height(dimensionResource(R.dimen.padding_medium)))
                    val importSuccess = stringResource(R.string.import_success)
                    val importLauncher = rememberLauncherForActivityResult(
                        ActivityResultContracts.OpenDocument(),
                    ) { uri ->
                        scope.launch {
                            uri
                                ?.let { context.contentResolver.openInputStream(it) }
                                ?.use {
                                    appContainer.import(it.bufferedReader().readText())
                                    goBack()
                                    Toast
                                        .makeText(context, importSuccess, Toast.LENGTH_SHORT)
                                        .show()
                                }
                        }
                    }
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = dimensionResource(R.dimen.padding_large))
                            .clickable { importLauncher.launch(arrayOf("application/json")) },
                    ) {
                        Text(stringResource(R.string.import_filters))
                        Text(
                            stringResource(R.string.import_warning),
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                    Box(Modifier.height(dimensionResource(R.dimen.padding_medium)))
                    val appNameAndVersion =
                        "${stringResource(R.string.app_name)}_${stringResource(R.string.app_version)}"
                    val exportSuccess = stringResource(R.string.export_success)
                    val exportLauncher = rememberLauncherForActivityResult(
                        ActivityResultContracts.CreateDocument("application/json"),
                    ) { uri ->
                        scope.launch {
                            uri
                                ?.let { context.contentResolver.openOutputStream(it) }
                                ?.use {
                                    it.write(appContainer.export().toByteArray())
                                    Toast
                                        .makeText(context, exportSuccess, Toast.LENGTH_SHORT)
                                        .show()
                                }
                        }
                    }
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = dimensionResource(R.dimen.padding_large))
                            .clickable {
                                exportLauncher.launch(
                                    appNameAndVersion + "_${
                                        Instant.now().atZone(ZoneId.systemDefault())
                                            .toLocalDateTime().format(
                                                DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"),
                                            )
                                    }.json",
                                )
                            },
                    ) {
                        Text(stringResource(R.string.export_filters))
                        Text(
                            stringResource(R.string.export_explanation),
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                    Box(Modifier.height(dimensionResource(R.dimen.padding_medium)))
                }
                Card(
                    Modifier
                        .fillMaxWidth()
                        .padding(dimensionResource(R.dimen.padding_small))
                        .clickable { goToAboutScreen() },
                ) {
                    Row(
                        Modifier.padding(dimensionResource(R.dimen.padding_large)),
                        Arrangement.spacedBy(dimensionResource(R.dimen.padding_small)),
                    ) {
                        Icon(Info, stringResource(R.string.alttext_info))
                        Text(stringResource(R.string.about))
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun SettingsScreenPreview() = Theme { SettingsScreen() }
