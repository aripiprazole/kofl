package com.lorenzoog.kofl.interpreter

class UnresolvedVarError(val name: String) : KoflRuntimeError("unresolved variable $name"){
  constructor(name: Token) : this(name.lexeme)
}
class UnresolvedFieldError(val name: String, val type: KoflObject) :
  KoflRuntimeError("unresolved field $name in $type")

class UninitializedVarError(val name: Token) : KoflRuntimeError("trying to access $name when it isn't initialized")
class AlreadyDeclaredVarError(val name: Token, resolver: Boolean = false) :
  KoflRuntimeError("trying to re-declare $name a variable that already exists: resolver = $resolver")

class UndefinedScopeAccessError(val name: String) :
  KoflRuntimeError("undefined scope when trying to use or define $name")

class Resolver(private val locals: MutableMap<Expr, Int>) {
  private val scopes = Stack<MutableMap<String, Boolean>>(190)

  fun resolve(stmts: List<Stmt>) {
    beginScope()
    stmts.forEach { resolve(it) }
    endScope()
  }

  private fun resolve(stmt: Stmt): Unit = when (stmt) {
    is Stmt.ExprStmt -> resolve(stmt)
    is Stmt.Block -> resolve(stmt)
    is Stmt.WhileStmt -> resolve(stmt)
    is Stmt.ReturnStmt -> resolve(stmt)
    is Stmt.ValDecl -> resolve(stmt)
    is Stmt.VarDecl -> resolve(stmt)
    is Stmt.TypeDef.Struct -> resolve(stmt)
  }

  private fun resolve(expr: Expr): Unit = when (expr) {
    is Expr.Assign -> resolve(expr)
    is Expr.Binary -> resolve(expr)
    is Expr.Logical -> resolve(expr)
    is Expr.Grouping -> resolve(expr)
    is Expr.Unary -> resolve(expr)
    is Expr.Var -> resolve(expr)
    is Expr.Get -> resolve(expr)
    is Expr.Set -> resolve(expr)
    is Expr.Call -> resolve(expr)
    is Expr.CommonFunc -> resolve(expr)
    is Expr.AnonymousFunc -> resolve(expr)
    is Expr.ExtensionFunc -> resolve(expr)
    is Expr.ThisExpr -> resolve(expr)
    is Expr.IfExpr -> resolve(expr)
    is Expr.NativeFunc -> Unit
    is Expr.Literal -> Unit
  }

  // STATEMENTS
  /**
   * we split declare and define to cover cases like
   * var x = "";
   * {
   *   var x = x + ";";
   * }
   */
  private fun resolve(stmt: Stmt.VarDecl) {
    declare(stmt.name)
    resolve(stmt.value)
    define(stmt.name)
  }

  /**
   * we split declare and define to cover cases like
   * val x = "";
   * {
   *   val x = x + ";";
   * }
   */
  private fun resolve(stmt: Stmt.ValDecl) {
    declare(stmt.name)
    resolve(stmt.value)
    define(stmt.name)
  }

  private fun resolve(stmt: Stmt.TypeDef.Struct) {
    declare(stmt.name)
    define(stmt.name)
  }

  private fun resolve(stmt: Stmt.Block) {
    beginScope()
    resolve(stmt.decls)
    endScope()
  }

  private fun resolve(stmt: Stmt.ReturnStmt) {
    resolve(stmt.expr)
  }

  private fun resolve(stmt: Stmt.WhileStmt) {
    resolve(stmt.condition)
    resolve(stmt.body)
  }

  private fun resolve(stmt: Stmt.ExprStmt) {
    resolve(stmt.expr)
  }

  // EXPRESSIONS
  private fun resolve(expr: Expr.IfExpr) {
    resolve(expr.condition)
    resolve(expr.thenBranch)
    expr.elseBranch?.also { this.resolve(it) }
  }

  private fun resolve(expr: Expr.Get) {
    resolve(expr.receiver)
  }

  private fun resolve(expr: Expr.Set) {
    resolve(expr.receiver)
    resolve(expr.value)
  }

  private fun resolve(expr: Expr.Var) {
    if (!scopes.isEmpty && scopes.peek()?.get(expr.name.lexeme) == false)
      throw UninitializedVarError(expr.name)

    resolveLocal(expr, expr.name)
  }

  private fun resolve(expr: Expr.Assign) {
    resolve(expr.value)
    resolveLocal(expr, expr.name)
  }

  private fun resolve(expr: Expr.ThisExpr) {
    resolveLocal(expr, expr.keyword)
  }

  private fun resolve(expr: Expr.Grouping) {
    resolve(expr.expr)
  }

  private fun resolve(expr: Expr.Binary) {
    resolve(expr.left)
    resolve(expr.right)
  }

  private fun resolve(expr: Expr.Logical) {
    resolve(expr.left)
    resolve(expr.right)
  }

  private fun resolve(expr: Expr.Unary) {
    resolve(expr.right)
  }

  private fun resolve(expr: Expr.Call) {
    resolve(expr.calle)
    expr.arguments.forEach { (_, v) ->
      resolve(v)
    }
  }

  private fun resolve(expr: Expr.AnonymousFunc) {
    beginScope()
    expr.arguments.forEach { (_, v) ->
      declare(v)
      define(v)
    }
    resolve(expr.body)
    endScope()
  }

  private fun resolve(expr: Expr.ExtensionFunc) {
    declare(expr.name)
    define(expr.name)

    beginScope()

    val scope = scopes.peek() ?: throw UndefinedScopeAccessError(expr.name.lexeme)
    scope["this"] = true

    expr.arguments.forEach { (_, v) ->
      declare(v)
      define(v)
    }

    resolve(expr.body)

    endScope()
  }

  private fun resolve(expr: Expr.CommonFunc) {
    declare(expr.name)
    define(expr.name)

    beginScope()
    expr.arguments.forEach { (_, v) ->
      declare(v)
      define(v)
    }
    resolve(expr.body)
    endScope()
  }

  private fun declare(name: Token) {
    println(scopes)
    if (scopes.isEmpty) return

    val scope = scopes.peek() ?: throw UndefinedScopeAccessError(name.lexeme)
    if (scope[name.lexeme] != null) throw AlreadyDeclaredVarError(name, resolver = true)

    scope[name.lexeme] = false
  }

  private fun define(name: Token) {
    if (scopes.isEmpty) return

    val scope = scopes.peek() ?: throw UndefinedScopeAccessError(name.lexeme)
    if (scope[name.lexeme] == null) throw UnresolvedVarError(name)

    scope[name.lexeme] = true
  }

  /**
   * Will pass to evaluator the current scope index,
   * if it is the current, will be 0, if enclosing, 1,
   * and so on
   */
  private fun resolveLocal(expr: Expr, name: Token) {
    for (index in (scopes.size - 1) downTo 0) {
      if (scopes[index]?.containsKey(name.lexeme) == true) {
        locals[expr] = index - 1 - scopes.size
      }
    }
  }

  private fun beginScope() {
    scopes.push(mutableMapOf())
  }

  private fun endScope() {
    scopes.pop()
  }
}