sealed class Stmt {
  data class ExprStmt(val expr: Expr) : Stmt()
  data class PrintStmt(val expr: Expr) : Stmt()
  data class Block(val decls: List<Stmt>) : Stmt()

  // TODO: replace List<Stmt> with Stmt.Block
  data class WhileStmt(val condition: Expr, val body: List<Stmt>) : Stmt()

  // TODO: add type
  data class ValDecl(val name: Token, val value: Expr) : Stmt()

  // TODO: add type
  data class VarDecl(val name: Token, val value: Expr) : Stmt()
}