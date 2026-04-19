package co.adityarajput.notifilter.views.screens

import android.content.ClipData
import android.content.Context.MODE_PRIVATE
import android.os.Build
import android.os.Handler
import android.os.Looper
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
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.toClipEntry
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.edit
import co.adityarajput.notifilter.BuildConfig
import co.adityarajput.notifilter.Constants.RUN_IN_FOREGROUND
import co.adityarajput.notifilter.Constants.SETTINGS
import co.adityarajput.notifilter.R
import co.adityarajput.notifilter.data.AppContainer
import co.adityarajput.notifilter.services.NotificationListener
import co.adityarajput.notifilter.utils.Logger
import co.adityarajput.notifilter.utils.Permission
import co.adityarajput.notifilter.utils.isGranted
import co.adityarajput.notifilter.utils.request
import co.adityarajput.notifilter.views.Theme
import co.adityarajput.notifilter.views.components.AppBar
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun SettingsScreen(
    goToLicensesScreen: () -> Unit = {},
    goToAboutScreen: () -> Unit = {},
    goBack: () -> Unit = {},
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val clipboard = LocalClipboard.current
    val appContainer = remember { AppContainer(context) }
    val handler = remember { Handler(Looper.getMainLooper()) }
    val sharedPreferences = remember { context.getSharedPreferences(SETTINGS, MODE_PRIVATE) }

    var isInvincible by remember {
        mutableStateOf(context.isGranted(Permission.UNRESTRICTED_BACKGROUND_USAGE))
    }
    var isRunningInForeground by remember {
        mutableStateOf(sharedPreferences.getBoolean(RUN_IN_FOREGROUND, false))
    }

    val watcher = object : Runnable {
        override fun run() {
            isInvincible = context.isGranted(Permission.UNRESTRICTED_BACKGROUND_USAGE)
            handler.postDelayed(this, 1000)
        }
    }
    DisposableEffect(Unit) {
        handler.post(watcher)
        onDispose { handler.removeCallbacksAndMessages(null) }
    }

    Scaffold(
        topBar = { AppBar(stringResource(R.string.settings), true, goBack) },
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
                    Text(
                        stringResource(R.string.settings_section_1),
                        Modifier.padding(
                            dimensionResource(R.dimen.padding_large),
                            dimensionResource(R.dimen.padding_medium),
                        ),
                        fontWeight = FontWeight.Medium,
                    )
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = dimensionResource(R.dimen.padding_large)),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                stringResource(R.string.disable_optimization),
                                style = MaterialTheme.typography.titleSmall,
                            )
                            Text(
                                stringResource(R.string.explain_disabling_battery_optimization),
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                        Switch(
                            isInvincible,
                            { context.request(Permission.UNRESTRICTED_BACKGROUND_USAGE, it) },
                        )
                    }
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(
                                dimensionResource(R.dimen.padding_large),
                                dimensionResource(R.dimen.padding_medium),
                            ),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                stringResource(R.string.run_in_foreground),
                                style = MaterialTheme.typography.titleSmall,
                            )
                            Text(
                                stringResource(R.string.explain_running_in_foreground),
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                        Switch(
                            isRunningInForeground,
                            {
                                isRunningInForeground = it
                                sharedPreferences.edit { putBoolean(RUN_IN_FOREGROUND, it) }
                                NotificationListener.updateForegroundStatus(it)
                            },
                        )
                    }
                }
                Card(
                    Modifier
                        .fillMaxWidth()
                        .padding(dimensionResource(R.dimen.padding_small)),
                ) {
                    val importSuccess = stringResource(R.string.import_success)
                    val exportSuccess = stringResource(R.string.export_success)
                    val appNameAndVersion =
                        "${stringResource(R.string.app_name)}_${BuildConfig.VERSION_NAME}"

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

                    Text(
                        stringResource(R.string.settings_section_2),
                        Modifier.padding(
                            dimensionResource(R.dimen.padding_large),
                            dimensionResource(R.dimen.padding_medium),
                        ),
                        fontWeight = FontWeight.Medium,
                    )
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = dimensionResource(R.dimen.padding_large))
                            .clickable { importLauncher.launch(arrayOf("application/json")) },
                    ) {
                        Text(
                            stringResource(R.string.import_filters),
                            style = MaterialTheme.typography.titleSmall,
                        )
                        Text(
                            stringResource(R.string.import_warning),
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .padding(
                                dimensionResource(R.dimen.padding_large),
                                dimensionResource(R.dimen.padding_medium),
                            )
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
                        Text(
                            stringResource(R.string.export_filters),
                            style = MaterialTheme.typography.titleSmall,
                        )
                        Text(
                            stringResource(R.string.export_explanation),
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
                Card(
                    Modifier
                        .fillMaxWidth()
                        .padding(dimensionResource(R.dimen.padding_small)),
                ) {
                    val copySuccess = stringResource(R.string.copy_success)
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .padding(
                                dimensionResource(R.dimen.padding_large),
                                dimensionResource(R.dimen.padding_medium),
                            ),
                        Arrangement.spacedBy(dimensionResource(R.dimen.padding_medium)),
                    ) {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clickable {
                                    scope.launch {
                                        clipboard.setClipEntry(
                                            ClipData.newPlainText(
                                                "logs",
                                                Logger.logs.joinToString("\n"),
                                            ).toClipEntry(),
                                        )
                                        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2)
                                            Toast
                                                .makeText(context, copySuccess, Toast.LENGTH_SHORT)
                                                .show()
                                    }
                                },
                            Arrangement.spacedBy(dimensionResource(R.dimen.padding_small)),
                        ) {
                            Icon(
                                painterResource(R.drawable.list_alt),
                                stringResource(R.string.alttext_logs),
                            )
                            Text(
                                stringResource(R.string.copy_logs),
                                fontWeight = FontWeight.Medium,
                            )
                        }
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clickable { goToLicensesScreen() },
                            Arrangement.spacedBy(dimensionResource(R.dimen.padding_small)),
                        ) {
                            Icon(
                                painterResource(R.drawable.license),
                                stringResource(R.string.licenses),
                            )
                            Text(
                                stringResource(R.string.view_licenses),
                                fontWeight = FontWeight.Medium,
                            )
                        }
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clickable { goToAboutScreen() },
                            Arrangement.spacedBy(dimensionResource(R.dimen.padding_small)),
                        ) {
                            Icon(
                                painterResource(R.drawable.info),
                                stringResource(R.string.alttext_info),
                            )
                            Text(
                                stringResource(R.string.about_app),
                                fontWeight = FontWeight.Medium,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun SettingsScreenPreview() = Theme { SettingsScreen() }
