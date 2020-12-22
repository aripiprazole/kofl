@file:OptIn(ExperimentalUnsignedTypes::class)

package com.lorenzoog.kofl.compiler.vm

import pw.binom.ByteBuffer
import pw.binom.writeInt

data class Chunk(
  val count: Int,
  val capacity: Int,
  val lines: IntArray,
  val code: UIntArray,
  val consts: ValueArray
) {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || other !is Chunk) return false

    if (count != other.count) return false
    if (capacity != other.capacity) return false
    if (!lines.contentEquals(other.lines)) return false
    if (code != other.code) return false
    if (consts != other.consts) return false

    return true
  }

  override fun hashCode(): Int {
    var result = count
    result = 31 * result + capacity
    result = 31 * result + lines.contentHashCode()
    result = 31 * result + code.hashCode()
    result = 31 * result + consts.hashCode()
    return result
  }
}

@OptIn(ExperimentalUnsignedTypes::class)
enum class ChunkOp(val end: ChunkOp? = null) {
  ChunkEnd,
  Chunk(ChunkEnd),

  InfoEnd,
  Info(InfoEnd),

  CodeEnd,
  Code(CodeEnd),

  ValueEnd,
  Value(ValueEnd),

  LinesEnd,
  Lines(LinesEnd),

  ConstsEnd,
  Consts(ConstsEnd),
}

fun ByteBuffer.writeChunkOp(buffer: ByteBuffer, chunk: ChunkOp) {
  writeInt(buffer, chunk.ordinal)
}

fun ByteBuffer.writeChunkInfo(op: ChunkOp, block: () -> Unit) {
  writeChunkOp(ByteBuffer.alloc(Int.SIZE_BITS), op)
  block()
  op.end?.let { writeChunkOp(ByteBuffer.alloc(Int.SIZE_BITS), op) }
}

enum class OpCode {
  Ret,
  Const,
  Negate,
  Sum,
  Sub,
  Mult,
  Div,
  True,
  False,
  Not,
  Concat,
  Pop,
  SGlobal,
  AGlobal;
}