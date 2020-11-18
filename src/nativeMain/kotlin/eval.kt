object Evaluator : ExprVisitor<Any> {
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

      TokenType.Plus -> if(left is String) left + right else
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

  fun eval(expr: Expr): Any {
    return expr.accept(this)
  }
}

fun show(any: Any?) = when(any) {
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
