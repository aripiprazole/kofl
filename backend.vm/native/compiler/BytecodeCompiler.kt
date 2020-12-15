@file:OptIn(ExperimentalUnsignedTypes::class)

package com.lorenzoog.kofl.vm.compiler

import com.lorenzoog.kofl.compiler.common.backend.*
import com.lorenzoog.kofl.compiler.common.typing.KoflType
import com.lorenzoog.kofl.compiler.common.typing.isAssignableBy
import com.lorenzoog.kofl.frontend.TokenType
import com.lorenzoog.kofl.interpreter.internal.object_t
import com.lorenzoog.kofl.interpreter.internal.string
import com.lorenzoog.kofl.vm.interop.*
import kotlinx.cinterop.*
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

    if (descriptor.type == KoflType.Boolean) {
      val opcode = if (value.toBoolean())
        OpCode.OP_TRUE
      else
        OpCode.OP_FALSE

      return emit(opcode, descriptor.line)
    }

    val const = when (descriptor.type) {
      KoflType.String -> makeConst(value)
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
    visitDescriptor(descriptor.right)

    when (descriptor.op) {
      TokenType.Minus -> emit(OpCode.OP_NEGATE, descriptor.line)
      TokenType.Bang -> emit(OpCode.OP_NOT, descriptor.line)
      else -> error("UNSUPPORTED UNARY OP ${descriptor.op}")
    }
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
    val (left, op, right) = descriptor

    visitDescriptor(left)

    if (KoflType.String.isAssignableBy(descriptor.left.type)) {
      if(right is MutableDescriptor) {
        visitDescriptor(right.mutate(type = KoflType.String))
      } else {
        TODO("emit toString call if isn't mutable descriptor")
      }

      return emit(OpCode.OP_CONCAT, descriptor.line)
    } else {
      visitDescriptor(descriptor.right)
    }

    when (op) {
      TokenType.Plus -> emit(OpCode.OP_CONCAT, descriptor.line)
      TokenType.Minus -> emit(OpCode.OP_SUB, descriptor.line)
      TokenType.Slash -> emit(OpCode.OP_DIV, descriptor.line)
      TokenType.Star -> emit(OpCode.OP_MULT, descriptor.line)
      else -> error("UNSUPPORTED BINARY OP ${descriptor.op}")
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
    _obj = heap.alloc<string> {
      length = value.length.toULong()
      values = value.cstr.placeTo(heap)
    }.reinterpret<object_t>().ptr
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