package com.lorenzoog.kofl.frontend

sealed class Stmt {
  interface Visitor<T> {
    fun visit(exprs: List<Stmt>) = exprs.map { visit(it) }
    fun visit(expr: Stmt): T = expr.accept(this)

    fun visitExprStmt(stmt: ExprStmt): T
    fun visitBlockStmt(stmt: Block): T
    fun visitWhileStmt(stmt: WhileStmt): T
    fun visitReturnStmt(stmt: ReturnStmt): T
    fun visitValDeclStmt(stmt: ValDecl): T
    fun visitVarDeclStmt(stmt: VarDecl): T
    fun visitStructTypedefStmt(stmt: TypeDef.Struct): T
  }

  abstract val line: Int

  abstract fun <T> accept(visitor: Visitor<T>): T

  data class ExprStmt(val expr: Expr, override val line: Int) : Stmt() {
    override fun <T> accept(visitor: Visitor<T>): T = visitor.visitExprStmt(this)
  }

  data class Block(val decls: List<Stmt>, override val line: Int) : Stmt() {
    override fun <T> accept(visitor: Visitor<T>): T = visitor.visitBlockStmt(this)
  }

  data class WhileStmt(val condition: Expr, val body: List<Stmt>, override val line: Int) : Stmt() {
    override fun <T> accept(visitor: Visitor<T>): T = visitor.visitWhileStmt(this)
  }

  data class ReturnStmt(val expr: Expr, override val line: Int) : Stmt() {
    override fun <T> accept(visitor: Visitor<T>): T = visitor.visitReturnStmt(this)
  }

  data class ValDecl(val name: Token, val type: Token?, val value: Expr, override val line: Int) : Stmt() {
    override fun <T> accept(visitor: Visitor<T>): T = visitor.visitValDeclStmt(this)
  }

  data class VarDecl(val name: Token, val type: Token?, val value: Expr, override val line: Int) : Stmt() {
    override fun <T> accept(visitor: Visitor<T>): T = visitor.visitVarDeclStmt(this)
  }

  sealed class TypeDef : Stmt() {
    data class Struct(val name: Token, val fields: Map<Token, Token>, override val line: Int) : TypeDef() {
      override fun <T> accept(visitor: Visitor<T>): T = visitor.visitStructTypedefStmt(this)
    }
  }
}