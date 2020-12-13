@file:OptIn(ExperimentalUnsignedTypes::class)

package com.lorenzoog.kofl.vm

import com.lorenzoog.kofl.vm.interop.Chunk
import com.lorenzoog.kofl.vm.interop.OpCode
import com.lorenzoog.kofl.vm.interop.Value
import kotlinx.cinterop.*
import platform.posix.sprintf

fun Value?.print(): String {
  if (this == null) return "NULL"

  return "TODO"
}

fun Chunk.disassemble(name: String) {
  println("== $name ==")

  var offset = 0
  while (offset < count) {
    offset = disassembleInstructions(name, offset)
  }
}

fun Chunk.disassembleInstructions(name: String, offset: Int): Int {
  if (lines == null) error("CHUNK $name SHOULD NOT HAVE LINES NULL")
  if (code == null) error("CHUNK $name SHOULD NOT HAVE CODE NULL")

  val line = if (offset > 0 && lines!![offset] == lines!![offset - 1])
    "   | " // indicates that the above instruction is the same here.
  else memScoped {
    val str = alloc<ByteVar>()
    sprintf(str.ptr, "%04d ", lines!![offset])
    str.ptr.toKString()
  }

  print(memScoped {
    val str = alloc<ByteVar>()
    sprintf(str.ptr, "%04d $line", offset)
    str.ptr.toKString()
  })

  val opcode = kotlin.runCatching {
    code!![offset].value
  }.getOrNull()

  return when (opcode) {
    OpCode.OP_RET -> simpleInstruction("RET", offset)
    OpCode.OP_NEGATE -> simpleInstruction("NEGATE", offset)
    OpCode.OP_MULT -> simpleInstruction("MUL", offset)
    OpCode.OP_SUM -> simpleInstruction("SUM", offset)
    OpCode.OP_SUB -> simpleInstruction("SUB", offset)
    OpCode.OP_DIV -> simpleInstruction("DIV", offset)
    OpCode.OP_CONCAT -> simpleInstruction("CONCAT", offset)
    OpCode.OP_POP -> simpleInstruction("POP", offset)
    OpCode.OP_STORE_GLOBAL -> simpleInstruction("STORE_GLOBAL", offset)
    OpCode.OP_ACCESS_GLOBAL -> simpleInstruction("ACCESS_GLOBAL", offset)
    OpCode.OP_CONST,
    OpCode.OP_TRUE,
    OpCode.OP_FALSE -> constantInstruction("CONST", offset)
    OpCode.OP_NOT -> simpleInstruction("NOT", offset)
    else -> {
      println("unknown opcode: $opcode")
      offset + 1
    }
  }
}

@OptIn(ExperimentalUnsignedTypes::class)
fun Chunk.constantInstruction(name: String, offset: Int): Int {
  val const = code!![offset + 1].value
  val offsetStr = memScoped {
    val str = alloc<ByteVar>()
    sprintf(str.ptr, "%04d", const)
    str.ptr.toKString()
  }

  println("$name $offsetStr '${values?.pointed?.values?.get(const.value.toInt()).print()}'")

  return offset + 2
}

private fun simpleInstruction(name: String, offset: Int): Int {
  println(name)

  return offset + 1
}