package com.lorenzoog.kofl.interpreter

class CodeEvaluator(private val locals: Map<Expr, Int>) : Expr.Visitor<KoflObject>, Stmt.Visitor<KoflObject> {
  override fun visitExprStmt(stmt: Stmt.ExprStmt, environment: MutableEnvironment): KoflObject {
    return visit(stmt.expr, environment)
  }

  override fun visitValDeclStmt(stmt: Stmt.ValDecl, environment: MutableEnvironment): KoflObject {
    environment.define(stmt.name, visit(stmt.value, environment).asKoflValue())

    return KoflUnit
  }

  override fun visitStructTypedefStmt(stmt: Stmt.TypeDef.Struct, environment: MutableEnvironment): KoflObject {
    environment.define(stmt.name, KoflStruct(stmt).asKoflValue())

    return KoflUnit
  }

  override fun visitVarDeclStmt(stmt: Stmt.VarDecl, environment: MutableEnvironment): KoflObject {
    environment.define(stmt.name, visit(stmt.value, environment).asKoflValue(mutable = true))

    return KoflUnit
  }

  override fun visitBlockStmt(stmt: Stmt.Block, environment: MutableEnvironment): KoflObject {
    val localEnvironment = MutableEnvironment(environment)

    stmt.decls.forEach { lStmt ->
      visit(lStmt, localEnvironment)
    }

    return KoflUnit
  }

  // TODO: add break and continue
  override fun visitWhileStmt(stmt: Stmt.WhileStmt, environment: MutableEnvironment): KoflObject {
    val localEnvironment = MutableEnvironment(environment)

    while (visit(stmt.condition, localEnvironment).isTruthy()) {
      visit(stmt.body, localEnvironment)
    }

    return KoflUnit
  }

  override fun visitReturnStmt(stmt: Stmt.ReturnStmt, environment: MutableEnvironment): KoflObject {
    throw Return(visit(stmt.expr, environment))
  }

  override fun visitGroupingExpr(expr: Expr.Grouping, environment: MutableEnvironment): KoflObject {
    return visit(expr.expr, environment)
  }

  override fun visitLiteralExpr(expr: Expr.Literal, environment: MutableEnvironment): KoflObject {
    return expr.value.asKoflObject()
  }

  override fun visitVarExpr(expr: Expr.Var, environment: MutableEnvironment): KoflObject {
    return lookup(expr.name, expr, environment).value
  }

  override fun visitThisExpr(expr: Expr.ThisExpr, environment: MutableEnvironment): KoflObject {
    return lookup(expr.keyword, expr, environment).value
  }

  override fun visitAssignExpr(expr: Expr.Assign, environment: MutableEnvironment): KoflObject {
    return assign(expr.name, expr.value, environment).asKoflObject()
  }

  override fun visitLogicalExpr(expr: Expr.Logical, environment: MutableEnvironment): KoflObject {
    val left = visit(expr.left, environment)
    val right = visit(expr.right, environment)

    return when (expr.op.type) {
      TokenType.Or -> (left.isTruthy() || right.isTruthy()).asKoflObject()
      TokenType.And -> (left.isTruthy() && right.isTruthy()).asKoflObject()
      else -> KoflUnit
    }
  }

  override fun visitIfExpr(expr: Expr.IfExpr, environment: MutableEnvironment): KoflObject {
    val localEnvironment = MutableEnvironment(environment)

    if (visit(expr.condition, environment).isTruthy()) {
      return visit(expr.thenBranch, localEnvironment).lastOrNull() ?: KoflUnit
    } else {
      expr.elseBranch?.let { stmts ->
        return visit(stmts, localEnvironment).lastOrNull() ?: KoflUnit
      }
    }

    return KoflUnit
  }

  override fun visitUnaryExpr(expr: Expr.Unary, environment: MutableEnvironment): KoflObject {
    return when (expr.op.type) {
      TokenType.Plus -> +visit(expr.right, environment).asKoflNumber()
      TokenType.Minus -> -visit(expr.right, environment).asKoflNumber()
      TokenType.Bang -> !visit(expr.right, environment).toString().toBoolean().asKoflBoolean()

      else -> throw IllegalOperationError(expr.op, "unary")
    }
  }

  override fun visitGetExpr(expr: Expr.Get, environment: MutableEnvironment): KoflObject {
    return when (val receiver = visit(expr.receiver, environment)) {
      is KoflInstance -> receiver[expr.name]?.value ?: throw UnresolvedFieldError(expr.name.lexeme, receiver)
      else -> throw TypeError("can't get fields from non-instances: $receiver")
    }
  }

  override fun visitSetExpr(expr: Expr.Set, environment: MutableEnvironment): KoflObject {
    when (val receiver = visit(expr.receiver, environment)) {
      is KoflInstance -> receiver[expr.name] = visit(expr.value, environment)
      else -> throw TypeError("can't set fields from non-instances")
    }

    return KoflUnit
  }

  override fun visitCallExpr(expr: Expr.Call, environment: MutableEnvironment): KoflObject {
    val arguments = visit(expr.arguments, environment)

    return when (val callee = visit(expr.calle, environment)) {
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

  override fun visitBinaryExpr(expr: Expr.Binary, environment: MutableEnvironment): KoflObject {
    val left = visit(expr.left, environment)
    val right = visit(expr.right, environment)

    if (expr.op.type.isNumberOp() && left is KoflNumber<*> && right is KoflNumber<*>) {
      val leftN = visit(expr.left, environment).asKoflNumber()
      val rightN = visit(expr.right, environment).asKoflNumber()

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

  override fun visitFuncExpr(expr: Expr.Func, environment: MutableEnvironment): KoflObject {
    return environment.define(expr.name, KoflCallable.Func(expr, this).asKoflValue()).asKoflObject()
  }

  override fun visitExtensionFuncExpr(expr: Expr.ExtensionFunc, environment: MutableEnvironment): KoflObject {
    val struct = lookup(expr.receiver, expr, environment).value as? KoflStruct ?: throw TypeError("struct type")

    struct.functions[expr.name.lexeme] = KoflCallable.ExtensionFunc(expr, this)

    return KoflUnit
  }

  override fun visitAnonymousFuncExpr(expr: Expr.AnonymousFunc, environment: MutableEnvironment): KoflObject {
    return KoflCallable.AnonymousFunc(expr, this)
  }

  override fun visitNativeFuncExpr(expr: Expr.NativeFunc, environment: MutableEnvironment): KoflObject {
    return KoflUnit
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
      environment[name] = visit(expr, environment)
    }

    environment.setAt(distance, name, visit(expr, environment))
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
