package co.adityarajput.notifilter.views.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.*
import androidx.compose.ui.text.style.TextDecoration
import co.adityarajput.notifilter.R
import co.adityarajput.notifilter.data.models.RegexTarget
import co.adityarajput.notifilter.utils.generateRegex
import co.adityarajput.notifilter.utils.getFirst
import co.adityarajput.notifilter.viewmodels.UpsertFilterViewModel

@Composable
fun SupportingText(viewModel: UpsertFilterViewModel, isPrimaryPattern: Boolean) {
    if (viewModel.state.values.notification == null) return

    val title = viewModel.state.values.notification!!.title.getFirst(20)
    val content = viewModel.state.values.notification!!.content.getFirst(20)

    val target = when (viewModel.state.values.regexTarget) {
        RegexTarget.TITLE -> title

        RegexTarget.CONTENT -> content

        RegexTarget.OR -> "$title' ${stringResource(R.string.or)} '$content"

        RegexTarget.AND if (isPrimaryPattern) -> title

        else -> content
    }
    val pattern = when (viewModel.state.values.regexTarget) {
        RegexTarget.OR -> title.generateRegex() + "|" + content.generateRegex()
        else -> target.generateRegex()
    }

    Text(
        buildAnnotatedString {
            append(stringResource(R.string.pattern_supporting, target))
            withLink(
                LinkAnnotation.Clickable(
                    "generate",
                    TextLinkStyles(
                        SpanStyle(
                            MaterialTheme.colorScheme.primary,
                            textDecoration = TextDecoration.Underline,
                        ),
                    ),
                ) {
                    viewModel.updateForm(
                        viewModel.state.page,
                        viewModel.state.values.run {
                            if (isPrimaryPattern) copy(queryPattern = pattern)
                            else copy(secondaryQueryPattern = pattern)
                        },
                    )
                },
            ) {
                append(stringResource(R.string.generate))
            }
        },
    )
}
