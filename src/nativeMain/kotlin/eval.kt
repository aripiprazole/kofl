class Evaluator(private val globalEnvironment: MutableEnvironment) {
  private val locals = mutableMapOf<Expr, Int>()

  fun eval(stmts: List<Stmt>, environment: MutableEnvironment = globalEnvironment): List<KoflObject> =
    stmts.map { stmt ->
      eval(stmt, environment)
    }

  private fun eval(stmts: List<Expr>, environment: MutableEnvironment = globalEnvironment): List<KoflObject> =
    stmts.map { stmt ->
      eval(stmt, environment)
    }

  //
  // STATEMENTS
  //
  private fun eval(stmt: Stmt, environment: MutableEnvironment): KoflObject = when (stmt) {
    is Stmt.WhileStmt -> eval(stmt, environment)
    is Stmt.Block -> eval(stmt, environment)
    is Stmt.VarDecl -> eval(stmt, environment)
    is Stmt.ValDecl -> eval(stmt, environment)
    is Stmt.ExprStmt -> eval(stmt, environment)
    is Stmt.ReturnStmt -> eval(stmt, environment)
  }

  private fun eval(stmt: Stmt.ExprStmt, environment: MutableEnvironment): KoflObject {
    return eval(stmt.expr, environment)
  }

  private fun eval(stmt: Stmt.ValDecl, environment: MutableEnvironment): KoflObject {
    environment.define(stmt.name, eval(stmt.value, environment).asKoflValue())

    return KoflUnit
  }

  private fun eval(stmt: Stmt.VarDecl, environment: MutableEnvironment): KoflObject {
    environment.define(stmt.name, eval(stmt.value, environment).asKoflValue(mutable = true))

    return KoflUnit
  }

  private fun eval(stmt: Stmt.Block, environment: MutableEnvironment): KoflObject {
    val localEnvironment = MutableEnvironment(environment)

    stmt.decls.forEach { lStmt ->
      eval(lStmt, localEnvironment)
    }

    return KoflUnit
  }

  // TODO: add break and continue
  private fun eval(stmt: Stmt.WhileStmt, environment: MutableEnvironment): KoflObject {
    val localEnvironment = MutableEnvironment(environment)

    while (eval(stmt.condition, localEnvironment).isTruthy()) {
      eval(stmt.body, localEnvironment)
    }

    return KoflUnit
  }

  private fun eval(stmt: Stmt.ReturnStmt, environment: MutableEnvironment): KoflObject {
    throw Return(eval(stmt.expr, environment))
  }

  //
// EXPRESSIONS
//
  private fun eval(expr: Expr, environment: MutableEnvironment): KoflObject = when (expr) {
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
    is Expr.AnonymousFunc -> eval(expr)
  }

  private fun eval(grouping: Expr.Grouping, environment: MutableEnvironment): KoflObject {
    return eval(grouping.expr, environment)
  }

  private fun eval(expr: Expr.Literal): KoflObject {
    return expr.value.asKoflObject()
  }

  private fun eval(expr: Expr.Var, environment: MutableEnvironment): KoflObject {
    return lookup(expr.name, expr, environment).value
  }

  private fun eval(expr: Expr.Assign, environment: MutableEnvironment): KoflObject {
    return assign(expr.name, expr, environment).asKoflObject()
  }

  private fun eval(expr: Expr.Logical, environment: MutableEnvironment): KoflObject {
    val left = eval(expr.left, environment)
    val right = eval(expr.right, environment)

    return when (expr.op.type) {
      TokenType.Or -> (left.isTruthy() || right.isTruthy()).asKoflObject()
      TokenType.And -> (left.isTruthy() && right.isTruthy()).asKoflObject()
      else -> KoflUnit
    }
  }

  private fun eval(expr: Expr.IfExpr, environment: MutableEnvironment): KoflObject {
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

  private fun eval(expr: Expr.Unary, environment: MutableEnvironment): KoflObject {
    return when (expr.op.type) {
      TokenType.Plus -> +eval(expr.right, environment).asKoflNumber()
      TokenType.Minus -> -eval(expr.right, environment).asKoflNumber()
      TokenType.Bang -> !eval(expr.right, environment).toString().toBoolean().asKoflBoolean()

      else -> throw IllegalOperationError(expr.op, "unary")
    }
  }

  private fun eval(expr: Expr.Call, environment: MutableEnvironment): KoflObject {
    val arguments = eval(expr.arguments, environment)

    return when (val callee = eval(expr.calle, environment)) {
      is KoflCallable -> when (callee.arity) {
        arguments.size -> try {
          callee(arguments, environment)
        } catch (aReturn: Return) {
          aReturn.value
        }
        else -> throw KoflRuntimeError("expecting ${callee.arity} args but got ${arguments.size}")
      }
      else -> throw TypeError("can't call a non-callable expr")
    }
  }

  private fun eval(expr: Expr.Binary, environment: MutableEnvironment): KoflObject {
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
        else -> throw IllegalOperationError(expr.op, "number binary op")
      }
    }

    return when (expr.op.type) {
      TokenType.EqualEqual -> (left == right).asKoflObject()
      TokenType.BangEqual -> (left != right).asKoflObject()

      TokenType.Plus -> when (left) {
        is KoflString -> (left + right.asKoflObject()).asKoflObject()
        else -> throw IllegalOperationError(expr.op, "add types that aren't string or number")
      }

      else -> throw IllegalOperationError(expr.op, "binary general op")
    }
  }

  private fun eval(expr: Expr.Func, environment: MutableEnvironment): KoflObject {
    return environment.define(expr.name, KoflCallable.Func(expr, this).asKoflValue()).asKoflObject()
  }

  private fun eval(expr: Expr.AnonymousFunc): KoflObject {
    return KoflCallable.AnonymousFunc(expr, this)
  }

  fun resolve(expr: Expr, depth: Int) {
    locals[expr] = depth
  }

  // utils
  @OptIn(KoflResolverInternals::class)
  private fun lookup(name: Token, expr: Expr, environment: MutableEnvironment): KoflValue {
    val distance = locals[expr] ?:  return globalEnvironment[name]

    return environment.getAt(distance, name)
  }

  @OptIn(KoflResolverInternals::class)
  private fun assign(name: Token, expr: Expr, environment: MutableEnvironment) {
    val distance = locals[expr] ?: return Unit.also {
      globalEnvironment[name] = eval(expr, environment)
    }

    environment.setAt(distance, name, eval(expr, environment))
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
