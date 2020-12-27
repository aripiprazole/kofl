package com.lorenzoog.kofl.frontend.parser.grammar

import com.lorenzoog.kofl.frontend.Expr
import com.lorenzoog.kofl.frontend.parser.lib.*

internal object Math : Grammar<Expr>() {
  override val rule get() = Term

  private val Factor = combine(Token.Primary, many(Token.Star or Token.Slash with Token.Primary)) { lhs, rest ->
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