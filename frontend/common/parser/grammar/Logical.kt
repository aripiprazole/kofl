@file:Suppress("MemberVisibilityCanBePrivate")

package com.lorenzoog.kofl.frontend.parser.grammar

import com.lorenzoog.kofl.frontend.Expr
import com.lorenzoog.kofl.frontend.parser.lib.Grammar
import com.lorenzoog.kofl.frontend.parser.lib.Parser
import com.lorenzoog.kofl.frontend.parser.lib.combine
import com.lorenzoog.kofl.frontend.parser.lib.label
import com.lorenzoog.kofl.frontend.parser.lib.many
import com.lorenzoog.kofl.frontend.parser.lib.or
import com.lorenzoog.kofl.frontend.parser.lib.plus
import kotlin.native.concurrent.ThreadLocal

@ThreadLocal
internal object Logical : Grammar<Expr>() {
  val Comparison = label("and")(
    combine(Math, many((Greater or GreaterEqual or Less or LessEqual) + Math)) { lhs, rest ->
      rest.fold(lhs) { acc, (op, rhs) ->
        Expr.Logical(acc, op, rhs, line)
      }
    }
  )

  val Equality = label("equality")(
    combine(Comparison, many((EqualEqual or BangEqual) + Comparison)) { lhs, rest ->
      rest.fold(lhs) { acc, (op, rhs) ->
        Expr.Logical(acc, op, rhs, line)
      }
    }
  )

  val LogicalAnd = label("logical-and")(
    combine(Equality, many(AndAnd + Equality)) { lhs, rest ->
      rest.fold(lhs) { acc, (op, rhs) ->
        Expr.Logical(acc, op, rhs, line)
      }
    }
  )

  val LogicalOr = label("logical-or")(
    combine(LogicalAnd, many(OrOr + LogicalAnd)) { lhs, rest ->
      rest.fold(lhs) { acc, (op, rhs) ->
        Expr.Logical(acc, op, rhs, line)
      }
    }
  )

  override val rule: Parser<Expr> = LogicalOr
}