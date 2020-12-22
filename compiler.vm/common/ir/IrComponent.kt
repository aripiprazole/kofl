@file:OptIn(ExperimentalUnsignedTypes::class)

package com.lorenzoog.kofl.compiler.vm.ir

import com.lorenzoog.kofl.compiler.common.typing.KoflType
import com.lorenzoog.kofl.compiler.vm.OpCode
import com.lorenzoog.kofl.frontend.TokenType

sealed class IrComponent {
  abstract fun render(context: IrContext)
}

class IrAccessVar(
  private val name: String,
  private val line: Int
) : IrComponent() {
  override fun render(context: IrContext) {
    context.write(OpCode.Const, context.makeConst(name), line)
    context.write(OpCode.AGlobal, line)
  }
}

class IrVar(
  private val name: String,
  private val value: IrComponent,
  private val line: Int
) : IrComponent() {
  override fun render(context: IrContext) {
    context.write(OpCode.Const, context.makeConst(name), line)
    value.render(context)
    context.write(OpCode.SGlobal, line)
  }
}

class IrVal(
  private val name: String,
  private val value: IrComponent,
  private val line: Int
) : IrComponent() {
  override fun render(context: IrContext) {
    context.write(OpCode.Const, context.makeConst(name), line)
    value.render(context)
    context.write(OpCode.SGlobal, line)
  }
}

class IrBinary(
  private val left: IrComponent,
  private val right: IrComponent,
  private val op: TokenType,
  private val line: Int,
) : IrComponent() {
  override fun render(context: IrContext) {
    val op = when (op) {
      TokenType.Plus -> OpCode.Sum
      TokenType.Minus -> OpCode.Sub
      TokenType.Slash -> OpCode.Div
      TokenType.Star -> OpCode.Mult
      else -> TODO("Unsupported binary op $op")
    }

    right.render(context)
    left.render(context)

    context.write(op, line)
  }
}

class IrConst(
  private val value: Any,
  private val type: KoflType,
  private val line: Int
) : IrComponent() {
  override fun render(context: IrContext) {
    val const = when (type) {
      KoflType.Boolean -> return context.write(if (value == true) OpCode.True else OpCode.False, line)
      KoflType.String -> context.makeConst(value.toString())
      KoflType.Int -> context.makeConst(value.toString().toInt())
      KoflType.Double -> context.makeConst(value.toString().toDouble())
      else -> TODO("Unsupported type $type")
    }

    context.write(OpCode.Const, const, line)
  }
}