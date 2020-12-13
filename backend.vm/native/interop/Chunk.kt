package com.lorenzoog.kofl.vm.interop

import com.lorenzoog.kofl.interpreter.internal.*
import kotlinx.cinterop.*

typealias OpCode = opcode
typealias Chunk = chunk

fun Chunk(): CPointer<Chunk> {
  return chunk_create(0, 0)
    ?: error("chunk_create(count = 0, capacity 0): returned null reference")
}

@OptIn(ExperimentalUnsignedTypes::class)
fun Chunk.write(op: UInt, line: Int) {
  chunk_write(ptr, op, line)
}

inline fun Chunk.addConst(value: CValue<Value>) = chunk_write_const(ptr, value)

@OptIn(ExperimentalUnsignedTypes::class)
fun UInt.toOpcode(): OpCode = uint_to_opcode(this)
