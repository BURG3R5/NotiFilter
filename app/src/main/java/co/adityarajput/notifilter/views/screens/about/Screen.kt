package co.adityarajput.notifilter.views.screens.about

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import co.adityarajput.notifilter.R
import co.adityarajput.notifilter.views.Theme
import co.adityarajput.notifilter.views.components.AppBar
import co.adityarajput.notifilter.views.icons.NotificationsOff

@Composable
fun AboutScreen(goBack: () -> Unit) {
    Scaffold(topBar = { AppBar(stringResource(R.string.about), true, goBack) }) { paddingValues ->
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
                AboutCard {
                    Box(
                        Modifier
                            .padding(top = dimensionResource(R.dimen.padding_large))
                            .align(Alignment.CenterHorizontally),
                        Alignment.Center,
                    ) {
                        Box(
                            Modifier
                                .size(80.dp)
                                .background(MaterialTheme.colorScheme.primary, shape = CircleShape),
                            Alignment.Center,
                        ) {
                            Icon(
                                NotificationsOff,
                                stringResource(R.string.alttext_app_logo),
                                Modifier.size(50.dp),
                            )
                        }
                    }
                    Text(
                        buildAnnotatedString {
                            withStyle(
                                SpanStyle(
                                    MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold,
                                )
                            ) { append(stringResource(R.string.app_name)) }
                            append(stringResource(R.string.app_description))
                        },
                        Modifier.padding(dimensionResource(R.dimen.padding_large)),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
                AboutCard {
                    Text(
                        AnnotatedString.fromHtml(stringResource(R.string.app_permissions)),
                        Modifier.padding(dimensionResource(R.dimen.padding_large)),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
                AboutCard {
                    Text(
                        AnnotatedString.fromHtml(
                            stringResource(R.string.app_links),
                            TextLinkStyles(
                                SpanStyle(
                                    MaterialTheme.colorScheme.primary,
                                    textDecoration = TextDecoration.Underline,
                                ),
                            ),
                        ),
                        Modifier.padding(dimensionResource(R.dimen.padding_large)),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
                Column(
                    Modifier
                        .fillMaxWidth(),
                    Arrangement.Bottom,
                    Alignment.CenterHorizontally,
                ) {
                    Text(
                        "v${stringResource(R.string.app_version)}",
                        Modifier.padding(
                            top = dimensionResource(R.dimen.padding_large),
                            bottom = dimensionResource(R.dimen.padding_small),
                        ),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Text(
                        AnnotatedString.fromHtml(
                            stringResource(R.string.dev_credit),
                            TextLinkStyles(
                                SpanStyle(
                                    MaterialTheme.colorScheme.primary,
                                    textDecoration = TextDecoration.Underline,
                                ),
                            ),
                        ),
                        Modifier.padding(bottom = dimensionResource(R.dimen.padding_large)),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }
        }
    }
}

@Composable
private fun AboutCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        Modifier
            .fillMaxWidth()
            .padding(dimensionResource(R.dimen.padding_small))
    ) { content() }
}

@Preview
@Composable
private fun AboutScreenPreview() = Theme { AboutScreen {} }
