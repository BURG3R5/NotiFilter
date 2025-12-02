package co.adityarajput.notifilter.views.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import co.adityarajput.notifilter.R
import co.adityarajput.notifilter.views.Theme
import co.adityarajput.notifilter.views.icons.NotificationsOff

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppBar(
    title: String,
    canNavigateBack: Boolean,
    leadingIconOnClick: () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
) {
    TopAppBar(
        {
            Text(
                title,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontSize = when {
                        canNavigateBack -> MaterialTheme.typography.headlineSmall.fontSize
                        else -> MaterialTheme.typography.headlineLarge.fontSize
                    }
                ),
            )
        },
        colors = TopAppBarDefaults.topAppBarColors().copy(
            containerColor = MaterialTheme.colorScheme.primary,
        ),
        navigationIcon = {
            IconButton(leadingIconOnClick) {
                if (canNavigateBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        stringResource(R.string.alttext_back_button)
                    )
                } else {
                    Icon(
                        NotificationsOff,
                        stringResource(R.string.alttext_app_logo)
                    )
                }
            }
        },
        actions = actions,
    )
}

@Preview
@Composable
private fun HomeAppBarPreview() {
    Theme {
        AppBar(stringResource(R.string.app_name), false)
    }
}

@Preview
@Composable
private fun AppBarPreview() {
    Theme {
        AppBar("Page Title", true)
    }
}
