package co.adityarajput.notifilter.data.models

import co.adityarajput.notifilter.R
import kotlinx.serialization.Serializable

@Serializable
enum class RegexTarget(val description: Int) {
    TITLE(R.string.title),
    CONTENT(R.string.content),
    OR(R.string.title_or_content),
    AND(R.string.title_and_content),
}
