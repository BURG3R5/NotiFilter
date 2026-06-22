package co.adityarajput.notifilter.utils

import co.adityarajput.evaluator.Evaluator
import co.adityarajput.evaluator.Function
import co.adityarajput.notifilter.data.models.Notification

fun String.evaluateAgainst(notification: Notification?) =
    Evaluator(
        object : Function("titleMatches", 1) {
            override fun evaluate(arguments: List<String>) =
                (arguments[0].containsMatchIn(notification?.title ?: "")).toString()
        },
        object : Function("contentMatches", 1) {
            override fun evaluate(arguments: List<String>) =
                (arguments[0].containsMatchIn(notification?.content ?: "")).toString()
        },
    ).evaluate(this).toBooleanStrict()
