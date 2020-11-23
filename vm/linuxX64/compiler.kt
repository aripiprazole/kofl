@file:OptIn(ExperimentalUnsignedTypes::class)

package com.lorenzoog.kofl.vm

import com.lorenzoog.kofl.interpreter.Expr
import com.lorenzoog.kofl.interpreter.MutableEnvironment
import com.lorenzoog.kofl.interpreter.Stmt
import com.lorenzoog.kofl.interpreter.TokenType
import platform.posix.UINT8_MAX

open class CompilationException : RuntimeException()

class Compiler : Expr.Visitor<Unit>, Stmt.Visitor<Unit> {
  // chunk index
  private var ci = 0
  private var chunks = arrayOf(Chunk())

  fun compile(exprs: List<Expr>, environment: MutableEnvironment): Array<Chunk> {
    visit(exprs, environment)
    endCompiler()

    return chunks
  }

  override fun visitAssignExpr(expr: Expr.Assign, environment: MutableEnvironment) {
    TODO("Not yet implemented")
  }

  override fun visitBinaryExpr(expr: Expr.Binary, environment: MutableEnvironment) {
    visit(expr.left, environment)
    visit(expr.right, environment)

    when (expr.op.type) {
      TokenType.Plus -> emit(OpCode.OpSum, expr.line)
      TokenType.Minus -> emit(OpCode.OpSubtract, expr.line)
      TokenType.Slash -> emit(OpCode.OpDivide, expr.line)
      TokenType.Star -> emit(OpCode.OpMultiply, expr.line)
      else -> Unit
    }
  }

  override fun visitLogicalExpr(expr: Expr.Logical, environment: MutableEnvironment) {
    TODO("Not yet implemented")
  }

  override fun visitGroupingExpr(expr: Expr.Grouping, environment: MutableEnvironment) {
    visit(expr.expr, environment)
  }

  override fun visitLiteralExpr(expr: Expr.Literal, environment: MutableEnvironment) {
    when (expr.value) {
      is Int -> emit(OpCode.OpConstant, makeConst(expr.value as Int), expr.line)
      is Number -> emit(OpCode.OpConstant, makeConst((expr.value as Number).toDouble()), expr.line)
      is Boolean -> emit(OpCode.OpConstant, makeConst(expr.value as Boolean), expr.line)
    }
  }

  override fun visitUnaryExpr(expr: Expr.Unary, environment: MutableEnvironment) {
    visit(expr.right, environment)

    when (expr.op.type) {
      TokenType.Minus -> emit(OpCode.OpNegate, expr.line)
      TokenType.Bang -> emit(OpCode.OpNot, expr.line)
    }
  }

  override fun visitVarExpr(expr: Expr.Var, environment: MutableEnvironment) {
    TODO("Not yet implemented")
  }

  override fun visitCallExpr(expr: Expr.Call, environment: MutableEnvironment) {
    TODO("Not yet implemented")
  }

  override fun visitGetExpr(expr: Expr.Get, environment: MutableEnvironment) {
    TODO("Not yet implemented")
  }

  override fun visitSetExpr(expr: Expr.Set, environment: MutableEnvironment) {
    TODO("Not yet implemented")
  }

  override fun visitFuncExpr(expr: Expr.Func, environment: MutableEnvironment) {
    TODO("Not yet implemented")
  }

  override fun visitThisExpr(expr: Expr.ThisExpr, environment: MutableEnvironment) {
    TODO("Not yet implemented")
  }

  override fun visitExtensionFuncExpr(expr: Expr.ExtensionFunc, environment: MutableEnvironment) {
    TODO("Not yet implemented")
  }

  override fun visitAnonymousFuncExpr(expr: Expr.AnonymousFunc, environment: MutableEnvironment) {
    TODO("Not yet implemented")
  }

  override fun visitNativeFuncExpr(expr: Expr.NativeFunc, environment: MutableEnvironment) {
    TODO("Not yet implemented")
  }

  override fun visitIfExpr(expr: Expr.IfExpr, environment: MutableEnvironment) {
    TODO("Not yet implemented")
  }

  override fun visitExprStmt(stmt: Stmt.ExprStmt, environment: MutableEnvironment) {
    TODO("Not yet implemented")
  }

  override fun visitBlockStmt(stmt: Stmt.Block, environment: MutableEnvironment) {
    TODO("Not yet implemented")
  }

  override fun visitWhileStmt(stmt: Stmt.WhileStmt, environment: MutableEnvironment) {
    TODO("Not yet implemented")
  }

  override fun visitReturnStmt(stmt: Stmt.ReturnStmt, environment: MutableEnvironment) {
    TODO("Not yet implemented")
  }

  override fun visitValDeclStmt(stmt: Stmt.ValDecl, environment: MutableEnvironment) {
    TODO("Not yet implemented")
  }

  override fun visitVarDeclStmt(stmt: Stmt.VarDecl, environment: MutableEnvironment) {
    TODO("Not yet implemented")
  }

  override fun visitStructTypedefStmt(stmt: Stmt.TypeDef.Struct, environment: MutableEnvironment) {
    TODO("Not yet implemented")
  }

  private fun makeConst(value: Boolean) = makeConst(BoolValue(value))
  private fun makeConst(value: Int) = makeConst(IntValue(value))
  private fun makeConst(value: Double) = makeConst(DoubleValue(value))

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