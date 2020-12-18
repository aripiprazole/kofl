@file:OptIn(ExperimentalUnsignedTypes::class)

package com.lorenzoog.kofl.compiler.vm

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
object OpChunk {
  const val ChunkStart: UByte = 0u
  const val ChunkEnd: UByte = 1u

  const val InfoStart: UByte = 2u
  const val InfoEnd: UByte = 3u

  const val CodeStart: UByte = 4u
  const val CodeEnd: UByte = 5u

  const val ValueStart: UByte = 6u
  const val ValueEnd: UByte = 7u

  const val LinesStart: UByte = 8u
  const val LinesEnd: UByte = 9u

  const val ConstsStart: UByte = 10u
  const val ConstsEnd: UByte = 11u
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