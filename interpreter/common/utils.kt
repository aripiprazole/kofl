package com.lorenzoog.kofl.interpreter

fun <T> DefaultExprVisitor(value: T): Expr.Visitor<T> = object : Expr.Visitor<T> {
  override fun visitAssignExpr(expr: Expr.Assign, environment: MutableEnvironment): T = value
  override fun visitBinaryExpr(expr: Expr.Binary, environment: MutableEnvironment): T = value
  override fun visitLogicalExpr(expr: Expr.Logical, environment: MutableEnvironment): T = value
  override fun visitGroupingExpr(expr: Expr.Grouping, environment: MutableEnvironment): T = value
  override fun visitLiteralExpr(expr: Expr.Literal, environment: MutableEnvironment): T = value
  override fun visitUnaryExpr(expr: Expr.Unary, environment: MutableEnvironment): T = value
  override fun visitVarExpr(expr: Expr.Var, environment: MutableEnvironment): T = value
  override fun visitCallExpr(expr: Expr.Call, environment: MutableEnvironment): T = value
  override fun visitGetExpr(expr: Expr.Get, environment: MutableEnvironment): T = value
  override fun visitSetExpr(expr: Expr.Set, environment: MutableEnvironment): T = value
  override fun visitFuncExpr(expr: Expr.Func, environment: MutableEnvironment): T = value
  override fun visitThisExpr(expr: Expr.ThisExpr, environment: MutableEnvironment): T = value
  override fun visitExtensionFuncExpr(expr: Expr.ExtensionFunc, environment: MutableEnvironment): T = value
  override fun visitAnonymousFuncExpr(expr: Expr.AnonymousFunc, environment: MutableEnvironment): T = value
  override fun visitNativeFuncExpr(expr: Expr.NativeFunc, environment: MutableEnvironment): T = value
  override fun visitIfExpr(expr: Expr.IfExpr, environment: MutableEnvironment): T = value
}

fun <T> DefaultStmtVisitor(value: T): Stmt.Visitor<T> = object : Stmt.Visitor<T> {
  override fun visitExprStmt(stmt: Stmt.ExprStmt, environment: MutableEnvironment): T = value
  override fun visitBlockStmt(stmt: Stmt.Block, environment: MutableEnvironment): T = value
  override fun visitWhileStmt(stmt: Stmt.WhileStmt, environment: MutableEnvironment): T = value
  override fun visitReturnStmt(stmt: Stmt.ReturnStmt, environment: MutableEnvironment): T = value
  override fun visitValDeclStmt(stmt: Stmt.ValDecl, environment: MutableEnvironment): T = value
  override fun visitVarDeclStmt(stmt: Stmt.VarDecl, environment: MutableEnvironment): T = value
  override fun visitStructTypedefStmt(stmt: Stmt.TypeDef.Struct, environment: MutableEnvironment): T = value
}

@Suppress("SpellCheckingInspection")
fun printerr(msg: String = "") {
  println(msg)
}
