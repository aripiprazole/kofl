@kotlin.native.concurrent.ThreadLocal
private val globalEnvironment = Environment()

fun eval(stmts: List<Stmt>, environment: Environment = globalEnvironment): List<Any> = stmts.map { stmt ->
  eval(stmt, environment)
}

fun eval(stmt: Stmt, environment: Environment = globalEnvironment): Any {
  fun eval(exprStmt: Stmt.ExprStmt): Any {
    return eval(exprStmt.expr, environment)
  }

  fun eval(printStmt: Stmt.PrintStmt) {
    println(eval(printStmt.expr))
  }

  fun eval(valDecl: Stmt.ValDecl): Any {
    return environment.define(valDecl.name, eval(valDecl.value, environment))
  }

  fun eval(varDecl: Stmt.VarDecl): Any {
    return environment.define(varDecl.name, eval(varDecl.value), immutable = false)
  }

  // TODO: do not mutate environment
  fun eval(block: Stmt.Block): Any {
    val localEnvironment = Environment(environment)

    return block.decls.forEach { stmt ->
      eval(stmt, localEnvironment)
    }
  }

  fun eval(whileStmt: Stmt.WhileStmt): Any {
    val localEnvironment = Environment(environment)

    while (eval(whileStmt.condition, localEnvironment) == true) {
      eval(whileStmt.body, localEnvironment)
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

  fun eval(literal: Expr.Literal): Any {
    return literal.value
  }

  fun eval(varExpr: Expr.Var): Any {
    return environment[varExpr.name].value
  }

  fun eval(assign: Expr.Assign): Any = eval(assign.value).also { value ->
    environment[assign.name] = value
  }

  fun eval(logical: Expr.Logical): Any {
    val left = eval(logical.left)
    val right = eval(logical.right)

    return when (logical.op.type) {
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

  fun eval(binary: Expr.Binary): Any {
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
        else -> throw UnsupportedOpError(binary.op, "number binary op")
      }
    }

    return when (binary.op.type) {
      TokenType.EqualEqual -> left == right
      TokenType.BangEqual -> left != right

      TokenType.Plus -> if (left is String) left + right else
        throw UnsupportedOpError(binary.op)

      else -> throw UnsupportedOpError(binary.op, "binary general op")
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
