package co.adityarajput.evaluator.functions

import co.adityarajput.evaluator.Function

/**
 * Equality
 */
object Equal : Function("equal", 2) {
    override fun evaluate(arguments: List<String>) =
        (arguments[0] == arguments[1]).toString()
}
