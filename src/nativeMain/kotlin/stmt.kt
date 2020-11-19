interface StmtVisitor<T> {
  fun visit(exprStmt: Stmt.ExprStmt): T
  fun visit(printStmt: Stmt.PrintStmt): T
  fun visit(valDecl: Stmt.ValDecl): T
  fun visit(varDecl: Stmt.VarDecl): T
  fun visit(whileStmt: Stmt.WhileStmt): T
  fun visit(block: Stmt.Block): T
}

sealed class Stmt {
  abstract fun <T> accept(visitor: StmtVisitor<T>): T

  data class ExprStmt(val expr: Expr) : Stmt() {
    override fun <T> accept(visitor: StmtVisitor<T>) = visitor.visit(this)
  }

  data class PrintStmt(val expr: Expr) : Stmt() {
    override fun <T> accept(visitor: StmtVisitor<T>) = visitor.visit(this)
  }

  data class Block(val decls: List<Stmt>) : Stmt() {
    override fun <T> accept(visitor: StmtVisitor<T>) = visitor.visit(this)
  }

  // TODO: replace List<Stmt> with Stmt.Block
  data class WhileStmt(val condition: Expr, val body: List<Stmt>) : Stmt() {
    override fun <T> accept(visitor: StmtVisitor<T>) = visitor.visit(this)
  }

  // TODO: add type
  data class ValDecl(val name: Token, val value: Expr) : Stmt() {
    override fun <T> accept(visitor: StmtVisitor<T>) = visitor.visit(this)
  }

  // TODO: add type
  data class VarDecl(val name: Token, val value: Expr) : Stmt() {
    override fun <T> accept(visitor: StmtVisitor<T>) = visitor.visit(this)
  }
}