package co.adityarajput.evaluator

/**
 * A generic function that takes one or more strings and returns a string.
 */
abstract class Function(val name: String, val argumentCount: Int) {
    abstract fun evaluate(arguments: List<String>): String
}
