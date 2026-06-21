package co.adityarajput.evaluator.functions

import co.adityarajput.evaluator.Function

/**
 * Logical OR
 */
object Or : Function("or", 2) {
    override fun evaluate(arguments: List<String>) =
        arguments[0].toBooleanStrict().or(arguments[1].toBooleanStrict()).toString()
}
