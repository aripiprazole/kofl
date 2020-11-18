interface StmtVisitor<T> {
  fun visit(exprStmt: Stmt.ExprStmt): T
  fun visit(printStmt: Stmt.PrintStmt): T
  fun visit(valDecl: Stmt.ValDecl): T
}

sealed class Stmt {
  abstract fun <T> accept(visitor: StmtVisitor<T>): T

  data class ExprStmt(val expr: Expr) : Stmt() {
    override fun <T> accept(visitor: StmtVisitor<T>) = visitor.visit(this)
  }

  data class PrintStmt(val expr: Expr) : Stmt() {
    override fun <T> accept(visitor: StmtVisitor<T>) = visitor.visit(this)
  }

  // TODO: add type
  data class ValDecl(val name: Token, val value: Expr) : Stmt() {
    override fun <T> accept(visitor: StmtVisitor<T>) = visitor.visit(this)
  }
}