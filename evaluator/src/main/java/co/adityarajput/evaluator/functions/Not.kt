package co.adityarajput.evaluator.functions

import co.adityarajput.evaluator.Function

/**
 * Logical NOT
 */
object Not : Function("not", 1) {
    override fun evaluate(arguments: List<String>) =
        arguments[0].toBooleanStrict().not().toString()
}
