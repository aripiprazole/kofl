package com.lorenzoog.kofl.frontend

sealed class Stmt {
  interface Visitor<T> {
    fun visitStmts(stmts: List<Stmt>) = stmts.map { visitStmt(it) }
    fun visitStmt(expr: Stmt): T = expr.accept(this)

    fun visitExprStmt(stmt: ExprStmt): T
    fun visitBlockStmt(stmt: Block): T
    fun visitWhileStmt(stmt: WhileStmt): T
    fun visitReturnStmt(stmt: ReturnStmt): T
    fun visitValDeclStmt(stmt: ValDecl): T
    fun visitVarDeclStmt(stmt: VarDecl): T
    fun visitStructTypedefStmt(stmt: Type.Class): T
  }

  abstract val line: Int

  abstract fun <T> accept(visitor: Visitor<T>): T

  data class ExprStmt(val expr: Expr, override val line: Int) : Stmt() {
    override fun <T> accept(visitor: Visitor<T>): T = visitor.visitExprStmt(this)
  }

  data class Block(val body: List<Stmt>, override val line: Int) : Stmt() {
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

  sealed class Type : Stmt() {
    data class Class(val name: Token, val parameters: Map<Token, Token>, override val line: Int) : Type() {
      override fun <T> accept(visitor: Visitor<T>): T = visitor.visitStructTypedefStmt(this)
    }
  }
}