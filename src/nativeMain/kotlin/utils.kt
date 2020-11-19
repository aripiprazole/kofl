//import platform.posix.fprintf
//import platform.posix.stderr

object SimpleAstDumper : ExprVisitor<String>, StmtVisitor<String> {
  fun dump(stmts: Collection<Stmt>) {
    println("simple ast dump:")
    println(stmts.joinToString("\n") { dump(it) })
  }

  private fun dump(expr: Expr): String = expr.accept(this)
  private fun dump(stmt: Stmt): String = stmt.accept(this)

  override fun visit(binary: Expr.Binary) = parenthesize(binary.op.lexeme, binary.left, binary.right)
  override fun visit(grouping: Expr.Grouping) = parenthesize("group", grouping.expr)
  override fun visit(unary: Expr.Unary) = parenthesize(unary.op.lexeme, unary.right)
  override fun visit(assign: Expr.Assign) = parenthesize("set", Expr.Literal(assign.name), assign.value)
  override fun visit(varExpr: Expr.Var) = parenthesize("get", Expr.Literal(varExpr.name))
  override fun visit(logical: Expr.Logical) = parenthesize(logical.op.lexeme, logical.left, logical.right)

  override fun visit(literal: Expr.Literal) =
    if (literal.value is String) "\"${literal.value}\"" else
      literal.value.toString()

  override fun visit(ifExpr: Expr.IfExpr) =
    parenthesize("if (${dump(ifExpr.condition)})", *ifExpr.thenBranch.toTypedArray()) +
      if (ifExpr.elseBranch != null)
        parenthesize("else", *ifExpr.elseBranch.toTypedArray())
      else ""

  override fun visit(exprStmt: Stmt.ExprStmt) = "(${dump(exprStmt.expr)})"
  override fun visit(printStmt: Stmt.PrintStmt) = parenthesize("print", printStmt.expr)
  override fun visit(valDecl: Stmt.ValDecl) = parenthesize("val", Expr.Literal(valDecl.name), valDecl.value)
  override fun visit(varDecl: Stmt.VarDecl) = parenthesize("var", Expr.Literal(varDecl.name), varDecl.value)
  override fun visit(block: Stmt.Block) = parenthesize("block", *block.decls.toTypedArray())
  override fun visit(whileStmt: Stmt.WhileStmt) = parenthesize("while", *whileStmt.body.toTypedArray())

  private fun parenthesize(op: String, vararg stmts: Stmt) = buildString {
    append("(").append(op)
    stmts.forEach { append(" ").append(dump(it)) }
    append(")")
  }

  private fun parenthesize(op: String, vararg expressions: Expr) = buildString {
    append("(").append(op)
    expressions.forEach { append(" ").append(dump(it)) }
    append(")")
  }

}

@Suppress("SpellCheckingInspection")
fun printerr(msg: String = "") {
//  fprintf(stderr, "$msg\n\n")
  println(msg)
}
