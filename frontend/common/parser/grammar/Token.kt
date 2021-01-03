@file:Suppress("MemberVisibilityCanBePrivate")

package com.lorenzoog.kofl.frontend.parser.grammar

import com.lorenzoog.kofl.frontend.Expr
import com.lorenzoog.kofl.frontend.TokenType
import com.lorenzoog.kofl.frontend.parser.lib.*

val token = lexeme(label("junk")(regex("""\s*""".toRegex())))

val Expression: Parser<Expr> = Access

val Plus = token(text(TokenType.Plus, "+"))
val Minus = token(text(TokenType.Minus, "-"))
val Star = token(text(TokenType.Star, "*"))
val Slash = token(text(TokenType.Slash, "/"))
val Dot = token(text(TokenType.Dot, "."))
val Comma = token(text(TokenType.Comma, ","))
val LeftParen = token(text(TokenType.LeftParen, "("))
val RightParen = token(text(TokenType.RightParen, ")"))
val Equal = token(text(TokenType.Comma, "="))

val EOF = eof(TokenType.Eof)

val Decimal = label("decimal")(numeric() map { it.toDouble() })

val Numeric = label("numeric")(token(Decimal).mapToLiteral())
val Text = label("text")(token(string(TokenType.String)).mapToLiteral())
val Identifier = label("identifier")(token(identifier(TokenType.Identifier))) map {
  Expr.Var(it, line)
}

val Group = label("group")(
  combine(LeftParen, token(Expression), RightParen) { _, value, _ ->
    Expr.Grouping(value, line)
  }
)

val Primary = label("primary")(
  Identifier or Text or Numeric or Group
)