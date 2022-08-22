package me.devgabi.kofl.compiler.common.typing.analyzer

import me.devgabi.kofl.compiler.common.KoflCompileException
import me.devgabi.kofl.compiler.common.backend.Descriptor
import me.devgabi.kofl.compiler.common.typing.KfType
import me.devgabi.kofl.compiler.common.typing.TypeScope
import me.devgabi.kofl.compiler.common.typing.createClassDefinition
import me.devgabi.kofl.compiler.common.typing.isAssignableBy
import me.devgabi.kofl.compiler.common.typing.isNumber
import me.devgabi.kofl.compiler.common.typing.match
import me.devgabi.kofl.frontend.Expr
import me.devgabi.kofl.frontend.Stack
import me.devgabi.kofl.frontend.Stmt
import me.devgabi.kofl.frontend.Token
import me.devgabi.kofl.frontend.TokenType
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

const val MAX_STACK = 512_000

private val BINARY_TOKENS = listOf(
  TokenType.Plus,
  TokenType.Slash,
  TokenType.Minus,
  TokenType.Star,
  TokenType.Greater,
  TokenType.GreaterEqual,
  TokenType.Less,
  TokenType.LessEqual
)

@ExperimentalContracts
class DefaultTreeAnalyzer(
  private val locals: MutableMap<Descriptor, Int>,
  private val container: Stack<TypeScope>,
  private val scopes: Stack<MutableMap<String, Boolean>> = Stack(MAX_STACK),
) : TreeAnalyzer, Expr.Visitor<KfType>, Stmt.Visitor<Unit> {
  override fun analyze(expr: Expr): KfType {
    return visitExpr(expr)
  }

  override fun validate(stmt: Stmt) {
    return visitStmt(stmt)
  }

  override fun validate(stmts: List<Stmt>) {
    scopes.push(mutableMapOf())
    visitStmts(stmts)
    scopes.pop()
  }

  override fun visitAssignExpr(expr: Expr.Assign): KfType {
    val expected = container.peek().lookup(expr.name.lexeme)
    val current = analyze(expr.value)

    if (expected != current)
      throw KoflCompileException.UnexpectedType(current, expected)

    return expected
  }

  override fun visitBinaryExpr(expr: Expr.Binary): KfType {
    val left = analyze(expr.left)
    val right = analyze(expr.right)

    if (expr.op.type in BINARY_TOKENS) {
      if (left.isNumber() && right.isNumber())
        return KfType.Double

      if (KfType.String.isAssignableBy(left))
        return KfType.String

      throw KoflCompileException.UnexpectedType(right, KfType.Int)
    }

    if (left.isAssignableBy(KfType.Boolean) && right.isAssignableBy(KfType.Boolean))
      return KfType.Boolean

    throw KoflCompileException.UnexpectedType(left, KfType.Boolean)
  }

  override fun visitLogicalExpr(expr: Expr.Logical): KfType {
    val expected = analyze(expr.left)
    val current = analyze(expr.right)

    if (expected != current)
      throw KoflCompileException.UnexpectedType(current, expected)

    return KfType.Boolean
  }

  override fun visitGroupingExpr(expr: Expr.Grouping): KfType {
    return analyze(expr.expr)
  }

  override fun visitLiteralExpr(expr: Expr.Literal): KfType {
    return when (expr.value) {
      is String -> KfType.String
      is Double -> KfType.Double
      is Int -> KfType.Int
      is Boolean -> KfType.Boolean
//      is KoflInstance -> value.type
      else -> throw KoflCompileException.InvalidType(expr.value::class)
    }
  }

  override fun visitUnaryExpr(expr: Expr.Unary): KfType {
    val right = analyze(expr.right)

    if (expr.op.type == TokenType.Bang) {
      if (!right.isAssignableBy(KfType.Boolean))
        throw KoflCompileException.UnexpectedType(right, KfType.Boolean)

      return KfType.Boolean
    }

    return right
  }

  override fun visitVarExpr(expr: Expr.Var): KfType {
    return container.peek().lookup(expr.name.lexeme)
  }

  override fun visitCallExpr(expr: Expr.Call): KfType {
    return findCallable(expr.calle, expr.arguments).returnType
  }

  override fun visitGetExpr(expr: Expr.Get): KfType {
    val receiver = analyze(expr.receiver)
    val name = expr.name.lexeme

    return receiver[name] ?: throw KoflCompileException.UnresolvedVar("$receiver.$name")
  }

  override fun visitSetExpr(expr: Expr.Set): KfType {
    val receiver = analyze(expr.receiver)
    val value = analyze(expr.value)
    val name = expr.name.lexeme
    val type = receiver[name] ?: throw KoflCompileException.UnresolvedVar("$receiver.$name")

    if (!type.isAssignableBy(value)) throw KoflCompileException.UnexpectedType(value, type)

    return type
  }

  override fun visitThisExpr(expr: Expr.ThisExpr): KfType {
    TODO("Not yet implemented")
  }

  override fun visitFuncExpr(expr: Expr.CommonFunc): KfType {
    val name = expr.name.lexeme
    val parameters = findParametersTypes(expr.parameters)
    val returnType: KfType

    scoped { current ->
      parameters.forEach { (name, type) ->
        current.define(name, type)
      }

      visitStmts(expr.body)

      returnType = findReturnTypeByToken(expr.returnType, expr.body)
    }

    return KfType.Function(parameters, returnType).also { function ->
      container.peek().defineFunction(name, function)
    }
  }

  override fun visitExtensionFuncExpr(expr: Expr.ExtensionFunc): KfType {
    val name = expr.name.lexeme
    val receiver = findTypeByName(expr.receiver.lexeme)
    val parameters = findParametersTypes(expr.parameters)
    val returnType: KfType

    scoped { current ->
      parameters.forEach { (name, type) ->
        current.define(name, type)
      }

      validate(expr.body)

      returnType = findReturnTypeByToken(expr.returnType, expr.body)
    }

    return KfType.Function(parameters, returnType, receiver).also { function ->
      container.peek().defineFunction(name, function)
    }
  }

  override fun visitAnonymousFuncExpr(expr: Expr.AnonymousFunc): KfType {
    val parameters = findParametersTypes(expr.parameters)
    val returnType: KfType

    scoped { current ->
      parameters.forEach { (name, type) ->
        current.define(name, type)
      }

      validate(expr.body)

      returnType = findReturnTypeByToken(expr.returnType, expr.body)
    }

    return KfType.Function(parameters, returnType)
  }

  override fun visitNativeFuncExpr(expr: Expr.NativeFunc): KfType {
    val parameters = findParametersTypes(expr.parameters)
    val returnType = findReturnTypeByToken(expr.returnType)

    return KfType.Function(parameters, returnType)
  }

  override fun visitIfExpr(expr: Expr.IfExpr): KfType {
    val condition = analyze(expr.condition)

    if (!KfType.Boolean.isAssignableBy(condition))
      throw KoflCompileException.UnexpectedType(condition, KfType.Boolean)

    val thenBranch = expr.thenBranch.also { thenStmts -> scoped { visitStmts(thenStmts) } }
    val thenLast = thenBranch.lastOrNull()

    val elseBranch = expr.elseBranch?.also { elseStmts -> scoped { visitStmts(elseStmts) } }
    val elseLast = elseBranch?.lastOrNull()

    if (
      thenBranch.isNotEmpty() &&
      thenLast != null &&
      elseBranch != null && elseBranch.isNotEmpty()
    ) {
      val then = analyze((thenLast as? Stmt.ExprStmt)?.expr ?: return KfType.Unit)
      val orElse = analyze((elseLast as? Stmt.ExprStmt)?.expr ?: return KfType.Unit)

      if (!then.isAssignableBy(orElse))
        throw KoflCompileException.UnexpectedType(orElse, then)

      return then
    }

    return KfType.Unit
  }

  override fun visitExprStmt(stmt: Stmt.ExprStmt) {
    analyze(stmt.expr)
  }

  override fun visitBlockStmt(stmt: Stmt.Block) {
    scoped { visitStmts(stmt.body) }
  }

  override fun visitWhileStmt(stmt: Stmt.WhileStmt) {
    val condition = analyze(stmt.condition)

    if (KfType.Boolean.isAssignableBy(condition))
      throw KoflCompileException.UnexpectedType(condition, KfType.Boolean)

    scoped { visitStmts(stmt.body) }
  }

  override fun visitReturnStmt(stmt: Stmt.ReturnStmt) {
    analyze(stmt.expr)
  }

  override fun visitValDeclStmt(stmt: Stmt.ValDecl) {
    findVariableType(stmt.name.lexeme, stmt.type?.lexeme, stmt.value)
  }

  override fun visitVarDeclStmt(stmt: Stmt.VarDecl) {
    findVariableType(stmt.name.lexeme, stmt.type?.lexeme, stmt.value)
  }

  override fun visitTypeRecordStmt(stmt: Stmt.Type.Record) {
    val name = stmt.name.lexeme
    val parameters = findParametersTypes(stmt.parameters)
    val functions = mutableMapOf<String, List<KfType.Function>>()
    val klass = createClassDefinition(name) {
      constructor(parameters)

      functions.forEach { (name, function) ->
        function(name, function)
      }
    }

    container.peek().defineType(name, klass)
  }

  override fun visitUseStmt(stmt: Stmt.UseDecl) {
    // TODO check if already imported in this scope
  }

  override fun visitModuleStmt(stmt: Stmt.ModuleDecl) {
    // TODO validate if is the unique in the file
  }

  override fun findOverload(name: Expr): List<KfType.Callable> {
    return when (name) {
      is Expr.Get -> analyze(name.receiver).functions[name.name.lexeme].orEmpty()
      is Expr.Var -> container.peek().lookupFunctionOverload(name.name.lexeme)
      else -> emptyList()
    }
  }

  override fun findCallable(name: Expr, arguments: Map<Token?, Expr>): KfType.Callable {
    val callee = when (name) {
      is Expr.Get ->
        analyze(name.receiver).functions[name.name.lexeme].orEmpty()
          .match(arguments.values.map { analyze(it) })
          ?: throw KoflCompileException.UnresolvedVar(name.name.lexeme)

      is Expr.Var ->
        container.peek()
          .lookupFunctionOverload(name.name.lexeme)
          .match(arguments.values.map { analyze(it) })
          ?: container.peek().lookup(name.name.lexeme)

      else -> analyze(name)
    }

    if (callee !is KfType.Callable)
      throw KoflCompileException.UnexpectedType(callee, KfType.Callable::class)

    return callee
  }

  private fun findVariableType(name: String, typeName: String?, value: Expr): KfType {
    val expected = analyze(value).also { container.peek().define(name, it) }

    if (typeName == null) return expected

    val actual = findTypeByName(typeName)

    if (!actual.isAssignableBy(expected))
      throw KoflCompileException.UnexpectedType(expected, actual)

    return actual
  }

  private fun declare(name: String) {
    if (container.isEmpty) return

    val scope = scopes.peek()
    if (scope[name] == null) throw KoflCompileException.UnresolvedVar(name)

    scope[name] = false
  }

  private fun define(name: String) {
    if (container.isEmpty) return

    val scope = scopes.peek()
    if (scope[name] != null) throw KoflCompileException.AlreadyResolvedVar(name)

    scope[name] = true
  }

  private fun findTypeByName(name: String): KfType {
    return container.peek().lookupType(name) ?: throw KoflCompileException.UnresolvedVar(name)
  }

  private fun findReturnTypeByToken(name: Token?, body: List<Stmt>): KfType {
    val type = findReturnTypeByToken(name)

    val stmt = body.filterIsInstance<Stmt.ReturnStmt>().firstOrNull()
    if (type != KfType.Unit && stmt == null)
      throw KoflCompileException.MissingReturn()

    val actual = stmt?.let { (expr) -> analyze(expr) } ?: KfType.Unit

    if (actual != type)
      throw KoflCompileException.UnexpectedType(actual, type)

    return type
  }

  private fun findParametersTypes(parameters: Map<Token, Token>): Map<String, KfType> {
    return parameters.mapKeys { (name) -> name.lexeme }.mapValues { (_, typeName) ->
      findTypeByName(typeName.lexeme)
    }
  }

  private fun findReturnTypeByToken(name: Token?): KfType {
    return findTypeByNameOrElse(name.toString())
  }

  private fun findTypeByNameOrElse(name: String): KfType {
    return container.peek().lookupType(name) ?: KfType.Unit
  }

  private inline fun <R> scoped(body: (TypeScope) -> R): R {
    contract {
      callsInPlace(body, InvocationKind.EXACTLY_ONCE)
    }

    container.push(TypeScope(container.peek()))
    val value = body(container.peek())
    container.pop()

    return value
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
}
