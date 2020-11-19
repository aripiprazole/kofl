@ThreadLocal
object Evaluator : ExprVisitor<Any>, StmtVisitor<Any> {
  fun eval(stmts: List<Stmt>): List<Any> = stmts.map { stmt ->
    eval(stmt)
  }

  private var environment = Environment()

  private fun eval(expr: Expr) = expr.accept(this)
  private fun eval(stmt: Stmt) = stmt.accept(this)

  override fun visit(exprStmt: Stmt.ExprStmt) = eval(exprStmt.expr)
  override fun visit(printStmt: Stmt.PrintStmt) = println(eval(printStmt.expr))
  override fun visit(valDecl: Stmt.ValDecl) =
    environment.define(valDecl.name, eval(valDecl.value))

  override fun visit(varDecl: Stmt.VarDecl) =
    environment.define(varDecl.name, eval(varDecl.value), immutable = false)

  // TODO: do not mutate environment
  override fun visit(block: Stmt.Block) {
    val previous = environment
    val localEnvironment = Environment(environment)

    return try {
      environment = localEnvironment

      block.decls.forEach { stmt -> eval(stmt) }
    } finally {
      environment = previous
    }
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
  override fun visit(varExpr: Expr.Var) = environment[varExpr.name].value
  override fun visit(assign: Expr.Assign) = eval(assign.value).also { value ->
    environment[assign.name] = value
  }

  override fun visit(unary: Expr.Unary): Any {
    return when (unary.op.type) {
      TokenType.Plus -> +eval(unary.right).toString().toDouble()
      TokenType.Minus -> -eval(unary.right).toString().toDouble()
      TokenType.Bang -> !eval(unary.right).toString().toBoolean()

      else -> throw UnsupportedOpError(unary.op)
    }
  }

  override fun visit(ifExpr: Expr.IfExpr): Any {
    if(eval(ifExpr.condition) == true) {
      return eval(ifExpr.thenBranch).lastOrNull() ?: Unit
    } else {
      ifExpr.elseBranch?.let { stmts ->
        return eval(stmts).lastOrNull() ?: Unit
      }
    }

    return Unit
  }
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
