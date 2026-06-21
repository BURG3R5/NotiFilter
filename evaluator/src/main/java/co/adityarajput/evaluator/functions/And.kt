package co.adityarajput.evaluator.functions

import co.adityarajput.evaluator.Function

/**
 * Logical AND
 */
object And : Function("and", 2) {
    override fun evaluate(arguments: List<String>) =
        arguments[0].toBooleanStrict().and(arguments[1].toBooleanStrict()).toString()
}
