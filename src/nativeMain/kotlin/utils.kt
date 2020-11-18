import platform.posix.fprintf
import platform.posix.stderr

object AstDumper : ExprVisitor<String>, StmtVisitor<String> {
  override fun visit(binary: Expr.Binary) = parenthesize(binary.op.lexeme, binary.left, binary.right)
  override fun visit(grouping: Expr.Grouping) = parenthesize("group", grouping.expr)
  override fun visit(unary: Expr.Unary) = parenthesize(unary.op.lexeme, unary.right)

  override fun visit(literal: Expr.Literal) =
    if (literal.value is String) "\"${literal.value}\"" else
      literal.value.toString()

  private fun parenthesize(op: String, vararg exprArray: Expr) = buildString {
    append("(").append(op)

    exprArray.forEach {
      append(" ").append(dump(it))
    }

    append(")")
  }

  fun dump(expr: Expr): String = expr.accept(this)
  fun dump(stmt: Stmt): String = stmt.accept(this)

  fun dump(stmts: Collection<Stmt>) {
    print(BLUE_COLOR)
    println(stmts.joinToString("\n") { dump(it) })
    print(WHILE_COLOR)
  }

  override fun visit(exprStmt: Stmt.ExprStmt) = "(${dump(exprStmt.expr)})"
  override fun visit(printStmt: Stmt.PrintStmt) = parenthesize("print", printStmt.expr)
  override fun visit(valDecl: Stmt.ValDecl) = parenthesize("define", Expr.Literal(valDecl.name), valDecl.value)
}

@Suppress("SpellCheckingInspection")
fun printerr(msg: String) {
  fprintf(stderr, RED_COLOR + "$msg\n" + WHILE_COLOR)
}
