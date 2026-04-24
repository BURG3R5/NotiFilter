package co.adityarajput.notifilter.utils

import android.util.Log
import co.adityarajput.notifilter.Constants
import org.acra.ACRA

object Logger {
    val logs = ArrayDeque<String>(Constants.LOG_SIZE)

    fun d(tag: String, msg: String) {
        Log.d(tag, msg)

        if (logs.size >= Constants.LOG_SIZE) logs.removeFirst()
        logs.addLast("[${System.currentTimeMillis()}][$tag][DEBUG] $msg")
    }

    fun i(tag: String, msg: String) {
        Log.i(tag, msg)

        if (logs.size >= Constants.LOG_SIZE) logs.removeFirst()
        logs.addLast("[${System.currentTimeMillis()}][$tag][INFO] $msg")

        ACRA.errorReporter.putCustomData("logs", logs.joinToString("\n"))
    }

    fun e(tag: String, msg: String, tr: Throwable? = null) {
        Log.e(tag, msg, tr)

        if (logs.size >= Constants.LOG_SIZE) logs.removeFirst()
        logs.addLast("[${System.currentTimeMillis()}][$tag][ERROR] $msg")

        ACRA.errorReporter.putCustomData("logs", logs.joinToString("\n"))
    }
}
