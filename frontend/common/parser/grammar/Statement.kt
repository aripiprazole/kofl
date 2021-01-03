package com.lorenzoog.kofl.frontend.parser.grammar

import com.lorenzoog.kofl.frontend.Stmt
import com.lorenzoog.kofl.frontend.parser.lib.*

internal object Statement : Grammar<Stmt>() {
  private val ExprStmt = label("expr-stmt")(
    combine(Expression, Semicolon) { stmt, _ ->
      Stmt.ExprStmt(stmt, line)
    }
  )

  private val ReturnStmt = label("return-stmt")(
    combine(Keywords.Return, Expression, Semicolon) { _, value, _ ->
      Stmt.ReturnStmt(value, line)
    }
  )

  override val rule: Parser<Stmt> = ReturnStmt or ExprStmt

  val Program = many(Statement) with EOF
}