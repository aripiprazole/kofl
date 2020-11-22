package com.lorenzoog.kofl.interpreter

fun dump(stmts: List<Stmt>) {
  println("simple ast dump:")
  println(stmts.joinToString(";\n"))
}

//fun dump(stmt: Stmt): String {
//  fun dump(stmt: Stmt.ExprStmt): String {
//    return "(${dump(stmt.expr)})"
//  }
//
//  fun dump(stmt: Stmt.PrintStmt): String {
//    return parenthesize("print", stmt.expr)
//  }
//
//  fun dump(stmt: Stmt.ValDecl): String {
//    return parenthesize("val", Expr.Literal(stmt.name), stmt.value)
//  }
//
//  fun dump(stmt: Stmt.VarDecl): String {
//    return parenthesize("var", Expr.Literal(stmt.name), stmt.value)
//  }
//
//  fun dump(stmt: Stmt.Block): String {
//    return parenthesize("block", *stmt.decls.toTypedArray())
//  }
//
//  fun dump(stmt: Stmt.WhileStmt): String {
//    return parenthesize("while", *stmt.body.toTypedArray())
//  }
//
//  return when (stmt) {
//    is Stmt.WhileStmt -> dump(stmt)
//    is Stmt.Block -> dump(stmt)
//    is Stmt.VarDecl -> dump(stmt)
//    is Stmt.ValDecl -> dump(stmt)
//    is Stmt.PrintStmt -> dump(stmt)
//    is Stmt.ExprStmt -> dump(stmt)
//  }
//}
//
//fun dump(expr: Expr): String {
//  fun dump(expr: Expr.Binary): String {
//    return parenthesize(expr.op.lexeme, expr.left, expr.right)
//  }
//
//  fun dump(expr: Expr.Grouping): String {
//    return parenthesize("group", expr.expr)
//  }
//
//  fun dump(expr: Expr.Unary): String {
//    return parenthesize(expr.op.lexeme, expr.right)
//  }
//
//  fun dump(expr: Expr.Assign): String {
//    return parenthesize("set", Expr.Literal(expr.name), expr.value)
//  }
//
//  fun dump(expr: Expr.Var): String {
//    return parenthesize("get", Expr.Literal(expr.name))
//  }
//
//  fun dump(expr: Expr.Logical): String {
//    return parenthesize(expr.op.lexeme, expr.left, expr.right)
//  }
//
//  fun dump(expr: Expr.Literal): String {
//    if (expr.value is String) return "\"${expr.value}\""
//
//    return expr.value.toString()
//  }
//
//  fun dump(expr: Expr.IfExpr): String {
//    return parenthesize("if ${dump(expr.condition)}", *expr.thenBranch.toTypedArray()) +
//      if (expr.elseBranch != null)
//        " " + parenthesize("else", *expr.elseBranch.toTypedArray())
//      else ""
//  }
//
//  fun dump(expr: Expr.Call): String {
//    return parenthesize(dump(expr.calle), *expr.arguments.toTypedArray())
//  }
//
//  fun dump(expr: Expr.Func): String {
//    return parenthesize(
//      op = "${expr.name} (${expr.arguments.joinToString(" ")})",
//      *expr.body.toTypedArray()
//    )
//  }
//
//  return when (expr) {
//    is Expr.Binary -> dump(expr)
//    is Expr.IfExpr -> dump(expr)
//    is Expr.Unary -> dump(expr)
//    is Expr.Grouping -> dump(expr)
//    is Expr.Assign -> dump(expr)
//    is Expr.Literal -> dump(expr)
//    is Expr.Var -> dump(expr)
//    is Expr.Logical -> dump(expr)
//    is Expr.Call -> dump(expr)
//    is Expr.Func -> dump(expr)
//  }
//}
//
//private fun parenthesize(op: String, vararg stmts: Stmt): String = "($op ${
//  stmts.joinToString(" ") {
//    dump(it)
//  }
//})"
//
//private fun parenthesize(op: String, vararg exprs: Expr): String = "($op ${
//  exprs.joinToString(" ") {
//    dump(it)
//  }
//})"
//
