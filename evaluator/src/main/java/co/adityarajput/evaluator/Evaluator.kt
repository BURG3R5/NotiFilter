package co.adityarajput.evaluator

import com.github.h0tk3y.betterParse.combinators.*
import com.github.h0tk3y.betterParse.grammar.Grammar
import com.github.h0tk3y.betterParse.grammar.parseToEnd
import com.github.h0tk3y.betterParse.grammar.parser
import com.github.h0tk3y.betterParse.lexer.literalToken
import com.github.h0tk3y.betterParse.lexer.regexToken
import com.github.h0tk3y.betterParse.parser.Parser

/**
 * A simple Grammar that evaluates expressions of strings and stringlist-to-string functions.
 */
class Evaluator(vararg extensions: Function) : Grammar<String>() {
    private val functions: Map<String, Function> =
        (listOf(
            co.adityarajput.evaluator.functions.And,
            co.adityarajput.evaluator.functions.Equal,
            co.adityarajput.evaluator.functions.Not,
            co.adityarajput.evaluator.functions.Or,
        ) + extensions).associateBy { it.name }

    fun evaluate(expression: String): String {
        if (expression.isBlank())
            throw IllegalArgumentException("Empty expression")

        try {
            return parseToEnd(expression)
        } catch (e: IllegalArgumentException) {
            throw e
        } catch (e: Exception) {
            throw IllegalArgumentException("Invalid expression: ${e.message}", e)
        }
    }

    // region Tokens
    private val leftParen by literalToken("(")

    private val rightParen by literalToken(")")

    private val comma by literalToken(",")

    @Suppress("unused")
    private val whitespace by regexToken("\\s+", ignore = true)

    private val stringLiteral by regexToken("\"[^\"]*\"")

    private val identifier by regexToken("[a-zA-Z_][a-zA-Z0-9_]*")
    // endregion

    // region Parsers
    private val stringParser = stringLiteral use { text.removeSurrounding("\"") }

    private val expr = stringParser or parser(this::functionCall)

    private val argList: Parser<List<String>> = separatedTerms(expr, comma)
    // endregion

    private val functionCall =
        (identifier * skip(leftParen) * argList * skip(rightParen)) map { (f, arguments) ->
            val name = f.text

            val function = functions[name]
                ?: throw IllegalArgumentException("Unknown function: $name")

            if (function.argumentCount != arguments.size)
                throw IllegalArgumentException("$name expects ${function.argumentCount} arguments but got ${arguments.size}")

            function.evaluate(arguments)
        }

    override val rootParser = functionCall

}
