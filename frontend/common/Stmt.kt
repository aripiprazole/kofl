package me.devgabi.kofl.frontend

sealed class Stmt {
  interface Visitor<T> {
    fun visitStmts(stmts: Collection<Stmt>) = stmts.map { visitStmt(it) }
    fun visitStmt(stmt: Stmt): T = stmt.accept(this)

    fun visitExprStmt(stmt: ExprStmt): T
    fun visitBlockStmt(stmt: Block): T
    fun visitWhileStmt(stmt: WhileStmt): T
    fun visitReturnStmt(stmt: ReturnStmt): T
    fun visitValDeclStmt(stmt: ValDecl): T
    fun visitVarDeclStmt(stmt: VarDecl): T
    fun visitUseStmt(stmt: UseDecl): T
    fun visitModuleStmt(stmt: ModuleDecl): T
    fun visitTypeRecordStmt(stmt: Type.Record): T
  }

  abstract val line: Int

  abstract fun <T> accept(visitor: Visitor<T>): T

  data class ModuleDecl(val module: Token, override val line: Int) : Stmt() {
    override fun <T> accept(visitor: Visitor<T>): T = visitor.visitModuleStmt(this)
  }

  data class UseDecl(val module: Token, override val line: Int) : Stmt() {
    override fun <T> accept(visitor: Visitor<T>): T = visitor.visitUseStmt(this)
  }

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

  data class CommentDecl(val content: String, override val line: Int) : Stmt() {
    override fun <T> accept(visitor: Visitor<T>): T = TODO("CommentDecl shouldn't be visited yet!")
  }

  data class ValDecl(val name: Token, val type: Token?, val value: Expr, override val line: Int) :
    Stmt() {
    override fun <T> accept(visitor: Visitor<T>): T = visitor.visitValDeclStmt(this)
  }

  data class VarDecl(val name: Token, val type: Token?, val value: Expr, override val line: Int) :
    Stmt() {
    override fun <T> accept(visitor: Visitor<T>): T = visitor.visitVarDeclStmt(this)
  }

  sealed class Type : Stmt() {
    data class Record(val name: Token, val parameters: Map<Token, Token>, override val line: Int) :
      Type() {
      override fun <T> accept(visitor: Visitor<T>): T = visitor.visitTypeRecordStmt(this)
    }
  }
}
