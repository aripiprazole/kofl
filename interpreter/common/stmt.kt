package com.lorenzoog.kofl.interpreter

class Return(val value: KoflObject) : RuntimeException(null, null)

sealed class Stmt {
  interface Visitor<T> {
    fun visit(exprs: List<Stmt>, environment: MutableEnvironment) = exprs.map { visit(it, environment) }
    fun visit(expr: Stmt, environment: MutableEnvironment): T = expr.accept(this, environment)

    fun visitExprStmt(stmt: ExprStmt, environment: MutableEnvironment): T
    fun visitBlockStmt(stmt: Block, environment: MutableEnvironment): T
    fun visitWhileStmt(stmt: WhileStmt, environment: MutableEnvironment): T
    fun visitReturnStmt(stmt: ReturnStmt, environment: MutableEnvironment): T
    fun visitValDeclStmt(stmt: ValDecl, environment: MutableEnvironment): T
    fun visitVarDeclStmt(stmt: VarDecl, environment: MutableEnvironment): T
    fun visitStructTypedefStmt(stmt: TypeDef.Struct, environment: MutableEnvironment): T
  }

  abstract fun <T> accept(visitor: Visitor<T>, environment: MutableEnvironment): T

  data class ExprStmt(val expr: Expr) : Stmt() {
    override fun <T> accept(visitor: Visitor<T>, environment: MutableEnvironment): T =
      visitor.visitExprStmt(this, environment)
  }

  data class Block(val decls: List<Stmt>) : Stmt() {
    override fun <T> accept(visitor: Visitor<T>, environment: MutableEnvironment): T =
      visitor.visitBlockStmt(this, environment)
  }

  data class WhileStmt(val condition: Expr, val body: List<Stmt>) : Stmt() {
    override fun <T> accept(visitor: Visitor<T>, environment: MutableEnvironment): T =
      visitor.visitWhileStmt(this, environment)
  }

  data class ReturnStmt(val expr: Expr) : Stmt() {
    override fun <T> accept(visitor: Visitor<T>, environment: MutableEnvironment): T =
      visitor.visitReturnStmt(this, environment)
  }

  data class ValDecl(val name: Token, val value: Expr) : Stmt() {
    override fun <T> accept(visitor: Visitor<T>, environment: MutableEnvironment): T =
      visitor.visitValDeclStmt(this, environment)
  }

  data class VarDecl(val name: Token, val value: Expr) : Stmt() {
    override fun <T> accept(visitor: Visitor<T>, environment: MutableEnvironment): T =
      visitor.visitVarDeclStmt(this, environment)
  }

  sealed class TypeDef : Stmt() {
    data class Struct(val name: Token, val fieldsDef: List<Token>) : TypeDef() {
      override fun <T> accept(visitor: Visitor<T>, environment: MutableEnvironment): T =
        visitor.visitStructTypedefStmt(this, environment)
    }
  }
}