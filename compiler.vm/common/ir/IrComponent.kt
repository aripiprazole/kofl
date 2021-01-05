package com.lorenzoog.kofl.compiler.vm.ir

import com.lorenzoog.kofl.compiler.common.typing.KfType
import com.lorenzoog.kofl.compiler.vm.OpCode
import com.lorenzoog.kofl.frontend.TokenType

@ExperimentalUnsignedTypes
sealed class IrComponent {
  abstract fun render(context: IrContext)
}

@ExperimentalUnsignedTypes
class IrAccessVar(
  private val name: String,
  private val line: Int
) : IrComponent() {
  override fun render(context: IrContext) {
    context.write(OpCode.Const, context.makeConst(name), line)
    context.write(OpCode.AGlobal, line)
  }
}

@ExperimentalUnsignedTypes
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

@ExperimentalUnsignedTypes
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

@ExperimentalUnsignedTypes
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

@ExperimentalUnsignedTypes
class IrConst(
  private val value: Any,
  private val type: KfType,
  private val line: Int
) : IrComponent() {
  override fun render(context: IrContext) {
    val const = when (type) {
      KfType.Boolean -> return context.write(if (value == true) OpCode.True else OpCode.False, line)
      KfType.String -> context.makeConst(value.toString())
      KfType.Int -> context.makeConst(value.toString().toInt())
      KfType.Double -> context.makeConst(value.toString().toDouble())
      else -> TODO("Unsupported type $type")
    }

    context.write(OpCode.Const, const, line)
  }
}
