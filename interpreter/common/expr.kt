package com.lorenzoog.kofl.interpreter

sealed class Expr {
  interface Visitor<T> {
    fun visit(exprs: List<Expr>) = exprs.map { visit(it) }
    fun visit(expr: Expr): T = expr.accept(this)

    fun visitAssignExpr(expr: Assign): T
    fun visitBinaryExpr(expr: Binary): T
    fun visitLogicalExpr(expr: Logical): T
    fun visitGroupingExpr(expr: Grouping): T
    fun visitLiteralExpr(expr: Literal): T
    fun visitUnaryExpr(expr: Unary): T
    fun visitVarExpr(expr: Var): T
    fun visitCallExpr(expr: Call): T
    fun visitGetExpr(expr: Get): T
    fun visitSetExpr(expr: Set): T
    fun visitFuncExpr(expr: CommonFunc): T
    fun visitThisExpr(expr: ThisExpr): T
    fun visitExtensionFuncExpr(expr: ExtensionFunc): T
    fun visitAnonymousFuncExpr(expr: AnonymousFunc): T
    fun visitNativeFuncExpr(expr: NativeFunc): T
    fun visitIfExpr(expr: IfExpr): T
  }

  abstract val line: Int

  abstract fun <T> accept(visitor: Visitor<T>): T

  data class Assign(val name: Token, val value: Expr, override val line: Int) : Expr() {
    override fun <T> accept(visitor: Visitor<T>): T = visitor.visitAssignExpr(this)
  }

  data class Binary(val left: Expr, val op: Token, val right: Expr, override val line: Int) : Expr() {
    override fun <T> accept(visitor: Visitor<T>): T = visitor.visitBinaryExpr(this)
  }

  data class Logical(val left: Expr, val op: Token, val right: Expr, override val line: Int) : Expr() {
    override fun <T> accept(visitor: Visitor<T>): T = visitor.visitLogicalExpr(this)
  }

  data class Grouping(val expr: Expr, override val line: Int) : Expr() {
    override fun <T> accept(visitor: Visitor<T>): T = visitor.visitGroupingExpr(this)
  }

  data class Literal(val value: Any, override val line: Int) : Expr() {
    override fun <T> accept(visitor: Visitor<T>): T = visitor.visitLiteralExpr(this)
  }

  data class Unary(val op: Token, val right: Expr, override val line: Int) : Expr() {
    override fun <T> accept(visitor: Visitor<T>): T = visitor.visitUnaryExpr(this)
  }

  data class Var(val name: Token, override val line: Int) : Expr() {
    override fun <T> accept(visitor: Visitor<T>): T = visitor.visitVarExpr(this)
  }

  data class Call(val calle: Expr, val arguments: Map<Token?, Expr>, override val line: Int) : Expr() {
    override fun <T> accept(visitor: Visitor<T>): T = visitor.visitCallExpr(this)
  }

  data class Get(val receiver: Expr, val name: Token, override val line: Int) : Expr() {
    override fun <T> accept(visitor: Visitor<T>): T = visitor.visitGetExpr(this)
  }

  data class Set(val receiver: Expr, val name: Token, val value: Expr, override val line: Int) : Expr() {
    override fun <T> accept(visitor: Visitor<T>): T = visitor.visitSetExpr(this)
  }

  data class ThisExpr(val keyword: Token, override val line: Int) : Expr() {
    override fun <T> accept(visitor: Visitor<T>): T = visitor.visitThisExpr(this)
  }

  data class IfExpr(
    val condition: Expr,
    val thenBranch: List<Stmt>,
    val elseBranch: List<Stmt>?,
    override val line: Int
  ) : Expr() {
    override fun <T> accept(visitor: Visitor<T>): T = visitor.visitIfExpr(this)
  }

  data class CommonFunc(
    val name: Token,
    val arguments: Map<Token, Token>,
    val body: List<Stmt>,
    val returnType: Token?,
    override val line: Int
  ) : Expr() {
    override fun <T> accept(visitor: Visitor<T>): T = visitor.visitFuncExpr(this)
  }

  data class ExtensionFunc(
    val receiver: Token,
    val name: Token,
    val arguments: Map<Token, Token>,
    val body: List<Stmt>,
    val returnType: Token?,
    override val line: Int
  ) : Expr() {
    override fun <T> accept(visitor: Visitor<T>): T = visitor.visitExtensionFuncExpr(this)
  }

  data class AnonymousFunc(
    val arguments: Map<Token, Token>,
    val body: List<Stmt>,
    val returnType: Token?,
    override val line: Int
  ) : Expr() {
    override fun <T> accept(visitor: Visitor<T>): T = visitor.visitAnonymousFuncExpr(this)
  }

  data class NativeFunc(
    val name: Token,
    val arguments: Map<Token, Token>,
    val returnType: Token?,
    override val line: Int
  ) : Expr() {
    override fun <T> accept(visitor: Visitor<T>): T = visitor.visitNativeFuncExpr(this)
  }
}
