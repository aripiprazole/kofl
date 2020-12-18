@file:OptIn(ExperimentalUnsignedTypes::class)

package com.lorenzoog.kofl.compiler.vm.ir

import com.lorenzoog.kofl.compiler.vm.*

class IrContext {
  private val code = mutableListOf<UByte>()
  private val lines = mutableListOf<Int>()
  private val consts = mutableListOf<Value>()

  fun write(byte: UByte, line: Int) {
    code += byte
    lines += line
  }

  fun makeConst(value: Value): UByte {
    consts += value

    return consts.size.toUByte()
  }

  fun toChunk(): Chunk {
    return Chunk(
      count = code.size,
      capacity = code.size + 8,
      lines = lines.toIntArray(),
      code = code.map { it.toUInt() }.toUIntArray(),
      consts = ValueArray(
        count = consts.size,
        capacity = consts.size + 8,
        values = consts.toTypedArray()
      )
    )
  }
}

fun IrContext.makeConst(int: Int): UByte {
  return makeConst(IntValue(int))
}

fun IrContext.makeConst(double: Double): UByte {
  return makeConst(DoubleValue(double))
}

fun IrContext.makeConst(string: String): UByte {
  return makeConst(StringValue(string))
}

fun IrContext.write(op: OpCode, line: Int) {
  write(op.ordinal.toUByte(), line)
}

fun IrContext.write(op: OpCode, const: UByte, line: Int) {
  write(op.ordinal.toUByte(), line)
  write(const, line)
}
