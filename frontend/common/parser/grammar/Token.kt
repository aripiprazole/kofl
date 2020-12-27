@file:Suppress("MemberVisibilityCanBePrivate")

package com.lorenzoog.kofl.frontend.parser.grammar

import com.lorenzoog.kofl.frontend.Expr
import com.lorenzoog.kofl.frontend.TokenType
import com.lorenzoog.kofl.frontend.parser.lib.*

internal object Token {
  val Expression: Parser<Expr> = Math.rule

  val Spaces = regex("""\s*""".toRegex())
  val Token = lexeme(Spaces)

  val Plus = text(TokenType.Plus, "+")
  val Minus = text(TokenType.Minus, "-")
  val Star = text(TokenType.Star, "*")
  val Slash = text(TokenType.Slash, "/")

  val String = regex(TokenType.String, """^"[a-zA-Z0-9]*"$""".toRegex())
  val Identifier = regex(TokenType.Identifier, """^[a-zA-Z][a-zA-Z0-9]*$""".toRegex())

  val LeftParen = text(TokenType.LeftParen, "(")
  val RightParen = text(TokenType.RightParen, ")")

  val Decimal = label("decimal")(regex("""\d+(?:\.\d+)?""".toRegex())) map {
    it.toDouble()
  }

  val Const = Token(Decimal) map { Expr.Literal(it, line) }

  val Group = combine(Token(LeftParen), Token(Expression), Token(RightParen)) { _, value, _ ->
    Expr.Grouping(value, line)
  }

  val Primary = Group or Const

  operator fun <T> invoke(parser: Parser<T>): Parser<T> {
    return Token(parser)
  }
}