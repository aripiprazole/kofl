package com.lorenzoog.kofl.frontend.parser.grammar

import com.lorenzoog.kofl.frontend.Expr
import com.lorenzoog.kofl.frontend.parser.lib.Parser
import com.lorenzoog.kofl.frontend.parser.lib.StringMatcher
import com.lorenzoog.kofl.frontend.parser.lib.map

/**
 * TODO: use real line and column
 */
val line get() = 0

fun <T> Parser<T>.mapToLiteral(): Parser<Expr.Literal> = { input ->
  map { Expr.Literal(it ?: error("$it should not be converted to Expr.Literal if it is null"), line) }.invoke(input)
}

fun Char.isDigit(): Boolean = this in '0'..'9'
fun Char.isAlpha(): Boolean = this in 'a'..'z' || this in 'A'..'Z' || this == '_'
fun Char.isAlphaNumeric(): Boolean = isAlpha() || isDigit()

fun String.isNumeric(): Boolean {
  return filter { it.isDigit() }.length == length
}

object Matchers {
  val number: StringMatcher = { input, index, char ->
    char.isDigit() || (char == '.' && input.getOrNull(index + 1)?.isDigit() == true)
  }
}
