@file:Suppress("MemberVisibilityCanBePrivate")

package com.lorenzoog.kofl.frontend.parser.grammar

import com.lorenzoog.kofl.frontend.Expr
import com.lorenzoog.kofl.frontend.parser.lib.*
import kotlin.native.concurrent.ThreadLocal

@ThreadLocal
internal object Math : Grammar<Expr>() {
  val Unary = label("unary")(
    Access or combine((Plus or Minus or Bang), Access) { op, rhs ->
      Expr.Unary(op, rhs, line)
    }
  )

  val Factor = label("factor")(
    combine(Unary, many((Star or Slash) + Unary)) { lhs, rest ->
      rest.fold(lhs) { acc, (op, expr) ->
        Expr.Binary(acc, op, expr, line)
      }
    }
  )

  val Term = label("term")(
    combine(Factor, many((Minus or Plus) + Factor)) { lhs, rest ->
      rest.fold(lhs) { acc, (op, expr) ->
        Expr.Binary(acc, op, expr, line)
      }
    }
  )

  override val rule = Term
}