@file:Suppress("MemberVisibilityCanBePrivate")

package com.lorenzoog.kofl.frontend.parser.grammar

import com.lorenzoog.kofl.frontend.Expr
import com.lorenzoog.kofl.frontend.TokenType
import com.lorenzoog.kofl.frontend.parser.lib.*

internal object Token {
  val Spaces = regex("""\s*""".toRegex())
  val Token = lexeme(Spaces)

  val Plus = token(TokenType.Plus, "+")
  val Minus = token(TokenType.Minus, "-")
  val Star = token(TokenType.Star, "*")
  val Slash = token(TokenType.Slash, "/")

  val LeftParen = token(TokenType.LeftParen, "(")
  val RightParen = token(TokenType.RightParen, ")")

  val Decimal = label("decimal")(regex("""\d+(?:\.\d+)?""".toRegex())) mapWith {
    it.toDouble()
  }

  val Const = Token(Decimal) mapWith { Expr.Literal(it, line) }
  val Group = combine(Token(LeftParen), Token(Const), Token(RightParen)) { _, value, _ ->
    Expr.Grouping(value, line)
  }

  val Primary = Const or Group

  operator fun <T> invoke(parser: ParseFunc<T>): ParseFunc<T> {
    return Token(parser)
  }
}