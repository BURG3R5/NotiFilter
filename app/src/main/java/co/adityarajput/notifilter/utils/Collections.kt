package co.adityarajput.notifilter.utils

fun <T> List<T>.filterFirst(count: Int, predicate: (T) -> Boolean): Pair<List<T>, Boolean> {
    val result = mutableListOf<T>()

    forEach {
        if (predicate(it)) {
            result.add(it)
            if (result.size == count) return result to false
        }
    }

    return result to true
}
