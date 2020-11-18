import platform.posix.fprintf
import platform.posix.stderr
import platform.posix.stdout

object AstDumper : ExprVisitor<String> {
  override fun visit(binary: Expr.Binary) = parenthesize(binary.op.lexeme, binary.left, binary.right)
  override fun visit(grouping: Expr.Grouping) = parenthesize("grouping", grouping.expr)
  override fun visit(unary: Expr.Unary) = parenthesize(unary.op.lexeme, unary.right)

  override fun visit(literal: Expr.Literal) = if(literal.value is String)
    "\"${literal.value}\""
  else literal.value.toString()

  private fun parenthesize(op: String, vararg exprArray: Expr) = buildString {
    append("(").append(op)

    exprArray.forEach {
      append(" ").append(it.accept(this@AstDumper))
    }

    append(")")
  }

  fun dump(expr: Expr) {
    print(BLUE_COLOR)
    println("ast dump:")
    println(expr.accept(this))
    print(WHILE_COLOR)
  }
}

@Suppress("SpellCheckingInspection")
fun printerr(msg: String) {
  fprintf(stderr, RED_COLOR + "$msg\n")
}
