package com.lorenzoog.kofl.interpreter.typing

import com.lorenzoog.kofl.frontend.*
import com.lorenzoog.kofl.interpreter.MAX_STACK
import com.lorenzoog.kofl.interpreter.backend.Descriptor
import com.lorenzoog.kofl.interpreter.exceptions.KoflCompileTimeException

private val BINARY_TOKENS = listOf(
  TokenType.Plus, TokenType.Slash, TokenType.Minus, TokenType.Star,
  TokenType.Greater, TokenType.GreaterEqual, TokenType.Less, TokenType.LessEqual
)

class TypeValidator(
  private val locals: MutableMap<Descriptor, Int>,
  private val container: Stack<TypeContainer>
) : Expr.Visitor<KoflType>, Stmt.Visitor<KoflType> {
  private val scopes = Stack<MutableMap<String, Boolean>>(MAX_STACK)

  override fun visitStmts(stmts: Collection<Stmt>): List<KoflType> {
    scopes.push(mutableMapOf())
    val visitedStmts = super.visitStmts(stmts)
    scopes.pop()

    return visitedStmts
  }

  override fun visitAssignExpr(expr: Expr.Assign): KoflType {
    val expected = container.peek().lookup(expr.name.lexeme)
    val current = visitExpr(expr.value)

    if (expected != current)
      throw KoflCompileTimeException.UnexpectedType(current, expected)

    return expected
  }

  override fun visitBinaryExpr(expr: Expr.Binary): KoflType {
    val left = visitExpr(expr.left)
    val right = visitExpr(expr.right)

    if (expr.op.type in BINARY_TOKENS) {
      if (left.isNumber() && right.isNumber())
        return KoflType.Primitive.Boolean

      throw  KoflCompileTimeException.UnexpectedType(right, KoflType.Primitive.Int)
    }

    if (left.isAssignableBy(KoflType.Primitive.Boolean) && right.isAssignableBy(KoflType.Primitive.Boolean))
      return KoflType.Primitive.Boolean

    throw  KoflCompileTimeException.UnexpectedType(left, KoflType.Primitive.Boolean)
  }

  override fun visitLogicalExpr(expr: Expr.Logical): KoflType {
    val expected = visitExpr(expr.left)
    val current = visitExpr(expr.right)

    if (expected != current)
      throw  KoflCompileTimeException.UnexpectedType(current, expected)

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
      else -> throw  KoflCompileTimeException.InvalidType(expr.value::class)
    }
  }

  override fun visitUnaryExpr(expr: Expr.Unary): KoflType {
    val right = visitExpr(expr.right)

    if (expr.op.type == TokenType.Bang) {
      if (right.isAssignableBy(KoflType.Primitive.Boolean))
        throw  KoflCompileTimeException.UnexpectedType(right, KoflType.Primitive.Boolean)

      return KoflType.Primitive.Boolean
    }

    return right
  }

  override fun visitVarExpr(expr: Expr.Var): KoflType {
    return container.peek().lookup(expr.name.lexeme)
  }

  override fun visitCallExpr(expr: Expr.Call): KoflType {
    val callee = findCallCallee(expr.calle, expr.arguments)

    return callee.returnType
  }

  fun findCallOverload(expr: Expr): Collection<KoflType.Callable> {
    return when (expr) {
      is Expr.Get -> visitExpr(expr.receiver).functions[expr.name.lexeme].orEmpty()
      is Expr.Var -> container.peek().lookupFunctionOverload(expr.name.lexeme)
      else -> emptyList()
    }
  }

  fun findCallCallee(expr: Expr, arguments: Map<Token?, Expr>): KoflType.Callable {
    val callee = when (expr) {
      is Expr.Get -> visitExpr(expr.receiver).functions[expr.name.lexeme].orEmpty()
        .match(arguments.values.map {
          visitExpr(it)
        }) ?: throw  KoflCompileTimeException.UnresolvedVar(expr.name.lexeme)
      is Expr.Var -> container.peek()
        .lookupFunctionOverload(expr.name.lexeme)
        .match(arguments.values.map {
          visitExpr(it)
        }) ?: throw  KoflCompileTimeException.UnresolvedVar(expr.name.lexeme)
      else -> visitExpr(expr)
    }

    if (callee !is KoflType.Callable)
      throw  KoflCompileTimeException.UnexpectedType(callee, KoflType.Callable::class)

    return callee
  }

  override fun visitGetExpr(expr: Expr.Get): KoflType {
    val receiver = visitExpr(expr.receiver)
    val name = expr.name.lexeme

    return receiver[name] ?: throw KoflCompileTimeException.UnresolvedVar("$receiver.$name")
  }

  override fun visitSetExpr(expr: Expr.Set): KoflType {
    val receiver = visitExpr(expr.receiver)
    val value = visitExpr(expr.value)
    val name = expr.name.lexeme
    val type = receiver[name] ?: throw KoflCompileTimeException.UnresolvedVar("$receiver.$name")

    if (!type.isAssignableBy(value)) throw KoflCompileTimeException.UnexpectedType(value, type)

    return type
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
      container.peek().defineFunction(name, function)
    }
  }

  override fun visitExtensionFuncExpr(expr: Expr.ExtensionFunc): KoflType {
    val name = expr.name.lexeme
    val receiver = findType(expr.receiver.lexeme)
    val parameters = typedParameters(expr.parameters)
    val returnType = typedReturn(expr.returnType, expr.body)

    scoped { visitStmts(expr.body) }

    return KoflType.Function(parameters, returnType, receiver).also { function ->
      container.peek().defineFunction(name, function)
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
      throw KoflCompileTimeException.UnexpectedType(condition, KoflType.Primitive.Boolean)

    val thenBranch = expr.thenBranch.also { thenStmts -> scoped { visitStmts(thenStmts) } }
    val thenLast = thenBranch.lastOrNull()

    val elseBranch = expr.elseBranch?.also { elseStmts -> scoped { visitStmts(elseStmts) } }
    val elseLast = elseBranch?.lastOrNull()

    if (thenBranch.isNotEmpty() && thenLast != null && elseBranch != null && elseBranch.isNotEmpty()) {
      val then = visitExpr((thenLast as? Stmt.ExprStmt)?.expr ?: return KoflType.Primitive.Unit)
      val orElse = visitExpr((elseLast as? Stmt.ExprStmt)?.expr ?: return KoflType.Primitive.Unit)

      if (then.isAssignableBy(orElse))
        throw KoflCompileTimeException.UnexpectedType(orElse, then)

      return then
    }

    return KoflType.Primitive.Unit
  }

  override fun visitExprStmt(stmt: Stmt.ExprStmt): KoflType {
    visitExpr(stmt.expr)

    return KoflType.Primitive.Unit
  }

  override fun visitBlockStmt(stmt: Stmt.Block): KoflType {
    scoped { visitStmts(stmt.body) }

    return KoflType.Primitive.Unit
  }

  override fun visitWhileStmt(stmt: Stmt.WhileStmt): KoflType {
    val condition = visitExpr(stmt.condition)
    if (KoflType.Primitive.Boolean.isAssignableBy(condition))
      throw KoflCompileTimeException.UnexpectedType(condition, KoflType.Primitive.Boolean)

    scoped { visitStmts(stmt.body) }

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

  override fun visitClassTypeStmt(stmt: Stmt.Type.Class): KoflType {
    val name = stmt.name.lexeme
    val parameters = typedParameters(stmt.parameters)
    val functions = mutableMapOf<String, List<KoflType.Function>>()
    val klass = KoflType.Class(name, parameters, functions)

    container.peek().defineType(name, klass)

    return klass
  }

  private fun declare(name: String) {
    if (container.isEmpty) return

    val scope = scopes.peek()
    if (scope[name] == null) throw KoflCompileTimeException.UnresolvedVar(name)

    scope[name] = false
  }

  private fun define(name: String) {
    if (container.isEmpty) return

    val scope = scopes.peek()
    if (scope[name] != null) throw KoflCompileTimeException.AlreadyResolvedVar(name)

    scope[name] = true
  }

  /**
   * Will pass to evaluator the current scope index,
   * if it is the current, will be 0, if enclosing, 1,
   * and so on
   */
  private fun resolveLocal(descriptor: Descriptor, name: String) {
    for (index in (container.size - 1) downTo 0) {
      if (container[index]?.containsName(name) == true) {
        locals[descriptor] = index - 1 - container.size
      }
    }
  }

  private fun typedReturn(name: Token?, body: List<Stmt>): KoflType {
    val type = typedReturn(name)

    val stmt = body.filterIsInstance<Stmt.ReturnStmt>().firstOrNull()
    if (type != KoflType.Primitive.Unit && stmt == null)
      throw KoflCompileTimeException.MissingReturn()

    val actualType = stmt?.let { visitStmt(it) } ?: KoflType.Primitive.Unit

    if (actualType != type)
      throw KoflCompileTimeException.UnexpectedType(actualType, type)

    return type
  }

  private fun typedVarDeclaration(name: String, typeName: String?, value: Expr): KoflType {
    val actual = visitExpr(value).also { container.peek().define(name, it) }

    if (typeName == null) return actual

    val found = findType(typeName)
    if (!actual.isAssignableBy(found))
      throw KoflCompileTimeException.UnexpectedType(actual, found)

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
    return container.peek().lookupType(name) ?: throw KoflCompileTimeException.UnresolvedVar(name)
  }

  private inline fun <R> scoped(body: () -> R): R {
    container.push(TypeContainer(container.peek()))
    val value = body()
    container.pop()

    return value
  }
}