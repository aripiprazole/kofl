@kotlin.native.concurrent.ThreadLocal
private val globalEnvironment = MutableEnvironment(NativeEnvironment())

fun eval(stmts: List<Stmt>, environment: MutableEnvironment = globalEnvironment): List<KoflObject> =
  stmts.map { stmt ->
    eval(stmt, environment)
  }

fun eval(stmts: List<Expr>, environment: MutableEnvironment = globalEnvironment): List<KoflObject> =
  stmts.map { stmt ->
    eval(stmt, environment)
  }

fun eval(stmt: Stmt, environment: MutableEnvironment = globalEnvironment): KoflObject {
  fun eval(stmt: Stmt.ExprStmt): KoflObject {
    return eval(stmt.expr, environment)
  }

  fun eval(stmt: Stmt.PrintStmt): KoflObject {
    return println(eval(stmt.expr)).asKoflObject()
  }

  fun eval(stmt: Stmt.ValDecl): KoflObject {
    environment.define(stmt.name, eval(stmt.value, environment).asKoflValue())

    return KoflUnit
  }

  fun eval(stmt: Stmt.VarDecl): KoflObject {
    environment.define(stmt.name, eval(stmt.value, environment).asKoflValue(mutable = true))

    return KoflUnit
  }

  fun eval(stmt: Stmt.Block): KoflObject {
    val localEnvironment = MutableEnvironment(environment)

    stmt.decls.forEach { lStmt ->
      eval(lStmt, localEnvironment)
    }

    return KoflUnit
  }

  // TODO: add break and continue
  fun eval(stmt: Stmt.WhileStmt): KoflObject {
    val localEnvironment = MutableEnvironment(environment)

    while (eval(stmt.condition, localEnvironment).isTruthy()) {
      eval(stmt.body, localEnvironment)
    }

    return KoflUnit
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

fun eval(expr: Expr, environment: MutableEnvironment = globalEnvironment): KoflObject {
  fun eval(grouping: Expr.Grouping): KoflObject {
    return eval(grouping.expr)
  }

  fun eval(expr: Expr.Literal): KoflObject {
    return expr.value.asKoflObject()
  }

  fun eval(expr: Expr.Var): KoflObject {
    return environment[expr.name].value
  }

  fun eval(expr: Expr.Assign): KoflObject = eval(expr.value).also { value ->
    environment[expr.name] = value.asKoflObject()
  }

  fun eval(expr: Expr.Logical): KoflObject {
    val left = eval(expr.left)
    val right = eval(expr.right)

    return when (expr.op.type) {
      TokenType.Or -> (left.isTruthy() || right.isTruthy()).asKoflObject()
      TokenType.And -> (left.isTruthy() && right.isTruthy()).asKoflObject()
      else -> KoflUnit
    }
  }

  fun eval(expr: Expr.IfExpr): KoflObject {
    val localEnvironment = MutableEnvironment(environment)

    if (eval(expr.condition) == KoflBoolean.True) {
      return eval(expr.thenBranch, localEnvironment).lastOrNull() ?: KoflUnit
    } else {
      expr.elseBranch?.let { stmts ->
        return eval(stmts, localEnvironment).lastOrNull() ?: KoflUnit
      }
    }

    return KoflUnit
  }

  fun eval(expr: Expr.Unary): KoflObject {
    return when (expr.op.type) {
      TokenType.Plus -> +eval(expr.right).toString().toDouble().asKoflNumber()
      TokenType.Minus -> -eval(expr.right).toString().toDouble().asKoflNumber()
      TokenType.Bang -> !eval(expr.right).toString().toBoolean().asKoflBoolean()

      else -> throw UnsupportedOpError(expr.op, "unary")
    }
  }

  fun eval(expr: Expr.Call): KoflObject {
    val arguments = eval(expr.arguments)

    return when (val callee = eval(expr.calle)) {
      is KoflCallable -> when (callee.arity) {
        arguments.size -> callee(arguments, environment)
        else -> throw RuntimeError("expecting ${callee.arity} args but got ${arguments.size}")
      }
      else -> throw TypeError("can't call a non-callable expr")
    }
  }

  fun eval(expr: Expr.Binary): KoflObject {
    val left = eval(expr.left)
    val right = eval(expr.right)

    if (expr.op.type.isNumberOp() && left is KoflNumber<*> && right is KoflNumber<*>) {
      val leftN = eval(expr.left).unwrap().toString().toDouble().asKoflNumber()
      val rightN = eval(expr.right).unwrap().toString().toDouble().asKoflNumber()

      return when (expr.op.type) {
        TokenType.Minus -> leftN - rightN
        TokenType.Plus -> leftN + rightN
        TokenType.Star -> leftN * rightN
        TokenType.Slash -> leftN / rightN
        TokenType.GreaterEqual -> (leftN >= rightN).asKoflBoolean()
        TokenType.Greater -> (leftN > rightN).asKoflBoolean()
        TokenType.Less -> (leftN < rightN).asKoflBoolean()
        TokenType.LessEqual -> (leftN <= rightN).asKoflBoolean()
        else -> throw UnsupportedOpError(expr.op, "number binary op")
      }
    }

    return when (expr.op.type) {
      TokenType.EqualEqual -> (left == right).asKoflObject()
      TokenType.BangEqual -> (left != right).asKoflObject()

      TokenType.Plus -> when (left) {
        is KoflString -> (left + right.asKoflObject()).asKoflObject()
        else -> throw UnsupportedOpError(expr.op)
      }

      else -> throw UnsupportedOpError(expr.op, "binary general op")
    }
  }

  fun eval(expr: Expr.Func): KoflObject {
    return environment.define(expr.name, KoflCallable.Func(expr).asKoflValue()).asKoflObject()
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
    is Expr.Call -> eval(expr)
    is Expr.Func -> eval(expr)
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
