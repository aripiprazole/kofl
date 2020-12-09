@file:OptIn(ExperimentalUnsignedTypes::class)

package com.lorenzoog.kofl.vm

import com.lorenzoog.kofl.frontend.Expr
import com.lorenzoog.kofl.frontend.Stmt
import com.lorenzoog.kofl.frontend.TokenType
import platform.posix.UINT8_MAX

open class CompilationException(message: String) : RuntimeException(message)

class Compiler : Expr.Visitor<Unit>, Stmt.Visitor<Unit> {
  // chunk index
  private var ci = 0
  private var chunks = arrayOf(Chunk())

  fun compile(stmts: List<Stmt>): Array<Chunk> {
    visitStmts(stmts)
    endCompiler()

    return chunks
  }

  fun compile(exprs: List<Expr>): Array<Chunk> {
    visitExprs(exprs)
    endCompiler()

    return chunks
  }

  override fun visitAssignExpr(expr: Expr.Assign) {
    TODO("Not yet implemented")
  }

  override fun visitBinaryExpr(expr: Expr.Binary) {
    visitExpr(expr.left)
    visitExpr(expr.right)

    when (expr.op.type) {
      TokenType.Plus -> emit(OpCode.OpSum, expr.line) // TODO: compile OpCode.Concat when have typechecking
      TokenType.Minus -> emit(OpCode.OpSubtract, expr.line)
      TokenType.Slash -> emit(OpCode.OpDivide, expr.line)
      TokenType.Star -> emit(OpCode.OpMultiply, expr.line)
      else -> Unit
    }
  }

  override fun visitLogicalExpr(expr: Expr.Logical) {
    TODO("Not yet implemented")
  }

  override fun visitGroupingExpr(expr: Expr.Grouping) {
    visitExpr(expr.expr)
  }

  override fun visitLiteralExpr(expr: Expr.Literal) {
    when (val value = expr.value) {
      is Int -> emit(OpCode.OpConstant, makeConst(value), expr.line)
      is Number -> emit(OpCode.OpConstant, makeConst(value.toDouble()), expr.line)
      is Boolean -> emit(OpCode.OpConstant, makeConst(value), expr.line)
      is String -> emit(OpCode.OpConstant, makeConst(value), expr.line)
    }
  }

  override fun visitUnaryExpr(expr: Expr.Unary) {
    visitExpr(expr.right)

    when (expr.op.type) {
      TokenType.Minus -> emit(OpCode.OpNegate, expr.line)
      TokenType.Bang -> emit(OpCode.OpNot, expr.line)
      else -> {
      }
    }
  }

  override fun visitVarExpr(expr: Expr.Var) {
    emit(OpCode.OpConstant, makeConst(expr.name.lexeme), expr.line)
    emit(OpCode.OpAccessGlobal, expr.line)
  }

  override fun visitCallExpr(expr: Expr.Call) {
    TODO("Not yet implemented")
  }

  override fun visitGetExpr(expr: Expr.Get) {
    TODO("Not yet implemented")
  }

  override fun visitSetExpr(expr: Expr.Set) {
    TODO("Not yet implemented")
  }

  override fun visitFuncExpr(expr: Expr.CommonFunc) {
    TODO("Not yet implemented")
  }

  override fun visitThisExpr(expr: Expr.ThisExpr) {
    TODO("Not yet implemented")
  }

  override fun visitExtensionFuncExpr(expr: Expr.ExtensionFunc) {
    TODO("Not yet implemented")
  }

  override fun visitAnonymousFuncExpr(expr: Expr.AnonymousFunc) {
    TODO("Not yet implemented")
  }

  override fun visitNativeFuncExpr(expr: Expr.NativeFunc) {
    TODO("Not yet implemented")
  }

  override fun visitIfExpr(expr: Expr.IfExpr) {
    TODO("Not yet implemented")
  }

  override fun visitExprStmt(stmt: Stmt.ExprStmt) {
    visitExpr(stmt.expr)
  }

  override fun visitBlockStmt(stmt: Stmt.Block) {
    TODO("Not yet implemented")
  }

  override fun visitWhileStmt(stmt: Stmt.WhileStmt) {
    TODO("Not yet implemented")
  }

  override fun visitReturnStmt(stmt: Stmt.ReturnStmt) {
    TODO("Not yet implemented")
  }

  override fun visitValDeclStmt(stmt: Stmt.ValDecl) {
    emit(OpCode.OpConstant, makeConst(stmt.name.lexeme), stmt.line)
    visitExpr(stmt.value)
    emit(OpCode.OpStoreGlobal, stmt.line)
  }

  override fun visitVarDeclStmt(stmt: Stmt.VarDecl) {
    TODO("Not yet implemented")
  }

  override fun visitClassTypeStmt(stmt: Stmt.Type.Class) {
    TODO("Not yet implemented")
  }

  private fun makeConst(value: Boolean) = makeConst(BoolValue(value))
  private fun makeConst(value: Int) = makeConst(IntValue(value))
  private fun makeConst(value: Double) = makeConst(DoubleValue(value))
  private fun makeConst(value: String) = makeConst(StrValue(value))

  private fun <T> makeConst(value: Value<T>): UByte {
    val const = chunk().addConstant(value)

    if (const > UINT8_MAX) {
      error("TOO LONG CONST") // TODO: make a error
    }

    return const.toUByte()
  }

  private fun chunk(): Chunk {
    return chunks[ci]
  }

  private fun endCompiler() {
    emit(OpCode.OpReturn, -1)
  }

  private fun emit(byte: UByte, line: Int) {
    chunk().write(byte, line)
  }

  private fun emit(op: UByte, value: UByte, line: Int) {
    emit(op, line)
    emit(value, line)
  }
}