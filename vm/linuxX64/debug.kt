@file:OptIn(ExperimentalUnsignedTypes::class)

package com.lorenzoog.kofl.vm

import kotlinx.cinterop.*
import platform.posix.printf
import platform.posix.sprintf

fun Chunk.disassemble(name: String) {
  printf("== $name ==\n")

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
    OpCode.OpConstant -> constantInstruction("OP_CONSTANT", offset)
    else -> {
      println("unknown opcode: $opcode")
      offset + 1
    }
  }
}

fun Chunk.constantInstruction(name: String, offset: Int): Int {
  val const = code[offset + 1]!!
  println(memScoped {
    val str = alloc<ByteVar>()
    sprintf(str.ptr, "%-16s %4d '%s'", name, constants.values[const.toInt()]!!.print(), const)
    str.ptr.toKString()
  })
  return offset + 2
}

private fun simpleInstruction(name: String, offset: Int): Int {
  println(name)
  return offset + 1
}