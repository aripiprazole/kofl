interface ExprVisitor<T> {
  fun visit(binary: Expr.Binary): T
  fun visit(grouping: Expr.Grouping): T
  fun visit(literal: Expr.Literal): T
  fun visit(unary: Expr.Unary): T
}

sealed class Expr {
  abstract fun <T> accept(visitor: ExprVisitor<T>): T

  data class Binary(val left: Expr, val op: Token, val right: Expr) : Expr() {
    override fun <T> accept(visitor: ExprVisitor<T>) = visitor.visit(this)
  }

  data class Grouping(val expr: Expr) : Expr() {
    override fun <T> accept(visitor: ExprVisitor<T>) = visitor.visit(this)
  }

  data class Literal(val value: Any) : Expr() {
    override fun <T> accept(visitor: ExprVisitor<T>) = visitor.visit(this)
  }

  data class Unary(val op: Token, val right: Expr) : Expr() {
    override fun <T> accept(visitor: ExprVisitor<T>) = visitor.visit(this)
  }
}
