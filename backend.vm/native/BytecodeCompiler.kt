@file:OptIn(ExperimentalUnsignedTypes::class)

package com.lorenzoog.kofl.vm

import com.lorenzoog.kofl.frontend.Expr
import com.lorenzoog.kofl.frontend.Stmt
import com.lorenzoog.kofl.frontend.TokenType
import com.lorenzoog.kofl.vm.interop.*
import kotlinx.cinterop.*
import platform.posix.UINT8_MAX

class BytecodeCompiler : Expr.Visitor<Unit>, Stmt.Visitor<Unit> {
  private val heap = MemScope()

  // chunk index
  private var ci = 0
  private var chunks = arrayOf(Chunk())

  fun compile(stmts: List<Stmt>): Array<CPointer<Chunk>> {
    println("COMPILING $stmts")

    visitStmts(stmts)
//    endCompiler()

    return chunks
  }

  fun compile(exprs: List<Expr>): Array<CPointer<Chunk>> {
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
      TokenType.Plus -> emit(OpCode.OP_SUM, expr.line) // TODO: compile OpCode.Concat when have typechecking
      TokenType.Minus -> emit(OpCode.OP_SUB, expr.line)
      TokenType.Slash -> emit(OpCode.OP_DIV, expr.line)
      TokenType.Star -> emit(OpCode.OP_MULT, expr.line)
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
      is Int -> emit(OpCode.OP_CONST, makeConst(value), expr.line)
      is Number -> emit(OpCode.OP_CONST, makeConst(value.toDouble()), expr.line)
      is Boolean -> emit(OpCode.OP_CONST, makeConst(value), expr.line)
      is String -> emit(OpCode.OP_CONST, makeConst(value), expr.line)
    }
  }

  override fun visitUnaryExpr(expr: Expr.Unary) {
    visitExpr(expr.right)

    when (expr.op.type) {
      TokenType.Minus -> emit(OpCode.OP_NEGATE, expr.line)
      TokenType.Bang -> emit(OpCode.OP_NOT, expr.line)
      else -> {
      }
    }
  }

  override fun visitVarExpr(expr: Expr.Var) {
    emit(OpCode.OP_CONST, makeConst(expr.name.lexeme), expr.line)
    emit(OpCode.OP_ACCESS_GLOBAL, expr.line)
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
    emit(OpCode.OP_CONST, makeConst(stmt.name.lexeme), stmt.line)
    visitExpr(stmt.value)
    emit(OpCode.OP_STORE_GLOBAL, stmt.line)
  }

  override fun visitVarDeclStmt(stmt: Stmt.VarDecl) {
    TODO("Not yet implemented")
  }

  override fun visitClassTypeStmt(stmt: Stmt.Type.Class) {
    TODO("Not yet implemented")
  }

  private fun makeConst(value: Boolean) = makeConst(Value(ValueType.V_TYPE_BOOL) {
    _bool = value
  })

  private fun makeConst(value: Double) = makeConst(Value(ValueType.V_TYPE_DOUBLE) {
    _double = value
  })

  private fun makeConst(value: Int) = makeConst(Value(ValueType.V_TYPE_INT) {
    _int = value
  })

  private fun makeConst(value: String) = makeConst(Value(ValueType.V_TYPE_STR) {
    _string = value.cstr.placeTo(heap)
  })

  @OptIn(ExperimentalUnsignedTypes::class)
  private fun makeConst(value: CValue<Value>): UInt {
    val const = chunk().addConst(value)

    if (const > UINT8_MAX) {
      error("TOO LONG CONST") // TODO: make a error
    }

    return const.toUInt()
  }

  private fun chunk(): Chunk {
    return chunks[ci].pointed
  }

  private fun endCompiler() {
    emit(OpCode.OP_RET, -1)
  }

  private fun emit(op: OpCode, line: Int) {
    emit(op.value, line)
  }

  private fun emit(op: UInt, line: Int) {
    chunk().write(op, line)
  }

  private fun emit(op: OpCode, value: UInt, line: Int) {
    emit(op, line)
    emit(value, line)
  }
}