@file:Suppress("MemberVisibilityCanBePrivate")

package me.devgabi.kofl.frontend.parser.grammar

import me.devgabi.kofl.frontend.Expr
import me.devgabi.kofl.frontend.parser.lib.Grammar
import me.devgabi.kofl.frontend.parser.lib.combine
import me.devgabi.kofl.frontend.parser.lib.label
import me.devgabi.kofl.frontend.parser.lib.many
import me.devgabi.kofl.frontend.parser.lib.or
import me.devgabi.kofl.frontend.parser.lib.plus
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
