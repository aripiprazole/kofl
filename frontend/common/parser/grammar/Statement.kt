@file:Suppress("MemberVisibilityCanBePrivate")

package com.lorenzoog.kofl.frontend.parser.grammar

import com.lorenzoog.kofl.frontend.Stmt
import com.lorenzoog.kofl.frontend.parser.lib.*

internal object Statement : Grammar<Stmt>() {
  val IfStmt = label("if-stmt")(
    If.If map { Stmt.ExprStmt(it, line) }
  )

  val ExprStmt = label("expr-stmt")(
    combine(Func, Semicolon) { stmt, _ ->
      Stmt.ExprStmt(stmt, line)
    }
  )

  val ReturnStmt = label("return-stmt")(
    combine(Keywords.Return, Func, Semicolon) { _, value, _ ->
      Stmt.ReturnStmt(value, line)
    }
  )

  override val rule: Parser<Stmt> = Declaration or IfStmt or ReturnStmt or ExprStmt
}