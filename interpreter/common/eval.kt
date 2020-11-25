package com.lorenzoog.kofl.interpreter

interface Evaluator<T> {
  fun eval(exprs: List<Expr>, environment: MutableEnvironment): List<T> {
    return exprs.map { eval(it, environment) }
  }

  fun eval(stmts: List<Stmt>, environment: MutableEnvironment): List<T> {
    return stmts.map { eval(it, environment) }
  }

  fun eval(stmt: Stmt, environment: MutableEnvironment): T
  fun eval(expr: Expr, environment: MutableEnvironment): T
}

class CodeEvaluator(private val locals: Map<Expr, Int>) : Evaluator<KoflObject> {
  //
  // STATEMENTS
  //
  override fun eval(stmt: Stmt, environment: MutableEnvironment): KoflObject = when (stmt) {
    is Stmt.WhileStmt -> eval(stmt, environment)
    is Stmt.Block -> eval(stmt, environment)
    is Stmt.VarDecl -> eval(stmt, environment)
    is Stmt.ValDecl -> eval(stmt, environment)
    is Stmt.ExprStmt -> eval(stmt, environment)
    is Stmt.ReturnStmt -> eval(stmt, environment)
    is Stmt.TypeDef.Struct -> eval(stmt, environment)
  }

  private fun eval(stmt: Stmt.ExprStmt, environment: MutableEnvironment): KoflObject {
    return eval(stmt.expr, environment)
  }

  private fun eval(stmt: Stmt.ValDecl, environment: MutableEnvironment): KoflObject {
    environment.define(stmt.name, eval(stmt.value, environment).asKoflValue())

    return KoflUnit
  }

  private fun eval(stmt: Stmt.TypeDef.Struct, environment: MutableEnvironment): KoflObject {
    environment.define(stmt.name, KoflStruct(stmt).asKoflValue())

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
  override fun eval(expr: Expr, environment: MutableEnvironment): KoflObject = when (expr) {
    is Expr.Binary -> eval(expr, environment)
    is Expr.IfExpr -> eval(expr, environment)
    is Expr.Unary -> eval(expr, environment)
    is Expr.Grouping -> eval(expr, environment)
    is Expr.Assign -> eval(expr, environment)
    is Expr.Literal -> eval(expr)
    is Expr.Var -> eval(expr, environment)
    is Expr.Logical -> eval(expr, environment)
    is Expr.Get -> eval(expr, environment)
    is Expr.ThisExpr -> eval(expr, environment)
    is Expr.Set -> eval(expr, environment)
    is Expr.Call -> eval(expr, environment)
    is Expr.CommonFunc -> eval(expr, environment)
    is Expr.ExtensionFunc -> eval(expr, environment)
    is Expr.AnonymousFunc -> eval(expr)
    // do nothing 'cause the env already have the native func, that was made
    // just for tooling be easier
    is Expr.NativeFunc -> KoflUnit
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

  private fun eval(expr: Expr.ThisExpr, environment: MutableEnvironment): KoflObject {
    return lookup(expr.keyword, expr, environment).value
  }

  private fun eval(expr: Expr.Assign, environment: MutableEnvironment): KoflObject {
    return assign(expr.name, expr.value, environment).asKoflObject()
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

  private fun eval(expr: Expr.Get, environment: MutableEnvironment): KoflObject {
    return when (val receiver = eval(expr.receiver, environment)) {
      is KoflInstance -> receiver[expr.name]?.value ?: throw UnresolvedFieldError(expr.name.lexeme, receiver)
      else -> throw TypeError("can't get fields from non-instances: $receiver")
    }
  }

  private fun eval(expr: Expr.Set, environment: MutableEnvironment): KoflObject {
    when (val receiver = eval(expr.receiver, environment)) {
      is KoflInstance -> receiver[expr.name] = eval(expr.value, environment)
      else -> throw TypeError("can't set fields from non-instances")
    }

    return KoflUnit
  }

  private fun eval(expr: Expr.Call, environment: MutableEnvironment): KoflObject {
    val arguments = expr.arguments
      .mapKeys { (key) ->
        key?.lexeme
      }
      .mapValues { (_, value) ->
        eval(value, environment)
      }

    return when (val callee = eval(expr.calle, environment)) {
      is KoflCallable -> when (callee.arity) {
        arguments.size -> try {
          callee(arguments, environment)
        } catch (aReturn: Return) {
          aReturn.value
        }
        else -> throw KoflRuntimeError("expecting ${callee.arity} args but got ${arguments.size} on call $callee")
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
        else -> throw IllegalOperationError(expr.op, "add: $left and $right")
      }

      else -> throw IllegalOperationError(expr.op, "binary general op")
    }
  }

  private fun eval(expr: Expr.CommonFunc, environment: MutableEnvironment): KoflObject {
    return environment.define(expr.name, KoflCallable.Func(expr, this).asKoflValue()).asKoflObject()
  }

  private fun eval(expr: Expr.ExtensionFunc, environment: MutableEnvironment): KoflObject {
    val struct = lookup(expr.receiver, expr, environment).value as? KoflStruct ?: throw TypeError("struct type")

    struct.functions[expr.name.lexeme] = KoflCallable.ExtensionFunc(expr, this)

    return KoflUnit
  }

  private fun eval(expr: Expr.AnonymousFunc): KoflObject {
    return KoflCallable.AnonymousFunc(expr, this)
  }

  // utils
  @OptIn(KoflResolverInternals::class)
  private fun lookup(name: Token, expr: Expr, environment: MutableEnvironment): KoflValue {
    val distance = locals[expr] ?: return environment[name]

    return environment.getAt(distance, name)
  }

  @OptIn(KoflResolverInternals::class)
  private fun assign(name: Token, expr: Expr, environment: MutableEnvironment) {
    val distance = locals[expr] ?: return Unit.also {
      environment[name] = eval(expr, environment)
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
