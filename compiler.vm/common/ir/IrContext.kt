package me.devgabi.kofl.compiler.vm.ir

import me.devgabi.kofl.compiler.vm.Chunk
import me.devgabi.kofl.compiler.vm.DoubleValue
import me.devgabi.kofl.compiler.vm.IntValue
import me.devgabi.kofl.compiler.vm.OpCode
import me.devgabi.kofl.compiler.vm.StringValue
import me.devgabi.kofl.compiler.vm.Value
import me.devgabi.kofl.compiler.vm.ValueArray

@ExperimentalUnsignedTypes
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

@ExperimentalUnsignedTypes
fun IrContext.makeConst(int: Int): UByte {
  return makeConst(IntValue(int))
}

@ExperimentalUnsignedTypes
fun IrContext.makeConst(double: Double): UByte {
  return makeConst(DoubleValue(double))
}

@ExperimentalUnsignedTypes
fun IrContext.makeConst(string: String): UByte {
  return makeConst(StringValue(string))
}

@ExperimentalUnsignedTypes
fun IrContext.write(op: OpCode, line: Int) {
  write(op.ordinal.toUByte(), line)
}

@ExperimentalUnsignedTypes
fun IrContext.write(op: OpCode, const: UByte, line: Int) {
  write(op, line)
  write(const, line)
}
