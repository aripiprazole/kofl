package com.lorenzoog.kofl.interpreter

import com.lorenzoog.kofl.frontend.Expr
import com.lorenzoog.kofl.frontend.Stmt
import com.lorenzoog.kofl.frontend.Token
import com.lorenzoog.kofl.frontend.TokenType

class Return(val value: KoflObject) : RuntimeException(null, null)

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

class CodeEvaluator(
  private val locals: Map<Expr, Int>,
  private val typeEnvironment: TypeEnvironment,
) : Evaluator<KoflObject> {
  //
  // STATEMENTS
  //
  override fun eval(stmt: Stmt, environment: MutableEnvironment): KoflObject = when (stmt) {
    is Stmt.WhileStmt -> evalWhileStmt(stmt, environment)
    is Stmt.Block -> evalBlockStmt(stmt, environment)
    is Stmt.VarDecl -> evalVarDeclStmt(stmt, environment)
    is Stmt.ValDecl -> evalValDeclStmt(stmt, environment)
    is Stmt.ExprStmt -> evalExprStmt(stmt, environment)
    is Stmt.ReturnStmt -> evalReturnStmt(stmt, environment)
    is Stmt.Type.Class -> evalTypeClassStmt(stmt, environment)
  }

  private fun evalExprStmt(stmt: Stmt.ExprStmt, environment: MutableEnvironment): KoflObject {
    return eval(stmt.expr, environment)
  }

  private fun evalValDeclStmt(stmt: Stmt.ValDecl, environment: MutableEnvironment): KoflObject {
    environment.define(stmt.name, eval(stmt.value, environment).asKoflValue())

    return KoflUnit
  }

  private fun evalTypeClassStmt(stmt: Stmt.Type.Class, environment: MutableEnvironment): KoflObject {
    val struct = KoflStruct(stmt.name.lexeme,
      fields = stmt.fields.mapKeys { (name) -> name.lexeme }.mapValues { (_, value) ->
        typeEnvironment.lookupType(value.lexeme)
      }
    )
    environment.define(stmt.name, struct.asKoflValue())

    return KoflUnit
  }

  private fun evalVarDeclStmt(stmt: Stmt.VarDecl, environment: MutableEnvironment): KoflObject {
    environment.define(stmt.name, eval(stmt.value, environment).asKoflValue(mutable = true))

    return KoflUnit
  }

  private fun evalBlockStmt(stmt: Stmt.Block, environment: MutableEnvironment): KoflObject {
    val localEnvironment = MutableEnvironment(environment)

    stmt.body.forEach { lStmt ->
      eval(lStmt, localEnvironment)
    }

    return KoflUnit
  }

  // TODO: add break and continue
  private fun evalWhileStmt(stmt: Stmt.WhileStmt, environment: MutableEnvironment): KoflObject {
    val localEnvironment = MutableEnvironment(environment)

    while (eval(stmt.condition, localEnvironment).isTruthy()) {
      eval(stmt.body, localEnvironment)
    }

    return KoflUnit
  }

  private fun evalReturnStmt(stmt: Stmt.ReturnStmt, environment: MutableEnvironment): KoflObject {
    throw Return(eval(stmt.expr, environment))
  }

  //
  // EXPRESSIONS
  //
  override fun eval(expr: Expr, environment: MutableEnvironment): KoflObject = when (expr) {
    is Expr.Binary -> evalBinaryExpr(expr, environment)
    is Expr.IfExpr -> evalIfExpr(expr, environment)
    is Expr.Unary -> evalUnaryExpr(expr, environment)
    is Expr.Grouping -> evalGroupingExpr(expr, environment)
    is Expr.Assign -> evalAssignExpr(expr, environment)
    is Expr.Literal -> evalLiteralExpr(expr)
    is Expr.Var -> evalVarExpr(expr, environment)
    is Expr.Logical -> evalLogicalExpr(expr, environment)
    is Expr.Get -> evalGetExpr(expr, environment)
    is Expr.ThisExpr -> evalThisExpr(expr, environment)
    is Expr.Set -> evalSetExpr(expr, environment)
    is Expr.Call -> evalCallExpr(expr, environment)
    is Expr.CommonFunc -> evalCommonFuncExpr(expr, environment)
    is Expr.ExtensionFunc -> evalExtensionFuncExpr(expr, environment)
    is Expr.AnonymousFunc -> evalAnonymousFuncExpr(expr)
    // do nothing 'cause the env already have the native func, that was made
    // just for tooling be easier
    is Expr.NativeFunc -> KoflUnit
  }

  private fun evalGroupingExpr(expr: Expr.Grouping, environment: MutableEnvironment): KoflObject {
    return eval(expr.expr, environment)
  }

  private fun evalLiteralExpr(expr: Expr.Literal): KoflObject {
    return expr.value.asKoflObject()
  }

  private fun evalVarExpr(expr: Expr.Var, environment: MutableEnvironment): KoflObject {
    return lookup(expr.name, expr, environment).value
  }

  private fun evalThisExpr(expr: Expr.ThisExpr, environment: MutableEnvironment): KoflObject {
    return lookup(expr.keyword, expr, environment).value
  }

  private fun evalAssignExpr(expr: Expr.Assign, environment: MutableEnvironment): KoflObject {
    return assign(expr.name, expr.value, environment).asKoflObject()
  }

  private fun evalLogicalExpr(expr: Expr.Logical, environment: MutableEnvironment): KoflObject {
    val left = eval(expr.left, environment)
    val right = eval(expr.right, environment)

    return when (expr.op.type) {
      TokenType.Or -> (left.isTruthy() || right.isTruthy()).asKoflObject()
      TokenType.And -> (left.isTruthy() && right.isTruthy()).asKoflObject()
      else -> KoflUnit
    }
  }

  private fun evalIfExpr(expr: Expr.IfExpr, environment: MutableEnvironment): KoflObject {
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

  private fun evalUnaryExpr(expr: Expr.Unary, environment: MutableEnvironment): KoflObject {
    return when (expr.op.type) {
      TokenType.Plus -> +eval(expr.right, environment).asKoflNumber()
      TokenType.Minus -> -eval(expr.right, environment).asKoflNumber()
      TokenType.Bang -> !eval(expr.right, environment).toString().toBoolean().asKoflBoolean()

      else -> throw IllegalOperationException(expr.op, "unary")
    }
  }

  private fun evalGetExpr(expr: Expr.Get, environment: MutableEnvironment): KoflObject {
    return when (val receiver = eval(expr.receiver, environment)) {
      is KoflInstance -> receiver.fields[expr.name.lexeme]?.value
        ?: throw UnresolvedVarException("$receiver.${expr.name.lexeme}")
      else -> throw TypeException("can't get fields from non-instances: $receiver")
    }
  }

  private fun evalSetExpr(expr: Expr.Set, environment: MutableEnvironment): KoflObject {
    when (val receiver = eval(expr.receiver, environment)) {
      is KoflInstance -> receiver.fields[expr.name.lexeme]?.also {
        if (it !is KoflValue.Mutable)
          throw IllegalOperationException(expr.name.lexeme, "update an immutable variable")

        it.value = eval(expr.value, environment)
      }
      else -> throw TypeException("can't set fields from non-instances")
    }

    return KoflUnit
  }


  private fun evalCallExpr(expr: Expr.Call, environment: MutableEnvironment): KoflObject {
    return try {
      val arguments = expr.arguments.mapKeys { (key) ->
        key?.lexeme
      }.mapValues { (_, value) ->
        eval(value, environment)
      }

      when (val callee = eval(expr.calle, environment)) {
        is KoflCallable -> callee(arguments, environment)
        else -> throw TypeException("can't call a non-callable expr")
      }
    } catch (aReturn: Return) {
      aReturn.value
    }
  }

  private fun evalBinaryExpr(expr: Expr.Binary, environment: MutableEnvironment): KoflObject {
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
        else -> throw IllegalOperationException(expr.op, "number binary op")
      }
    }

    return when (expr.op.type) {
      TokenType.EqualEqual -> (left == right).asKoflObject()
      TokenType.BangEqual -> (left != right).asKoflObject()

      TokenType.Plus -> when (left) {
        is KoflString -> (left + right.asKoflObject()).asKoflObject()
        else -> throw IllegalOperationException(expr.op, "add: $left and $right")
      }

      else -> throw IllegalOperationException(expr.op, "binary general op")
    }
  }

  @OptIn(ExperimentalStdlibApi::class)
  private fun evalCommonFuncExpr(expr: Expr.CommonFunc, environment: MutableEnvironment): KoflObject {
    val parameters = buildMap<String, KoflType> {
      expr.parameters.forEach { (name, type) ->
        set(name.lexeme, typeEnvironment.lookupType(type.lexeme))
      }
    }
    val returnType = typeEnvironment.lookupTypeOrNull(expr.returnType.toString()) ?: KoflUnit

    return environment
      .define(expr.name, Func(parameters, returnType, expr, this).asKoflValue())
      .asKoflObject()
  }

  @OptIn(ExperimentalStdlibApi::class)
  private fun evalExtensionFuncExpr(expr: Expr.ExtensionFunc, environment: MutableEnvironment): KoflObject {
//    val struct = lookup(expr.receiver, expr, environment).value as? KoflStruct ?: throw TypeException("struct type")
    val parameters = buildMap<String, KoflType> {
      expr.parameters.forEach { (name, type) ->
        set(name.lexeme, typeEnvironment.lookupType(type.lexeme))
      }
    }
    val returnType = typeEnvironment.lookupTypeOrNull(expr.returnType.toString()) ?: KoflUnit
    val receiver = typeEnvironment.lookupType(expr.receiver.lexeme)

    return ExtensionFunc(parameters, returnType, receiver, expr, this)
  }

  @OptIn(ExperimentalStdlibApi::class)
  private fun evalAnonymousFuncExpr(expr: Expr.AnonymousFunc): KoflObject {
    val parameters = buildMap<String, KoflType> {
      expr.parameters.forEach { (name, type) ->
        set(name.lexeme, typeEnvironment.lookupType(type.lexeme))
      }
    }
    val returnType = typeEnvironment.lookupTypeOrNull(expr.returnType.toString()) ?: KoflUnit

    return AnonymousFunc(parameters, returnType, expr, this)
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
