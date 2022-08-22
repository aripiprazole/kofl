@file:Suppress("MemberVisibilityCanBePrivate")

package me.devgabi.kofl.frontend.parser.grammar

import me.devgabi.kofl.frontend.Expr
import me.devgabi.kofl.frontend.Stmt
import me.devgabi.kofl.frontend.parser.lib.Grammar
import me.devgabi.kofl.frontend.parser.lib.Parser
import me.devgabi.kofl.frontend.parser.lib.combine
import me.devgabi.kofl.frontend.parser.lib.label
import me.devgabi.kofl.frontend.parser.lib.map
import me.devgabi.kofl.frontend.parser.lib.optional
import me.devgabi.kofl.frontend.parser.lib.or
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
