@file:Suppress("MemberVisibilityCanBePrivate")

package com.lorenzoog.kofl.frontend.parser.grammar

import com.lorenzoog.kofl.frontend.Stmt
import com.lorenzoog.kofl.frontend.parser.lib.*
import kotlin.native.concurrent.ThreadLocal

@ThreadLocal
internal object Statement : Grammar<Stmt>() {
  val IfStmt = label("if-stmt")(
    optionalSemicolon(If.If map { Stmt.ExprStmt(it, line) })
  )

  val ExprStmt = label("expr-stmt")(
    semicolon(Func map { expr -> Stmt.ExprStmt(expr, line) })
  )

  val ReturnStmt = label("return-stmt")(
    semicolon(combine(Keywords.Return, Func) { _, value ->
      Stmt.ReturnStmt(value, line)
    })
  )

  val WhileStmt = label("while-stmt")(
    optionalSemicolon(combine(Keywords.While, Func, Body) { _, condition, body ->
      Stmt.WhileStmt(condition, body, line)
    })
  )

  val REPL = many(this) + EOF

  override val rule: Parser<Stmt> = Declaration or WhileStmt or IfStmt or ReturnStmt or ExprStmt
}