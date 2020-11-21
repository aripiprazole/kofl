fun eval(stmts: List<Stmt>, environment: MutableEnvironment): List<KoflObject> =
  stmts.map { stmt ->
    eval(stmt, environment)
  }

fun eval(stmts: List<Expr>, environment: MutableEnvironment): List<KoflObject> =
  stmts.map { stmt ->
    eval(stmt, environment)
  }

//
// STATEMENTS
//
fun eval(stmt: Stmt, environment: MutableEnvironment): KoflObject = when (stmt) {
  is Stmt.WhileStmt -> eval(stmt, environment)
  is Stmt.Block -> eval(stmt, environment)
  is Stmt.VarDecl -> eval(stmt, environment)
  is Stmt.ValDecl -> eval(stmt, environment)
  is Stmt.ExprStmt -> eval(stmt, environment)
  is Stmt.ReturnStmt -> eval(stmt, environment)
}

fun eval(stmt: Stmt.ExprStmt, environment: MutableEnvironment): KoflObject {
  return eval(stmt.expr, environment)
}

fun eval(stmt: Stmt.ValDecl, environment: MutableEnvironment): KoflObject {
  environment.define(stmt.name, eval(stmt.value, environment).asKoflValue())

  return KoflUnit
}

fun eval(stmt: Stmt.VarDecl, environment: MutableEnvironment): KoflObject {
  environment.define(stmt.name, eval(stmt.value, environment).asKoflValue(mutable = true))

  return KoflUnit
}

fun eval(stmt: Stmt.Block, environment: MutableEnvironment): KoflObject {
  val localEnvironment = MutableEnvironment(environment)

  stmt.decls.forEach { lStmt ->
    eval(lStmt, localEnvironment)
  }

  return KoflUnit
}

// TODO: add break and continue
fun eval(stmt: Stmt.WhileStmt, environment: MutableEnvironment): KoflObject {
  val localEnvironment = MutableEnvironment(environment)

  while (eval(stmt.condition, localEnvironment).isTruthy()) {
    eval(stmt.body, localEnvironment)
  }

  return KoflUnit
}

fun eval(stmt: Stmt.ReturnStmt, environment: MutableEnvironment): KoflObject {
  throw Return(eval(stmt.expr, environment))
}

//
// EXPRESSIONS
//
fun eval(expr: Expr, environment: MutableEnvironment): KoflObject = when (expr) {
  is Expr.Binary -> eval(expr, environment)
  is Expr.IfExpr -> eval(expr, environment)
  is Expr.Unary -> eval(expr, environment)
  is Expr.Grouping -> eval(expr, environment)
  is Expr.Assign -> eval(expr, environment)
  is Expr.Literal -> eval(expr)
  is Expr.Var -> eval(expr, environment)
  is Expr.Logical -> eval(expr, environment)
  is Expr.Call -> eval(expr, environment)
  is Expr.Func -> eval(expr, environment)
}

fun eval(grouping: Expr.Grouping, environment: MutableEnvironment): KoflObject {
  return eval(grouping.expr, environment)
}

fun eval(expr: Expr.Literal): KoflObject {
  return expr.value.asKoflObject()
}

fun eval(expr: Expr.Var, environment: MutableEnvironment): KoflObject {
  return environment[expr.name].value
}

fun eval(expr: Expr.Assign, environment: MutableEnvironment): KoflObject = eval(expr.value, environment).also { value ->
  environment[expr.name] = value.asKoflObject()
}

fun eval(expr: Expr.Logical, environment: MutableEnvironment): KoflObject {
  val left = eval(expr.left, environment)
  val right = eval(expr.right, environment)

  return when (expr.op.type) {
    TokenType.Or -> (left.isTruthy() || right.isTruthy()).asKoflObject()
    TokenType.And -> (left.isTruthy() && right.isTruthy()).asKoflObject()
    else -> KoflUnit
  }
}

fun eval(expr: Expr.IfExpr, environment: MutableEnvironment): KoflObject {
  val localEnvironment = MutableEnvironment(environment)

  if (eval(expr.condition, environment) == KoflBoolean.True) {
    return eval(expr.thenBranch, localEnvironment).lastOrNull() ?: KoflUnit
  } else {
    expr.elseBranch?.let { stmts ->
      return eval(stmts, localEnvironment).lastOrNull() ?: KoflUnit
    }
  }

  return KoflUnit
}

fun eval(expr: Expr.Unary, environment: MutableEnvironment): KoflObject {
  return when (expr.op.type) {
    TokenType.Plus -> +eval(expr.right, environment).asKoflNumber()
    TokenType.Minus -> -eval(expr.right, environment).asKoflNumber()
    TokenType.Bang -> !eval(expr.right, environment).toString().toBoolean().asKoflBoolean()

    else -> throw UnsupportedOpError(expr.op, "unary")
  }
}

fun eval(expr: Expr.Call, environment: MutableEnvironment): KoflObject {
  val arguments = eval(expr.arguments, environment)

  return when (val callee = eval(expr.calle, environment)) {
    is KoflCallable -> when (callee.arity) {
      arguments.size -> try {
        callee(arguments, environment)
      } catch (aReturn: Return) {
        aReturn.value
      }
      else -> throw RuntimeError("expecting ${callee.arity} args but got ${arguments.size}")
    }
    else -> throw TypeError("can't call a non-callable expr")
  }
}

fun eval(expr: Expr.Binary, environment: MutableEnvironment): KoflObject {
  val left = eval(expr.left, environment)
  val right = eval(expr.right, environment)

  if (expr.op.type.isNumberOp() && left is KoflNumber<*> && right is KoflNumber<*>) {
    val leftN = eval(expr.left, environment).asKoflNumber()
    val rightN = eval(expr.right, environment).asKoflNumber()

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

fun eval(expr: Expr.Func, environment: MutableEnvironment): KoflObject {
  return environment.define(expr.name, KoflCallable.Func(expr).asKoflValue()).asKoflObject()
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
