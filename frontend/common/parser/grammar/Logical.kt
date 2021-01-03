package com.lorenzoog.kofl.frontend.parser.grammar

import com.lorenzoog.kofl.frontend.Expr
import com.lorenzoog.kofl.frontend.parser.lib.*

internal object Logical : Grammar<Expr>() {
  private val Comparison = label("and")(
    combine(Math, many((Greater or GreaterEqual or Less or LessEqual) with Math)) { lhs, rest ->
      rest.fold(lhs) { acc, (op, rhs) ->
        Expr.Logical(acc, op, rhs, line)
      }
    }
  )

  private val Equality = label("equality")(
    combine(Comparison, many((EqualEqual or BangEqual) with Comparison)) { lhs, rest ->
      rest.fold(lhs) { acc, (op, rhs) ->
        Expr.Logical(acc, op, rhs, line)
      }
    }
  )

  private val LogicalAnd = label("logical-and")(
    combine(Equality, many(AndAnd with Equality)) { lhs, rest ->
      rest.fold(lhs) { acc, (op, rhs) ->
        Expr.Logical(acc, op, rhs, line)
      }
    }
  )

  private val LogicalOr = label("logical-or")(
    combine(LogicalAnd, many(OrOr with LogicalAnd)) { lhs, rest ->
      rest.fold(lhs) { acc, (op, rhs) ->
        Expr.Logical(acc, op, rhs, line)
      }
    }
  )

  override val rule: Parser<Expr> = LogicalOr
}