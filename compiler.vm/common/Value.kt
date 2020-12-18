@file:OptIn(ExperimentalUnsignedTypes::class)

package com.lorenzoog.kofl.compiler.vm

sealed class Value {
  fun toUByteArray(): UByteArray {
    return ubyteArrayOf(
      OpChunk.ValueStart,
      *render(),
      OpChunk.ValueEnd,
    )
  }

  protected abstract fun render(): UByteArray
}

class StringValue(private val value: String) : Value() {
  override fun render(): UByteArray {
    return value.encodeToByteArray().toUByteArray()
  }
}

class DoubleValue(private val value: Double) : Value() {
  override fun render(): UByteArray {
    return ubyteArrayOf(value.toInt().toUByte())
  }
}

class IntValue(private val value: Int) : Value() {
  override fun render(): UByteArray {
    return ubyteArrayOf(value.toUByte())
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
