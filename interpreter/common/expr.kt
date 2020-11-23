package com.lorenzoog.kofl.interpreter

sealed class Expr {
  interface Visitor<T> {
    fun visit(exprs: List<Expr>, environment: MutableEnvironment) = exprs.map { visit(it,environment) }
    fun visit(expr: Expr, environment: MutableEnvironment): T = expr.accept(this,environment)

    fun visitAssignExpr(expr: Assign, environment: MutableEnvironment): T
    fun visitBinaryExpr(expr: Binary, environment: MutableEnvironment): T
    fun visitLogicalExpr(expr: Logical, environment: MutableEnvironment): T
    fun visitGroupingExpr(expr: Grouping, environment: MutableEnvironment): T
    fun visitLiteralExpr(expr: Literal, environment: MutableEnvironment): T
    fun visitUnaryExpr(expr: Unary, environment: MutableEnvironment): T
    fun visitVarExpr(expr: Var, environment: MutableEnvironment): T
    fun visitCallExpr(expr: Call, environment: MutableEnvironment): T
    fun visitGetExpr(expr: Get, environment: MutableEnvironment): T
    fun visitSetExpr(expr: Set, environment: MutableEnvironment): T
    fun visitFuncExpr(expr: Func, environment: MutableEnvironment): T
    fun visitThisExpr(expr: ThisExpr, environment: MutableEnvironment): T
    fun visitExtensionFuncExpr(expr: ExtensionFunc, environment: MutableEnvironment): T
    fun visitAnonymousFuncExpr(expr: AnonymousFunc, environment: MutableEnvironment): T
    fun visitNativeFuncExpr(expr: NativeFunc, environment: MutableEnvironment): T
    fun visitIfExpr(expr: IfExpr, environment: MutableEnvironment): T
  }

  abstract fun <T> accept(visitor: Visitor<T>, environment: MutableEnvironment): T

  data class Assign(val name: Token, val value: Expr) : Expr() {
    override fun <T> accept(visitor: Visitor<T>, environment: MutableEnvironment): T =
      visitor.visitAssignExpr(this, environment)
  }

  data class Binary(val left: Expr, val op: Token, val right: Expr) : Expr() {
    override fun <T> accept(visitor: Visitor<T>, environment: MutableEnvironment): T =
      visitor.visitBinaryExpr(this, environment)
  }

  data class Logical(val left: Expr, val op: Token, val right: Expr) : Expr() {
    override fun <T> accept(visitor: Visitor<T>, environment: MutableEnvironment): T =
      visitor.visitLogicalExpr(this, environment)
  }

  data class Grouping(val expr: Expr) : Expr() {
    override fun <T> accept(visitor: Visitor<T>, environment: MutableEnvironment): T =
      visitor.visitGroupingExpr(this, environment)
  }

  data class Literal(val value: Any) : Expr() {
    override fun <T> accept(visitor: Visitor<T>, environment: MutableEnvironment): T =
      visitor.visitLiteralExpr(this, environment)
  }

  data class Unary(val op: Token, val right: Expr) : Expr() {
    override fun <T> accept(visitor: Visitor<T>, environment: MutableEnvironment): T =
      visitor.visitUnaryExpr(this, environment)
  }

  data class Var(val name: Token) : Expr() {
    override fun <T> accept(visitor: Visitor<T>, environment: MutableEnvironment): T =
      visitor.visitVarExpr(this, environment)
  }

  data class Call(val calle: Expr, val arguments: List<Expr>) : Expr() {
    override fun <T> accept(visitor: Visitor<T>, environment: MutableEnvironment): T =
      visitor.visitCallExpr(this, environment)
  }

  data class Get(val receiver: Expr, val name: Token) : Expr() {
    override fun <T> accept(visitor: Visitor<T>, environment: MutableEnvironment): T =
      visitor.visitGetExpr(this, environment)
  }

  data class Set(val receiver: Expr, val name: Token, val value: Expr) : Expr() {
    override fun <T> accept(visitor: Visitor<T>, environment: MutableEnvironment): T =
      visitor.visitSetExpr(this, environment)
  }

  data class Func(val name: Token, val arguments: List<Token>, val body: List<Stmt>) : Expr() {
    override fun <T> accept(visitor: Visitor<T>, environment: MutableEnvironment): T =
      visitor.visitFuncExpr(this, environment)
  }

  data class ThisExpr(val keyword: Token) : Expr() {
    override fun <T> accept(visitor: Visitor<T>, environment: MutableEnvironment): T =
      visitor.visitThisExpr(this, environment)
  }

  data class ExtensionFunc(
    val receiver: Token, val name: Token,
    val arguments: List<Token>, val body: List<Stmt>
  ) : Expr() {
    override fun <T> accept(visitor: Visitor<T>, environment: MutableEnvironment): T =
      visitor.visitExtensionFuncExpr(this, environment)
  }

  data class AnonymousFunc(val arguments: List<Token>, val body: List<Stmt>) : Expr() {
    override fun <T> accept(visitor: Visitor<T>, environment: MutableEnvironment): T =
      visitor.visitAnonymousFuncExpr(this, environment)
  }

  data class NativeFunc(val name: Token, val arguments: List<Token>) : Expr() {
    override fun <T> accept(visitor: Visitor<T>, environment: MutableEnvironment): T =
      visitor.visitNativeFuncExpr(this, environment)
  }

  data class IfExpr(val condition: Expr, val thenBranch: List<Stmt>, val elseBranch: List<Stmt>?) : Expr() {
    override fun <T> accept(visitor: Visitor<T>, environment: MutableEnvironment): T =
      visitor.visitIfExpr(this, environment)
  }
}
