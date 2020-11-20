fun dump(stmts: Collection<Stmt>) {
  println("simple ast dump:")
  println(stmts.joinToString("\n") { dump(it) })
}

fun dump(stmt: Stmt): String {
  fun dump(exprStmt: Stmt.ExprStmt): String {
    return "(${dump(exprStmt.expr)})"
  }

  fun dump(printStmt: Stmt.PrintStmt): String {
    return parenthesize("print", printStmt.expr)
  }

  fun dump(valDecl: Stmt.ValDecl): String {
    return parenthesize("val", Expr.Literal(valDecl.name), valDecl.value)
  }

  fun dump(varDecl: Stmt.VarDecl): String {
    return parenthesize("var", Expr.Literal(varDecl.name), varDecl.value)
  }

  fun dump(block: Stmt.Block): String {
    return parenthesize("block", *block.decls.toTypedArray())
  }

  fun dump(whileStmt: Stmt.WhileStmt): String {
    return parenthesize("while", *whileStmt.body.toTypedArray())
  }

  return when (stmt) {
    is Stmt.WhileStmt -> dump(stmt)
    is Stmt.Block -> dump(stmt)
    is Stmt.VarDecl -> dump(stmt)
    is Stmt.ValDecl -> dump(stmt)
    is Stmt.PrintStmt -> dump(stmt)
    is Stmt.ExprStmt -> dump(stmt)
  }
}

fun dump(expr: Expr): String {
  fun dump(binary: Expr.Binary): String {
    return parenthesize(binary.op.lexeme, binary.left, binary.right)
  }

  fun dump(grouping: Expr.Grouping): String {
    return parenthesize("group", grouping.expr)
  }

  fun dump(unary: Expr.Unary): String {
    return parenthesize(unary.op.lexeme, unary.right)
  }

  fun dump(assign: Expr.Assign): String {
    return parenthesize("set", Expr.Literal(assign.name), assign.value)
  }

  fun dump(varExpr: Expr.Var): String {
    return parenthesize("get", Expr.Literal(varExpr.name))
  }

  fun dump(logical: Expr.Logical): String {
    return parenthesize(logical.op.lexeme, logical.left, logical.right)
  }

  fun dump(literal: Expr.Literal): String {
    if (literal.value is String) return "\"${literal.value}\""

    return literal.value.toString()
  }

  fun dump(ifExpr: Expr.IfExpr): String {
    return parenthesize("if ${dump(ifExpr.condition)}", *ifExpr.thenBranch.toTypedArray()) +
      if (ifExpr.elseBranch != null)
        " " + parenthesize("else", *ifExpr.elseBranch.toTypedArray())
      else ""
  }
  return when (expr) {
    is Expr.Binary -> dump(expr)
    is Expr.IfExpr -> dump(expr)
    is Expr.Unary -> dump(expr)
    is Expr.Grouping -> dump(expr)
    is Expr.Assign -> dump(expr)
    is Expr.Literal -> dump(expr)
    is Expr.Var -> dump(expr)
    is Expr.Logical -> dump(expr)
  }
}

private fun parenthesize(op: String, vararg stmts: Stmt): String = "($op ${
  stmts.joinToString(" ") {
    dump(it)
  }
})"

private fun parenthesize(op: String, vararg exprs: Expr): String = "($op ${
  exprs.joinToString(" ") {
    dump(it)
  }
})"

