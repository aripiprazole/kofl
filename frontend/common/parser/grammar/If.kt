@file:Suppress("MemberVisibilityCanBePrivate")

package com.lorenzoog.kofl.frontend.parser.grammar

import com.lorenzoog.kofl.frontend.Expr
import com.lorenzoog.kofl.frontend.Stmt
import com.lorenzoog.kofl.frontend.parser.lib.*
import kotlin.native.concurrent.ThreadLocal

@ThreadLocal
internal object If : Grammar<Expr>() {
  val ElseBody = label("else-branch")(
    combine(Keywords.Else, (Block or (Func.map { listOf<Stmt>(Stmt.ExprStmt(it, line)) }))) { _, body ->
      body
    }
  )

  val If: Parser<Expr> = label("if")(
    combine(Keywords.If, Func, Body, ElseBody.optional()) { _, condition, thenBranch, elseBranch ->
      Expr.IfExpr(condition, thenBranch, elseBranch, line)
    }
  )

  override val rule = If
}