@file:OptIn(ExperimentalUnsignedTypes::class)

package com.lorenzoog.kofl.vm

import kotlinx.cinterop.*
import platform.posix.sprintf

fun Chunk.disassemble(name: String) {
  println("== $name ==")

  var offset = 0
  while (offset < count) {
    offset = disassembleInstructions(offset)
  }
}

fun Chunk.disassembleInstructions(offset: Int): Int {
  val line = if (offset > 0 && lines[offset] == lines[offset - 1])
    "   | " // indicates that the above instruction is the same here.
  else memScoped {
    val str = alloc<ByteVar>()
    sprintf(str.ptr, "%04d ", lines[offset]!!)
    str.ptr.toKString()
  }

  print(memScoped {
    val str = alloc<ByteVar>()
    sprintf(str.ptr, "%04d $line", offset)
    str.ptr.toKString()
  })

  return when (val opcode = code[offset]) {
    OpCode.OpReturn -> simpleInstruction("OP_RETURN", offset)
    OpCode.OpNegate -> simpleInstruction("OP_NEGATE", offset)
    OpCode.OpMultiply -> simpleInstruction("OP_MULTIPLY", offset)
    OpCode.OpSum -> simpleInstruction("OP_SUM", offset)
    OpCode.OpSubtract -> simpleInstruction("OP_SUBTRACT", offset)
    OpCode.OpDivide -> simpleInstruction("OP_DIVIDE", offset)
    OpCode.OpConcat -> simpleInstruction("OP_CONCAT", offset)
    OpCode.OpPop -> simpleInstruction("OP_POP", offset)
    OpCode.OpStoreGlobal -> simpleInstruction("OP_STORE_GLOBAL", offset)
    OpCode.OpAccessGlobal -> simpleInstruction("OP_ACCESS_GLOBAL", offset)
    OpCode.OpConstant,
    OpCode.OpTrue,
    OpCode.OpFalse -> constantInstruction("OP_CONSTANT", offset)
    OpCode.OpNot -> simpleInstruction("OP_NOT", offset)
    else -> {
      println("unknown opcode: $opcode")
      offset + 1
    }
  }
}

fun Chunk.constantInstruction(name: String, offset: Int): Int {
  val const = code[offset + 1]!!
  val offsetStr = memScoped {
    val str = alloc<ByteVar>()
    sprintf(str.ptr, "%04d", const)
    str.ptr.toKString()
  }
  println("$name $offsetStr '${constants.values[const.toInt()]!!.print()}'")
  return offset + 2
}

private fun simpleInstruction(name: String, offset: Int): Int {
  println(name)
  return offset + 1
}