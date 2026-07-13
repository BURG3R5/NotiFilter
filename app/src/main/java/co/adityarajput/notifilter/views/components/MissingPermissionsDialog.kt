package co.adityarajput.notifilter.views.components

import android.os.Handler
import android.os.Looper
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import co.adityarajput.notifilter.R
import co.adityarajput.notifilter.utils.Permission
import co.adityarajput.notifilter.utils.isGranted
import co.adityarajput.notifilter.utils.request

@Composable
fun MissingPermissionsDialog(
    permissions: Set<Permission>,
    hideDialog: () -> Unit,
    hidePermanently: () -> Unit,
) {
    val context = LocalContext.current
    val handler = remember { Handler(Looper.getMainLooper()) }

    var hasPermissions by remember { mutableStateOf(permissions.associateWith { false }) }
    val watcher = object : Runnable {
        override fun run() {
            hasPermissions = context.isGranted(permissions)

            if (!hasPermissions.all { it.value })
                handler.postDelayed(this, 500)
        }
    }
    DisposableEffect(Unit) {
        handler.post(watcher)
        onDispose { handler.removeCallbacksAndMessages(null) }
    }

    AlertDialog(
        hideDialog,
        title = { Text(stringResource(R.string.missing_permissions)) },
        text = {
            Column {
                Text(
                    stringResource(R.string.explain_missing_permissions),
                    style = MaterialTheme.typography.bodyMedium,
                )
                hasPermissions.forEach { (permission, granted) ->
                    Row(
                        Modifier
                            .toggleable(granted, !granted) { context.request(permission) }
                            .padding(vertical = dimensionResource(R.dimen.padding_small)),
                        Arrangement.spacedBy(dimensionResource(R.dimen.padding_small)),
                        Alignment.CenterVertically,
                    ) {
                        Checkbox(granted, null)
                        Text(
                            stringResource(
                                when (permission) {
                                    Permission.ACCESSIBILITY_SERVICE -> R.string.explain_accessibility_service_permission
                                    Permission.POST_NOTIFICATIONS -> R.string.explain_post_notifications_permission
                                    Permission.NOTIFICATION_POLICY -> R.string.explain_notification_policy_permission
                                    Permission.SCHEDULE_EXACT_ALARM -> R.string.explain_exact_alarm_permission
                                    else -> 0
                                },
                            ),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Normal,
                        )
                    }
                }
            }
        },
        confirmButton = {
            if (!hasPermissions.all { it.value }) {
                TextButton(
                    hidePermanently,
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.tertiary),
                ) { Text(stringResource(R.string.hide_permanently)) }
            } else {
                TextButton(hideDialog) {
                    Text(stringResource(R.string.done), fontWeight = FontWeight.Normal)
                }
            }
        },
    )
}
