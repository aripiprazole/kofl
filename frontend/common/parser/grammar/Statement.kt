package com.lorenzoog.kofl.frontend.parser.grammar

import com.lorenzoog.kofl.frontend.Stmt
import com.lorenzoog.kofl.frontend.parser.lib.*

internal object Statement : Grammar<Stmt>() {
  private val FuncStmt = label("func-stmt")(
    (Func.Func + Semicolon.optional()) map { Stmt.ExprStmt(it.first, line) }
  )

  private val IfStmt = label("if-stmt")(
    (If.If + Semicolon.optional()) map { Stmt.ExprStmt(it.first, line) }
  )

  private val ExprStmt = label("expr-stmt")(
    combine(Func, Semicolon) { stmt, _ ->
      Stmt.ExprStmt(stmt, line)
    }
  )

  private val ReturnStmt = label("return-stmt")(
    combine(Keywords.Return, Func, Semicolon) { _, value, _ ->
      Stmt.ReturnStmt(value, line)
    }
  )

  override val rule: Parser<Stmt> = FuncStmt or IfStmt or ReturnStmt or ExprStmt

  val Block = many(Statement)
}