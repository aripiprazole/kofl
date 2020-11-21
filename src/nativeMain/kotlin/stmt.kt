class Return(val value: KoflObject) : RuntimeException(null, null)

sealed class Stmt {
  data class ExprStmt(val expr: Expr) : Stmt()
  data class Block(val decls: List<Stmt>) : Stmt()
  data class WhileStmt(val condition: Expr, val body: List<Stmt>) : Stmt()
  data class ReturnStmt(val expr: Expr) : Stmt()
  data class ValDecl(val name: Token, val value: Expr) : Stmt()
  data class VarDecl(val name: Token, val value: Expr) : Stmt()

  sealed class TypeDef : Stmt() {
    data class Struct(val name: Token, val fields: List<Token>) : TypeDef()
  }
}