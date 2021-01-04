package com.lorenzoog.kofl.frontend.parser.grammar

import com.lorenzoog.kofl.frontend.Expr
import com.lorenzoog.kofl.frontend.Stmt
import com.lorenzoog.kofl.frontend.parser.lib.*

internal object If : Grammar<Expr>() {
  private val Block = combine(LeftBrace, lazied { Statement.Block }, RightBrace) { _, body, _ ->
    body
  }

  private val IfBody = label("if-body")(
    Block or combine(Keywords.Then, Func) { _, expr -> listOf(Stmt.ExprStmt(expr, line)) }
  )

  private val ElseBody = label("else-body")(
    combine(Keywords.Else, (Block or (Func.map { listOf<Stmt>(Stmt.ExprStmt(it, line)) }))) { _, body ->
      body
    }
  )

  val If: Parser<Expr> = label("if")(
    combine(Keywords.If, Func, IfBody, ElseBody.optional()) { _, condition, thenBranch, elseBranch ->
      Expr.IfExpr(condition, thenBranch, elseBranch, line)
    }
  )

  override val rule = If
}