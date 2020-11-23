package com.lorenzoog.kofl.interpreter

/**
 * This class will push all global declarations lazy
 * to globalEnvironment, that was made to make
 * typechecking easier
 */
class DeclEvaluator(
  private val locals: MutableMap<Expr, Int>,
  private val evaluator: CodeEvaluator
) : Expr.Visitor<Unit> by DefaultExprVisitor(Unit),
  Stmt.Visitor<Unit> by DefaultStmtVisitor(Unit) {
  override fun visitValDeclStmt(stmt: Stmt.ValDecl, environment: MutableEnvironment) {
    environment.define(stmt.name, KoflValue.Lazy.Immutable {
      evaluator.visit(stmt.value, environment)
    })
  }

  override fun visitVarDeclStmt(stmt: Stmt.VarDecl, environment: MutableEnvironment) {
    environment.define(stmt.name, KoflValue.Lazy.Mutable {
      evaluator.visit(stmt.value, environment)
    })
  }

  override fun visitStructTypedefStmt(stmt: Stmt.TypeDef.Struct, environment: MutableEnvironment) {
    environment.define(stmt.name, KoflStruct(stmt).asKoflValue())
  }

  @OptIn(KoflResolverInternals::class)
  override fun visitFuncExpr(expr: Expr.Func, environment: MutableEnvironment) {
    locals[expr] = 0

    environment.define(expr.name, KoflCallable.Func(expr, evaluator).asKoflValue()).asKoflObject()
  }

  @OptIn(KoflResolverInternals::class)
  override fun visitExtensionFuncExpr(expr: Expr.ExtensionFunc, environment: MutableEnvironment) {
    locals[expr] = 0

    environment.define(expr.name, KoflCallable.ExtensionFunc(expr, evaluator).asKoflValue()).asKoflObject()
  }
}