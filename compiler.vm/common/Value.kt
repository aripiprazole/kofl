@file:OptIn(ExperimentalUnsignedTypes::class)

package me.devgabi.kofl.compiler.vm

import pw.binom.ByteBuffer
import pw.binom.writeDouble
import pw.binom.writeInt
import pw.binom.writeUTF8String

sealed class Value {
  abstract val size: Int

  abstract fun write(buffer: ByteBuffer)
}

data class StringValue(private val value: String) : Value() {
  override val size = Char.SIZE_BITS * value.length + Int.SIZE_BITS

  override fun write(buffer: ByteBuffer) {
    buffer.writeUTF8String(ByteBuffer.alloc(size), value)
  }
}

data class DoubleValue(private val value: Double) : Value() {
  override val size: Int = Double.SIZE_BITS

  override fun write(buffer: ByteBuffer) {
    buffer.writeDouble(ByteBuffer.alloc(size), value)
  }
}

data class IntValue(private val value: Int) : Value() {
  override val size: Int = Int.SIZE_BITS

  override fun write(buffer: ByteBuffer) {
    buffer.writeInt(ByteBuffer.alloc(size), value)
  }
}

data class ValueArray(
  val count: Int,
  val capacity: Int,
  val values: Array<Value>
) {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || other !is ValueArray) return false

    if (count != other.count) return false
    if (capacity != other.capacity) return false
    if (!values.contentEquals(other.values)) return false

    return true
  }

  override fun hashCode(): Int {
    var result = count
    result = 31 * result + capacity
    result = 31 * result + values.contentHashCode()
    return result
  }
}
