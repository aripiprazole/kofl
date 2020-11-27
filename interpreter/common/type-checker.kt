package com.lorenzoog.kofl.interpreter

import com.lorenzoog.kofl.frontend.*

const val MAX_STACK = 512_000

class TypeChecker(
  private val evaluator: CodeEvaluator,
  private val types: Stack<TypeEnvironment> = Stack(MAX_STACK)
) : Expr.Visitor<KoflType>, Stmt.Visitor<KoflType> {
  override fun visitAssignExpr(expr: Expr.Assign): KoflType {
    val expected = types.peek().lookup(expr.name.lexeme)
    val current = visitExpr(expr.value)

    if (expected != current)
      throw InvalidDeclaredTypeException(current, expected)

    return expected
  }

  override fun visitBinaryExpr(expr: Expr.Binary): KoflType {
    val left = visitExpr(expr.left)
    val right = visitExpr(expr.right)

    val tokens = listOf(
      TokenType.Plus, TokenType.Slash, TokenType.Minus, TokenType.Star,
      TokenType.Greater, TokenType.GreaterEqual, TokenType.Less, TokenType.LessEqual
    )

    if (expr.op.type in tokens) {
      if (left.isNumberType() && right.isNumberType())
        return KoflBoolean

      throw InvalidDeclaredTypeException(right, KoflInt)
    }

    if (left.isAssignableBy(KoflBoolean) && right.isAssignableBy(KoflBoolean))
      return KoflBoolean

    throw InvalidDeclaredTypeException(left.toString(), KoflBoolean.toString())
  }

  override fun visitLogicalExpr(expr: Expr.Logical): KoflType {
    val expected = visitExpr(expr.left)
    val current = visitExpr(expr.right)

    if (expected != current)
      throw InvalidDeclaredTypeException(current.toString(), expected.toString())

    return KoflBoolean
  }

  override fun visitGroupingExpr(expr: Expr.Grouping): KoflType {
    return visitExpr(expr.expr)
  }

  override fun visitLiteralExpr(expr: Expr.Literal): KoflType {
    return when (val value = expr.value) {
      is String -> KoflString
      is Double -> KoflDouble
      is Int -> KoflInt
      is Boolean -> KoflBoolean
      is KoflInstance -> value.type
      else -> throw InvalidTypeException(expr.value::class)
    }
  }

  override fun visitUnaryExpr(expr: Expr.Unary): KoflType {
    val right = visitExpr(expr.right)

    if (expr.op.type == TokenType.Bang) {
      if (right.isAssignableBy(KoflBoolean))
        throw InvalidDeclaredTypeException(right, KoflBoolean)
      else return KoflBoolean
    }

    return right
  }

  override fun visitVarExpr(expr: Expr.Var): KoflType {
    return types.peek().lookup(expr.name.lexeme)
  }

  override fun visitCallExpr(expr: Expr.Call): KoflType {
    val callee = when (val callee = expr.calle) {
      is Expr.Get -> visitExpr(callee.receiver)
      is Expr.Var -> types.peek()
        .lookupFuncOverload(callee.name.lexeme)
        .match(expr.arguments.values.map {
          visitExpr(it)
        }) ?: throw NameNotFoundException(callee.name.lexeme)
      else -> visitExpr(expr.calle)
    }

    if (callee !is KoflCallable)
      throw CompileTypeException("expected $callee to be a function")

    if (callee is KoflStruct)
      return callee

    return callee.returnType
  }

  override fun visitGetExpr(expr: Expr.Get): KoflType {
    val stmt = visitExpr(expr.receiver)
    if (stmt !is KoflStruct) throw UnresolvedVarException("$stmt.${expr.name.lexeme}")

    return stmt.fields[expr.name.lexeme] ?: throw UnresolvedVarException("$stmt.${expr.name.lexeme}")
  }

  override fun visitSetExpr(expr: Expr.Set): KoflType {
    TODO("Not yet implemented")
  }

  override fun visitThisExpr(expr: Expr.ThisExpr): KoflType {
    TODO("Not yet implemented")
  }

  override fun visitFuncExpr(expr: Expr.CommonFunc): KoflType {
    return Func(
      funcParameters(expr.parameters),
      funcType(expr.returnType, expr.body),
      expr, evaluator
    ).also { func ->
      funcBody(expr.body)

      types.peek().defineFunc(expr.name.lexeme, func)
    }
  }

  override fun visitExtensionFuncExpr(expr: Expr.ExtensionFunc): KoflType {
    return ExtensionFunc(
      funcParameters(expr.parameters),
      funcType(expr.returnType, expr.body),
      findType(expr.receiver.lexeme),
      expr, evaluator
    ).also { func ->
      funcBody(expr.body)

      types.peek().defineFunc(expr.name.lexeme, func)
    }
  }

  override fun visitAnonymousFuncExpr(expr: Expr.AnonymousFunc): KoflType {
    funcBody(expr.body)

    return AnonymousFunc(
      funcParameters(expr.parameters),
      funcType(expr.returnType, expr.body),
      expr, evaluator
    ).also {
      funcBody(expr.body)
    }
  }

  override fun visitNativeFuncExpr(expr: Expr.NativeFunc): KoflType {
    return NativeFunc(
      expr.name.lexeme, funcParameters(expr.parameters),
      returnType = findTypeOrNull(expr.returnType?.lexeme) ?: KoflUnit,
      nativeCall = { _, _ -> KoflUnit }
    ).also { func ->
      types.peek().defineFunc(expr.name.lexeme, func)
    }
  }

  override fun visitIfExpr(expr: Expr.IfExpr): KoflType {
    val condition = visitExpr(expr.condition)
    if (condition != KoflBoolean) throw InvalidDeclaredTypeException(condition.toString(), KoflBoolean.toString())

    val thenBranch = expr.thenBranch.also { thenStmts ->
      scoped {
        visitStmts(thenStmts)
      }
    }
    val thenLast = thenBranch.lastOrNull()
    val elseBranch = expr.elseBranch?.also { elseStmts ->
      scoped {
        visitStmts(elseStmts)
      }
    }
    val elseLast = elseBranch?.lastOrNull()

    if (thenBranch.isNotEmpty() && thenLast != null && elseBranch != null && elseBranch.isNotEmpty()) {
      val thenType = visitExpr((thenLast as? Stmt.ExprStmt)?.expr ?: return KoflUnit)
      val elseType = visitExpr((elseLast as? Stmt.ExprStmt)?.expr ?: return KoflUnit)

      if (thenType != elseType) {
        throw InvalidDeclaredTypeException(elseType.toString(), thenType.toString())
      }

      return thenType
    }

    return KoflUnit
  }

  override fun visitExprStmt(stmt: Stmt.ExprStmt): KoflType {
    visitExpr(stmt.expr)

    return KoflUnit
  }

  override fun visitBlockStmt(stmt: Stmt.Block): KoflType {
    scoped {
      visitStmts(stmt.body)
    }

    return KoflUnit
  }

  override fun visitWhileStmt(stmt: Stmt.WhileStmt): KoflType {
    val conditionType = visitExpr(stmt.condition)
    if (conditionType != KoflBoolean)
      throw InvalidDeclaredTypeException(conditionType, KoflBoolean)

    scoped {
      visitStmts(stmt.body)
    }

    return KoflUnit
  }

  override fun visitReturnStmt(stmt: Stmt.ReturnStmt): KoflType {
    return visitExpr(stmt.expr)
  }

  override fun visitValDeclStmt(stmt: Stmt.ValDecl): KoflType {
    return findVarType(stmt.name.lexeme, stmt.type?.lexeme, stmt.value)
  }

  override fun visitVarDeclStmt(stmt: Stmt.VarDecl): KoflType {
    return findVarType(stmt.name.lexeme, stmt.type?.lexeme, stmt.value)
  }

  private inline fun findVarType(name: String, typeName: String?, value: Expr): KoflType {
    val actualType = visitExpr(value).also {
      types.peek().define(name, it)
    }

    return if (typeName == null) actualType
    else findType(typeName).also { foundType ->
      if (!actualType.isAssignableBy(foundType))
        throw InvalidDeclaredTypeException(actualType, foundType)
    }
  }


  private fun funcParameters(parameters: Map<Token, Token>): Map<String, KoflType> {
    return parameters.mapKeys { (name) -> name.lexeme }.mapValues { (_, typeName) ->
      findType(typeName.lexeme)
    }
  }

  private fun funcBody(body: List<Stmt>): List<KoflType> {
    return scoped {
      visitStmts(body)
    }
  }

  private fun funcType(returnTypeStr: Token?, body: List<Stmt>): KoflType {
    val returnType = findTypeOrNull(returnTypeStr?.lexeme) ?: KoflUnit

    val returnStmt = body.filterIsInstance<Stmt.ReturnStmt>().firstOrNull()
    if (returnType != KoflUnit && returnStmt == null) throw MissingReturnException()

    val actualType = returnStmt?.let { visitStmt(it) } ?: KoflUnit

    if (actualType != returnType)
      throw InvalidDeclaredTypeException(actualType, returnType)

    return returnType
  }

  override fun visitStructTypedefStmt(stmt: Stmt.Type.Class): KoflType {
    val struct = KoflStruct(stmt.name.lexeme, stmt.fields.mapKeys { (name) -> name.lexeme }
      .mapValues { (_, value) ->
        findType(value.lexeme)
      })

    types.peek().defineType(stmt.name.lexeme, struct)

    return struct
  }

  private inline fun findTypeOrNull(typeName: String?): KoflType? {
    return types.peek().lookupTypeOrNull(typeName.toString())
  }

  private inline fun findType(typeName: String?): KoflType {
    return findTypeOrNull(typeName) ?: throw TypeNotFoundException(typeName.toString())
  }

  private inline fun <R> scoped(body: () -> R): R {
    beginScope()
    val value = body()
    endScope()

    return value
  }

  private inline fun beginScope() {
    types.push(TypeEnvironment(types.peek()))
  }

  private inline fun endScope() {
    types.pop()
  }
}