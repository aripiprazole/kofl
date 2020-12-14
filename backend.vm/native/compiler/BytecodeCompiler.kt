@file:OptIn(ExperimentalUnsignedTypes::class)

package com.lorenzoog.kofl.vm.compiler

import com.lorenzoog.kofl.compiler.common.backend.*
import com.lorenzoog.kofl.compiler.common.typing.KoflType
import com.lorenzoog.kofl.compiler.common.typing.isAssignableBy
import com.lorenzoog.kofl.frontend.TokenType
import com.lorenzoog.kofl.vm.interop.*
import kotlinx.cinterop.CValue
import kotlinx.cinterop.MemScope
import kotlinx.cinterop.cstr
import kotlinx.cinterop.placeTo
import platform.posix.UINT8_MAX

class BytecodeCompiler : Descriptor.Visitor<Unit> {
  private val heap = MemScope()
  private val chunk = Chunk()

  fun compile(descriptors: Collection<Descriptor>): Chunk {
    visitDescriptors(descriptors)
    emit(OpCode.OP_RET, -1)

    return chunk
  }

  override fun visitConstDescriptor(descriptor: ConstDescriptor) {
    val value = descriptor.value.toString()
    val const = when (descriptor.type) {
      KoflType.String -> makeConst(value)
      KoflType.Boolean -> makeConst(value.toBoolean())
      KoflType.Double -> makeConst(value.toDouble())
      KoflType.Int -> makeConst(value.toDouble())
      else -> error("UNSUPPORTED CONST TYPE ${descriptor.type} (${descriptor.value::class})")
    }
    emit(OpCode.OP_CONST, const, descriptor.line)
  }

  override fun visitThisDescriptor(descriptor: ThisDescriptor) {
    TODO("Not yet implemented")
  }

  override fun visitSetDescriptor(descriptor: SetDescriptor) {
    TODO("Not yet implemented")
  }

  override fun visitGetDescriptor(descriptor: GetDescriptor) {
    TODO("Not yet implemented")
  }

  override fun visitCallDescriptor(descriptor: CallDescriptor) {
    TODO("Not yet implemented")
  }

  override fun visitAccessVarDescriptor(descriptor: AccessVarDescriptor) {
    emit(OpCode.OP_CONST, makeConst(descriptor.name), descriptor.line)
    emit(OpCode.OP_ACCESS_GLOBAL, descriptor.line)
  }

  override fun visitAccessFunctionDescriptor(descriptor: AccessFunctionDescriptor) {
    TODO("Not yet implemented")
  }

  override fun visitUnaryDescriptor(descriptor: UnaryDescriptor) {
    TODO("Not yet implemented")
  }

  override fun visitValDescriptor(descriptor: ValDescriptor) {
    emit(OpCode.OP_CONST, makeConst(descriptor.name), descriptor.line)
    visitDescriptor(descriptor.value)
    emit(OpCode.OP_STORE_GLOBAL, descriptor.line)
  }

  override fun visitVarDescriptor(descriptor: VarDescriptor) {
    emit(OpCode.OP_CONST, makeConst(descriptor.name), descriptor.line)
    visitDescriptor(descriptor.value)
    emit(OpCode.OP_STORE_GLOBAL, descriptor.line)
  }

  override fun visitAssignDescriptor(descriptor: AssignDescriptor) {
    TODO("Not yet implemented")
  }

  override fun visitReturnDescriptor(descriptor: ReturnDescriptor) {
    TODO("Not yet implemented")
  }

  override fun visitBlockDescriptor(descriptor: BlockDescriptor) {
    TODO("Not yet implemented")
  }

  override fun visitWhileDescriptor(descriptor: WhileDescriptor) {
    TODO("Not yet implemented")
  }

  override fun visitIfDescriptor(descriptor: IfDescriptor) {
    TODO("Not yet implemented")
  }

  override fun visitLogicalDescriptor(descriptor: LogicalDescriptor) {
    TODO("Not yet implemented")
  }

  override fun visitBinaryDescriptor(descriptor: BinaryDescriptor) {
    visitDescriptor(descriptor.left)
    visitDescriptor(descriptor.right)

    when (descriptor.op) {
      TokenType.Plus -> emit(
        op = if (KoflType.String.isAssignableBy(descriptor.left.type))
          OpCode.OP_CONCAT
        else OpCode.OP_SUM,
        line = descriptor.line
      )
      TokenType.Minus -> emit(OpCode.OP_SUB, descriptor.line)
      TokenType.Slash -> emit(OpCode.OP_DIV, descriptor.line)
      TokenType.Star -> emit(OpCode.OP_MULT, descriptor.line)
      else -> {}
    }
  }

  override fun visitLocalFunctionDescriptor(descriptor: LocalFunctionDescriptor) {
    TODO("Not yet implemented")
  }

  override fun visitNativeFunctionDescriptor(descriptor: NativeFunctionDescriptor) {
    TODO("Not yet implemented")
  }

  override fun visitFunctionDescriptor(descriptor: FunctionDescriptor) {
    TODO("Not yet implemented")
  }

  override fun visitClassDescriptor(descriptor: ClassDescriptor) {
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
    val const = chunk.addConst(value)

    if (const > UINT8_MAX) {
      error("TOO LONG CONST") // TODO: make a error
    }

    return const.toUInt()
  }

  private fun endCompiler() {
    emit(OpCode.OP_RET, -1)
  }

  private fun emit(op: OpCode, line: Int) {
    emit(op.value, line)
  }

  private fun emit(op: UInt, line: Int) {
    chunk.write(op, line)
  }

  private fun emit(op: OpCode, value: UInt, line: Int) {
    emit(op, line)
    emit(value, line)
  }
}