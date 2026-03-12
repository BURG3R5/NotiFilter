package co.adityarajput.notifilter.utils

import net.fellbaum.jemoji.EmojiManager

private const val EMOJI_PATTERN_DISPLAY = "\\p{Emoji}"
private const val EMOJI_PATTERN_INTERNAL = "\uE010"

fun String.containsMatchIn(input: String): Boolean {
    var pattern = this
    var text = input

    if (contains(EMOJI_PATTERN_DISPLAY)) {
        pattern = pattern.replace(EMOJI_PATTERN_DISPLAY, EMOJI_PATTERN_INTERNAL)
        text = EmojiManager.replaceAllEmojis(text, EMOJI_PATTERN_INTERNAL)
    }

    return Regex(pattern).containsMatchIn(text)
}

fun String.isValidRegex() = try {
    this
        .replace(EMOJI_PATTERN_DISPLAY, EMOJI_PATTERN_INTERNAL)
        .run { Regex(this).pattern == this }
} catch (_: Exception) {
    false
}

private const val REGEX_META_CHARACTERS = "\\.^$|?*+()[]{}"

fun String.generateRegex() = buildString {
    append('^')
    for (char in this@generateRegex) {
        if (REGEX_META_CHARACTERS.contains(char))
            append('\\')
        append(char)
    }
    append('$')
}
