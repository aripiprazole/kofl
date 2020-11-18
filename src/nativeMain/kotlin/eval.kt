@ThreadLocal
object Evaluator : ExprVisitor<Any>, StmtVisitor<Any> {
  private val environment = Environment()

  fun eval(expr: Expr) = expr.accept(this)
  fun eval(stmt: Stmt) = stmt.accept(this)

  fun eval(stmts: Collection<Stmt>): Collection<Any> = stmts.map { stmt ->
    eval(stmt)
  }

  override fun visit(binary: Expr.Binary): Any {
    val left = eval(binary.left)
    val right = eval(binary.right)

    if (binary.op.type.isNumberOp() && left is Double && right is Double) {
      val leftN = eval(binary.left).toString().toDouble()
      val rightN = eval(binary.right).toString().toDouble()

      return when (binary.op.type) {
        TokenType.Minus -> leftN - rightN
        TokenType.Plus -> leftN + rightN
        TokenType.Star -> leftN * rightN
        TokenType.Slash -> leftN / rightN
        TokenType.GreaterEqual -> leftN >= rightN
        TokenType.Greater -> leftN > rightN
        TokenType.Less -> leftN < rightN
        TokenType.LessEqual -> leftN <= rightN
        else -> throw UnsupportedOpError(binary.op)
      }
    }

    return when (binary.op.type) {
      TokenType.EqualEqual -> left == right
      TokenType.BangEqual -> left != right

      TokenType.Plus -> if (left is String) left + right else
        throw UnsupportedOpError(binary.op)

      else -> throw UnsupportedOpError(binary.op)
    }
  }

  override fun visit(grouping: Expr.Grouping) = eval(grouping.expr)

  override fun visit(literal: Expr.Literal) = literal.value

  override fun visit(unary: Expr.Unary): Any {
    return when (unary.op.type) {
      TokenType.Plus -> +eval(unary.right).toString().toDouble()
      TokenType.Minus -> -eval(unary.right).toString().toDouble()
      TokenType.Bang -> !eval(unary.right).toString().toBoolean()

      else -> throw UnsupportedOpError(unary.op)
    }
  }

  override fun visit(exprStmt: Stmt.ExprStmt) = eval(exprStmt.expr)

  override fun visit(printStmt: Stmt.PrintStmt): Any {
    return println(eval(printStmt.expr))
  }

  override fun visit(valDecl: Stmt.ValDecl): Any {
    return environment.define(valDecl.name, eval(valDecl.value), immutable = true)
  }
}

fun show(any: Any?) = when (any) {
  null -> "NULL"
  is String -> "\"$any\""
  else -> any
}

private fun TokenType.isNumberOp() =
  this == TokenType.Minus
    || this == TokenType.Plus
    || this == TokenType.Star
    || this == TokenType.Slash
    || this == TokenType.GreaterEqual
    || this == TokenType.Greater
    || this == TokenType.Less
    || this == TokenType.LessEqual
