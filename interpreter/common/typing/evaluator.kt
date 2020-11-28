package com.lorenzoog.kofl.interpreter.typing

import com.lorenzoog.kofl.frontend.*

open class TypeException(message: String) : KoflException("static type", message)

class NameNotFoundException(name: String) : TypeException("name $name not found!")

class TypeNotFoundException(name: String) : TypeException("type $name not found!")

class InvalidTypeException(value: Any) : TypeException("invalid kofl type in $value")

class MissingReturnException : TypeException("missing return function body")

class InvalidDeclaredTypeException(current: Any, expected: Any) :
  TypeException("excepted $expected but got $current")

private val BINARY_TOKENS = listOf(
  TokenType.Plus, TokenType.Slash, TokenType.Minus, TokenType.Star,
  TokenType.Greater, TokenType.GreaterEqual, TokenType.Less, TokenType.LessEqual
)

class TypeValidator(private val container: Stack<TypeContainer>) : Expr.Visitor<KoflType>, Stmt.Visitor<KoflType> {
  override fun visitAssignExpr(expr: Expr.Assign): KoflType {
    val expected = container.peek().lookup(expr.name.lexeme)
    val current = visitExpr(expr.value)

    if (expected != current)
      throw InvalidDeclaredTypeException(current, expected)

    return expected
  }

  override fun visitBinaryExpr(expr: Expr.Binary): KoflType {
    val left = visitExpr(expr.left)
    val right = visitExpr(expr.right)

    if (expr.op.type in BINARY_TOKENS) {
      if (left.isNumber() && right.isNumber())
        return KoflType.Primitive.Boolean

      throw InvalidDeclaredTypeException(right, KoflType.Primitive.Int)
    }

    if (left.isAssignableBy(KoflType.Primitive.Boolean) && right.isAssignableBy(KoflType.Primitive.Boolean))
      return KoflType.Primitive.Boolean

    throw InvalidDeclaredTypeException(left, KoflType.Primitive.Boolean)
  }

  override fun visitLogicalExpr(expr: Expr.Logical): KoflType {
    val expected = visitExpr(expr.left)
    val current = visitExpr(expr.right)

    if (expected != current)
      throw InvalidDeclaredTypeException(current, expected)

    return KoflType.Primitive.Boolean
  }

  override fun visitGroupingExpr(expr: Expr.Grouping): KoflType {
    return visitExpr(expr.expr)
  }

  override fun visitLiteralExpr(expr: Expr.Literal): KoflType {
    return when (expr.value) {
      is String -> KoflType.Primitive.String
      is Double -> KoflType.Primitive.Double
      is Int -> KoflType.Primitive.Int
      is Boolean -> KoflType.Primitive.Boolean
//      is KoflInstance -> value.type
      else -> throw InvalidTypeException(expr.value::class)
    }
  }

  override fun visitUnaryExpr(expr: Expr.Unary): KoflType {
    val right = visitExpr(expr.right)

    if (expr.op.type == TokenType.Bang) {
      if (right.isAssignableBy(KoflType.Primitive.Boolean))
        throw InvalidDeclaredTypeException(right, KoflType.Primitive.Boolean)

      return KoflType.Primitive.Boolean
    }

    return right
  }

  override fun visitVarExpr(expr: Expr.Var): KoflType {
    return container.peek().lookup(expr.name.lexeme)
  }

  override fun visitCallExpr(expr: Expr.Call): KoflType {
    val callee = when (val callee = expr.calle) {
      is Expr.Get -> visitExpr(callee.receiver)
      is Expr.Var -> container.peek()
        .lookupFuncOverload(callee.name.lexeme)
        .match(expr.arguments.values.map {
          visitExpr(it)
        }) ?: throw NameNotFoundException(callee.name.lexeme)
      else -> visitExpr(expr.calle)
    }

    if (callee !is KoflType.Function)
      throw TypeException("expected $callee to be a function")

//    if (callee is KoflStruct)
//      return callee

    return callee.returnType
  }

  override fun visitGetExpr(expr: Expr.Get): KoflType {
    TODO()
  }

  override fun visitSetExpr(expr: Expr.Set): KoflType {
    TODO("Not yet implemented")
  }

  override fun visitThisExpr(expr: Expr.ThisExpr): KoflType {
    TODO("Not yet implemented")
  }

  override fun visitFuncExpr(expr: Expr.CommonFunc): KoflType {
    val name = expr.name.lexeme
    val parameters = typedParameters(expr.parameters)
    val returnType = typedReturn(expr.returnType, expr.body)

    scoped { visitStmts(expr.body) }

    return KoflType.Function(parameters, returnType).also { function ->
      container.peek().defineFunc(name, function)
    }
  }

  override fun visitExtensionFuncExpr(expr: Expr.ExtensionFunc): KoflType {
    val name = expr.name.lexeme
    val receiver = findType(expr.receiver.lexeme)
    val parameters = typedParameters(expr.parameters)
    val returnType = typedReturn(expr.returnType, expr.body)

    scoped { visitStmts(expr.body) }

    return KoflType.Function(parameters, returnType, receiver).also { function ->
      container.peek().defineFunc(name, function)
    }
  }

