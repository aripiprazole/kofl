@kotlin.native.concurrent.ThreadLocal
private val globalEnvironment = Environment()

fun eval(stmts: List<Stmt>, environment: Environment = globalEnvironment): List<Any> = stmts.map { stmt ->
  eval(stmt, environment)
}

fun eval(stmt: Stmt, environment: Environment = globalEnvironment): Any {
  fun eval(stmt: Stmt.ExprStmt): Any {
    return eval(stmt.expr, environment)
  }

  fun eval(stmt: Stmt.PrintStmt) {
    println(eval(stmt.expr))
  }

  fun eval(stmt: Stmt.ValDecl): Any {
    return environment.define(stmt.name, eval(stmt.value, environment))
  }

  fun eval(stmt: Stmt.VarDecl): Any {
    return environment.define(stmt.name, eval(stmt.value), immutable = false)
  }

  // TODO: do not mutate environment
  fun eval(stmt: Stmt.Block): Any {
    val localEnvironment = Environment(environment)

    return stmt.decls.forEach { lStmt ->
      eval(lStmt, localEnvironment)
    }
  }

  // TODO: add break and continue
  fun eval(stmt: Stmt.WhileStmt): Any {
    val localEnvironment = Environment(environment)

    while (eval(stmt.condition, localEnvironment) == true) {
      eval(stmt.body, localEnvironment)
    }

    return Unit
  }

  return when (stmt) {
    is Stmt.WhileStmt -> eval(stmt)
    is Stmt.Block -> eval(stmt)
    is Stmt.VarDecl -> eval(stmt)
    is Stmt.ValDecl -> eval(stmt)
    is Stmt.PrintStmt -> eval(stmt)
    is Stmt.ExprStmt -> eval(stmt)
  }
}

fun eval(expr: Expr, environment: Environment = globalEnvironment): Any {
  fun eval(grouping: Expr.Grouping): Any {
    return eval(grouping.expr)
  }

  fun eval(expr: Expr.Literal): Any {
    return expr.value
  }

  fun eval(expr: Expr.Var): Any {
    return environment[expr.name].value
  }

  fun eval(expr: Expr.Assign): Any = eval(expr.value).also { value ->
    environment[expr.name] = value
  }

  fun eval(expr: Expr.Logical): Any {
    val left = eval(expr.left)
    val right = eval(expr.right)

    return when (expr.op.type) {
      TokenType.Or -> left == true || right == true
      TokenType.And -> left == true && right == true
      else -> Unit
    }
  }

  fun eval(expr: Expr.IfExpr): Any {
    val localEnvironment = Environment(environment)

    if (eval(expr.condition) == true) {
      return eval(expr.thenBranch, localEnvironment).lastOrNull() ?: Unit
    } else {
      expr.elseBranch?.let { stmts ->
        return eval(stmts, localEnvironment).lastOrNull() ?: Unit
      }
    }

    return Unit
  }

  fun eval(expr: Expr.Unary): Any {
    return when (expr.op.type) {
      TokenType.Plus -> +eval(expr.right).toString().toDouble()
      TokenType.Minus -> -eval(expr.right).toString().toDouble()
      TokenType.Bang -> !eval(expr.right).toString().toBoolean()

      else -> throw UnsupportedOpError(expr.op, "unary")
    }
  }

  fun eval(expr: Expr.Binary): Any {
    val left = eval(expr.left)
    val right = eval(expr.right)

    if (expr.op.type.isNumberOp() && left is Double && right is Double) {
      val leftN = eval(expr.left).toString().toDouble()
      val rightN = eval(expr.right).toString().toDouble()

      return when (expr.op.type) {
        TokenType.Minus -> leftN - rightN
        TokenType.Plus -> leftN + rightN
        TokenType.Star -> leftN * rightN
        TokenType.Slash -> leftN / rightN
        TokenType.GreaterEqual -> leftN >= rightN
        TokenType.Greater -> leftN > rightN
        TokenType.Less -> leftN < rightN
        TokenType.LessEqual -> leftN <= rightN
        else -> throw UnsupportedOpError(expr.op, "number binary op")
      }
    }

    return when (expr.op.type) {
      TokenType.EqualEqual -> left == right
      TokenType.BangEqual -> left != right

      TokenType.Plus -> if (left is String) left + right else
        throw UnsupportedOpError(expr.op)

      else -> throw UnsupportedOpError(expr.op, "binary general op")
    }
  }

  return when (expr) {
    is Expr.Binary -> eval(expr)
    is Expr.IfExpr -> eval(expr)
    is Expr.Unary -> eval(expr)
    is Expr.Grouping -> eval(expr)
    is Expr.Assign -> eval(expr)
    is Expr.Literal -> eval(expr)
    is Expr.Var -> eval(expr)
    is Expr.Logical -> eval(expr)
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
