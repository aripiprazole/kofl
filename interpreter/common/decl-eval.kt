package com.lorenzoog.kofl.interpreter

/**
 * This class will push all global declarations lazy
 * to globalEnvironment, that was made to make
 * typechecking easier
 */
class DeclEvaluator(
  override val globalEnvironment: MutableEnvironment,
  private val locals: MutableMap<Expr, Int>,
  private val evaluator: CodeEvaluator
) : Evaluator<Unit> {
  override fun eval(stmt: Stmt, environment: MutableEnvironment): Unit = when (stmt) {
    is Stmt.VarDecl -> eval(stmt, environment)
    is Stmt.ValDecl -> eval(stmt, environment)
    is Stmt.TypeDef.Struct -> eval(stmt, environment)
    else -> Unit
  }

  private fun eval(stmt: Stmt.ValDecl, environment: MutableEnvironment) {
    environment.define(stmt.name, KoflValue.Lazy.Immutable {
      evaluator.eval(stmt.value, environment)
    })
  }

  private fun eval(stmt: Stmt.VarDecl, environment: MutableEnvironment) {
    environment.define(stmt.name, KoflValue.Lazy.Mutable {
      evaluator.eval(stmt.value, environment)
    })
  }

  private fun eval(stmt: Stmt.TypeDef.Struct, environment: MutableEnvironment) {
    environment.define(stmt.name, KoflStruct(stmt).asKoflValue())
  }

  @OptIn(KoflResolverInternals::class)
  override fun eval(expr: Expr, environment: MutableEnvironment): Unit = when (expr) {
    is Expr.Func -> eval(expr, environment)
    is Expr.ExtensionFunc -> eval(expr, environment)
    else -> Unit
  }

  @OptIn(KoflResolverInternals::class)
  private fun eval(expr: Expr.Func, environment: MutableEnvironment) {
    locals[expr] = 0

    environment.define(expr.name, KoflCallable.Func(expr, evaluator).asKoflValue()).asKoflObject()
  }

  @OptIn(KoflResolverInternals::class)
  private fun eval(expr: Expr.ExtensionFunc, environment: MutableEnvironment) {
    locals[expr] = 0

    environment.define(expr.name, KoflCallable.ExtensionFunc(expr, evaluator).asKoflValue()).asKoflObject()
  }
}