  override fun visitAnonymousFuncExpr(expr: Expr.AnonymousFunc): KoflType {
    val parameters = typedParameters(expr.parameters)
    val returnType = typedReturn(expr.returnType, expr.body)

    scoped { visitStmts(expr.body) }

    return KoflType.Function(parameters, returnType)
  }

  override fun visitNativeFuncExpr(expr: Expr.NativeFunc): KoflType {
    val parameters = typedParameters(expr.parameters)
    val returnType = typedReturn(expr.returnType)

    return KoflType.Function(parameters, returnType)
  }

  override fun visitIfExpr(expr: Expr.IfExpr): KoflType {
    val condition = visitExpr(expr.condition)
    if (KoflType.Primitive.Boolean.isAssignableBy(condition))
      throw InvalidDeclaredTypeException(condition, KoflType.Primitive.Boolean)

    val thenBranch = expr.thenBranch.also { thenStmts -> scoped { visitStmts(thenStmts) } }
    val thenLast = thenBranch.lastOrNull()

    val elseBranch = expr.elseBranch?.also { elseStmts -> scoped { visitStmts(elseStmts) } }
    val elseLast = elseBranch?.lastOrNull()

    if (thenBranch.isNotEmpty() && thenLast != null && elseBranch != null && elseBranch.isNotEmpty()) {
      val then = visitExpr((thenLast as? Stmt.ExprStmt)?.expr ?: return KoflType.Primitive.Unit)
      val orElse = visitExpr((elseLast as? Stmt.ExprStmt)?.expr ?: return KoflType.Primitive.Unit)

      if (then.isAssignableBy(orElse)) throw InvalidDeclaredTypeException(orElse, then)

      return then
    }

    return KoflType.Primitive.Unit
  }

  override fun visitExprStmt(stmt: Stmt.ExprStmt): KoflType {
    visitExpr(stmt.expr)

    return KoflType.Primitive.Unit
  }

  override fun visitBlockStmt(stmt: Stmt.Block): KoflType {
    scoped {
      visitStmts(stmt.body)
    }

    return KoflType.Primitive.Unit
  }

  override fun visitWhileStmt(stmt: Stmt.WhileStmt): KoflType {
    val condition = visitExpr(stmt.condition)
    if (KoflType.Primitive.Boolean.isAssignableBy(condition))
      throw InvalidDeclaredTypeException(condition, KoflType.Primitive.Boolean)

    scoped {
      visitStmts(stmt.body)
    }

    return KoflType.Primitive.Unit
  }

  override fun visitReturnStmt(stmt: Stmt.ReturnStmt): KoflType {
    return visitExpr(stmt.expr)
  }

  override fun visitValDeclStmt(stmt: Stmt.ValDecl): KoflType {
    return typedVarDeclaration(stmt.name.lexeme, stmt.type?.lexeme, stmt.value)
  }

  override fun visitVarDeclStmt(stmt: Stmt.VarDecl): KoflType {
    return typedVarDeclaration(stmt.name.lexeme, stmt.type?.lexeme, stmt.value)
  }

  override fun visitStructTypedefStmt(stmt: Stmt.Type.Class): KoflType {
    TODO()
  }

  private fun typedReturn(name: Token?, body: List<Stmt>): KoflType {
    val type = typedReturn(name)

    val stmt = body.filterIsInstance<Stmt.ReturnStmt>().firstOrNull()
    if (type != KoflType.Primitive.Unit && stmt == null)
      throw MissingReturnException()

    val actualType = stmt?.let { visitStmt(it) } ?: KoflType.Primitive.Unit

    if (actualType != type)
      throw InvalidDeclaredTypeException(actualType, type)

    return type
  }

  private fun typedVarDeclaration(name: String, typeName: String?, value: Expr): KoflType {
    val actual = visitExpr(value).also { container.peek().define(name, it) }

    if (typeName == null) return actual

    val found = findType(typeName)
    if (!actual.isAssignableBy(found))
      throw InvalidDeclaredTypeException(actual, found)

    return found
  }

  private fun typedParameters(parameters: Map<Token, Token>): Map<String, KoflType> {
    return parameters.mapKeys { (name) -> name.lexeme }.mapValues { (_, typeName) ->
      findType(typeName.lexeme)
    }
  }

  private inline fun typedReturn(name: Token?): KoflType {
    return findTypeOrDefault(name.toString())
  }

  private inline fun findTypeOrDefault(name: String): KoflType {
    return container.peek().lookupType(name) ?: KoflType.Primitive.Unit
  }

  private inline fun findType(name: String): KoflType {
    return container.peek().lookupType(name) ?: throw TypeNotFoundException(name)
  }

  private inline fun <R> scoped(body: () -> R): R {
    container.push(TypeContainer(container.peek()))
    val value = body()
    container.pop()

    return value
  }
}