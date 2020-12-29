@file:Suppress("MemberVisibilityCanBePrivate")

package com.lorenzoog.kofl.frontend.parser.grammar

import com.lorenzoog.kofl.frontend.Expr
import com.lorenzoog.kofl.frontend.TokenType
import com.lorenzoog.kofl.frontend.parser.lib.*

internal object Token {
  val token = lexeme(regex("""\s*""".toRegex()))

  val Expression: Parser<Expr> = Math.rule

  val Plus = text(TokenType.Plus, "+")
  val Minus = text(TokenType.Minus, "-")
  val Star = text(TokenType.Star, "*")
  val Slash = text(TokenType.Slash, "/")

  val LeftParen = text(TokenType.LeftParen, "(")
  val RightParen = text(TokenType.RightParen, ")")

  val Decimal = label("decimal")(predicate(Matchers.number) map {
    it.toDouble()
  })

  val Numeric = token(Decimal).mapToLiteral()

  val Text = label("text")(
    token(regex(TokenType.String, """^"[a-zA-Z0-9]*"$""".toRegex())).mapToLiteral()
  )

  val Identifier = label("identifier")(
    token(regex(TokenType.Identifier, """[a-zA-Z][a-zA-Z0-9_]*""".toRegex())) map {
      Expr.Var(it, line)
    }
  )

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