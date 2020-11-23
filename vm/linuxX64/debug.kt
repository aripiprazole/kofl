@file:OptIn(ExperimentalUnsignedTypes::class)

package com.lorenzoog.kofl.vm

import platform.posix.printf

fun Chunk.disassemble(name: String) {
  printf("== $name ==\n")

  var offset = 0
  while (offset < count) {
    offset = disassembleInstructions(offset)
  }
}

fun Chunk.disassembleInstructions(offset: Int): Int {
  printf("%04d ", offset)

  if (offset > 0 && lines[offset] == lines[offset - 1]) {
    printf("   | ") // indicates that the above instruction is the same here.
  } else {
    printf("%04d ", lines[offset]!!)
  }

  return when (val opcode = code[offset]) {
    OpCode.OpReturn -> simpleInstruction("OP_RETURN", offset)
    OpCode.OpNegate -> simpleInstruction("OP_NEGATE", offset)
    OpCode.OpMultiply -> simpleInstruction("OP_MULTIPLY", offset)
    OpCode.OpSum -> simpleInstruction("OP_SUM", offset)
    OpCode.OpSubtract -> simpleInstruction("OP_SUBTRACT", offset)
    OpCode.OpDivide -> simpleInstruction("OP_DIVIDE", offset)
    OpCode.OpConstant -> constantInstruction("OP_CONSTANT", offset)
    else -> {
      printf("unknown opcode: $opcode\n")
      offset + 1
    }
  }
}

fun Chunk.constantInstruction(name: String, offset: Int): Int {
  val const = code[offset + 1]!!
  printf("%-16s %4d '", name, const)
  constants.values[const.toInt()]!!.print()
  printf("'\n")
  return offset + 2
}

private fun simpleInstruction(name: String, offset: Int): Int {
  printf("$name\n")
  return offset + 1
}