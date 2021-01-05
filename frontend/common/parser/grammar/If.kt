@file:Suppress("MemberVisibilityCanBePrivate")

package com.lorenzoog.kofl.frontend.parser.grammar

import com.lorenzoog.kofl.frontend.Expr
import com.lorenzoog.kofl.frontend.Stmt
import com.lorenzoog.kofl.frontend.parser.lib.Grammar
import com.lorenzoog.kofl.frontend.parser.lib.Parser
import com.lorenzoog.kofl.frontend.parser.lib.combine
import com.lorenzoog.kofl.frontend.parser.lib.label
import com.lorenzoog.kofl.frontend.parser.lib.map
import com.lorenzoog.kofl.frontend.parser.lib.optional
import com.lorenzoog.kofl.frontend.parser.lib.or
import kotlin.native.concurrent.ThreadLocal

@ThreadLocal
internal object If : Grammar<Expr>() {
  val ElseBody = label("else-branch")(
    combine(
      Keywords.Else,
      (Block or (Func.map { listOf<Stmt>(Stmt.ExprStmt(it, line)) }))
    ) { _, body ->
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
