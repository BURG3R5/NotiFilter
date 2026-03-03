package co.adityarajput.notifilter.utils

fun String.generateRegex() = buildString {
    append('^')
    for (char in this@generateRegex) {
        if (REGEX_META_CHARACTERS.contains(char)) {
            append('\\')
        }
        append(char)
    }
    append('$')
}

private val REGEX_META_CHARACTERS =
    setOf('\\', '.', '^', '$', '|', '?', '*', '+', '(', ')', '[', ']', '{', '}')
