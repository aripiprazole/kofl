package com.lorenzoog.kofl.frontend.parser.grammar

import com.lorenzoog.kofl.frontend.Expr
import com.lorenzoog.kofl.frontend.parser.lib.*

internal object Math : Grammar<Expr>() {
  override val rule = lazied { Term }

  private val Unary = Token.Primary or combine(Token.Plus or Token.Minus, Token.Primary) { op, rhs ->
    Expr.Unary(op, rhs, line)
  }

  private val Factor = combine(Unary, many(Token.Star or Token.Slash with Unary)) { lhs, rest ->
    rest.fold(lhs) { acc, (op, expr) ->
      Expr.Binary(acc, op, expr, line)
    }
  }

  private val Term = combine(Factor, many(Token.Minus or Token.Plus with Factor)) { lhs, rest ->
    rest.fold(lhs) { acc, (op, expr) ->
      Expr.Binary(acc, op, expr, line)
    }
  }
}