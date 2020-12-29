package com.lorenzoog.kofl.frontend.parser.grammar

import com.lorenzoog.kofl.frontend.Expr
import com.lorenzoog.kofl.frontend.parser.lib.*

internal object Math : Grammar<Expr>() {
  override val rule = lazied { Term }

  private val Unary = label("unary")(
    Access or combine(Token(Token.Plus or Token.Minus), Access) { op, rhs ->
      Expr.Unary(op, rhs, line)
    }
  )

  private val Factor = label("factor")(
    combine(Unary, many(Token(Token.Star or Token.Slash) with Unary)) { lhs, rest ->
      rest.fold(lhs) { acc, (op, expr) ->
        Expr.Binary(acc, op, expr, line)
      }
    }
  )

  private val Term = label("term")(
    combine(Factor, many(Token(Token.Minus or Token.Plus) with Factor)) { lhs, rest ->
      rest.fold(lhs) { acc, (op, expr) ->
        Expr.Binary(acc, op, expr, line)
      }
    }
  )
}