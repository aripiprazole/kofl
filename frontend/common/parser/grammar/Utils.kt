package com.lorenzoog.kofl.frontend.parser.grammar

import com.lorenzoog.kofl.frontend.Stmt
import com.lorenzoog.kofl.frontend.parser.lib.Parser
import com.lorenzoog.kofl.frontend.parser.lib.combine
import com.lorenzoog.kofl.frontend.parser.lib.label
import com.lorenzoog.kofl.frontend.parser.lib.lazied
import com.lorenzoog.kofl.frontend.parser.lib.many
import com.lorenzoog.kofl.frontend.parser.lib.or
import com.lorenzoog.kofl.frontend.parser.lib.plus

val Block = label("block")(
  combine(LeftBrace, many(lazied { Statement }), RightBrace) { _, body, _ ->
    body
  }
)

val Body = label("body")(
  Block or label("expression-body")(
    combine(Keywords.Then, Func) { _, expr -> listOf(Stmt.ExprStmt(expr, line)) }
  )
)

fun <T> semicolon(parser: Parser<T>): Parser<T> {
  return combine(parser, (Semicolon + many(Semicolon))) { a, _ -> a }
}

fun <T> optionalSemicolon(parser: Parser<T>): Parser<T> {
  return combine(parser, many(Semicolon)) { a, _ -> a }
}

fun Char.isDigit(): Boolean = this in '0'..'9'
fun Char.isAlpha(): Boolean = this in 'a'..'z' || this in 'A'..'Z' || this == '_'
fun Char.isAlphaNumeric(): Boolean = isAlpha() || isDigit()
