@file:Suppress("MemberVisibilityCanBePrivate")

package com.lorenzoog.kofl.frontend.parser.grammar

import com.lorenzoog.kofl.frontend.Expr
import com.lorenzoog.kofl.frontend.TokenType
import com.lorenzoog.kofl.frontend.parser.lib.*

internal object Token {
  val token = lexeme(label("junk")(regex("""\s*""".toRegex())))

  val Expression: Parser<Expr> = Access

  val Plus = text(TokenType.Plus, "+")
  val Minus = text(TokenType.Minus, "-")
  val Star = text(TokenType.Star, "*")
  val Slash = text(TokenType.Slash, "/")
  val Dot = text(TokenType.Dot, ".")
  val Comma = text(TokenType.Comma, ",")
  val Equal = text(TokenType.Comma, "=")

  val EOF = eof(TokenType.Eof)

  val LeftParen = text(TokenType.LeftParen, "(")
  val RightParen = text(TokenType.RightParen, ")")

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

  operator fun <T> invoke(parser: Parser<T>): Parser<T> {
    return token(parser)
  }
}