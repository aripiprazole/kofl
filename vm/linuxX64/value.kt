package com.lorenzoog.kofl.vm

fun <T> Value<T>?.print(): String {
  if (this == null) {
    return "null"
  }

  return toString()
}

interface Value<T> {
  val value: T
}

inline class StrValue(override val value: String) : Value<String> {
  override fun toString(): String = value
}

inline class DoubleValue(override val value: Double) : Value<Double> {
  override fun toString(): String = value.toString()
}

inline class IntValue(override val value: Int) : Value<Int> {
  override fun toString(): String = value.toString()
}

inline class BoolValue(override val value: Boolean) : Value<Boolean> {
  override fun toString(): String = value.toString()
}

/**
 * This is a [Value] constant array, to be stored
 * in [Chunk]
 */
data class ValueArray(
  var capacity: Int = 0,
  var count: Int = 0,
  var values: Array<Value<*>?> = arrayOfNulls(0)
) {
  @Suppress("UNCHECKED_CAST")
  operator fun <T> get(index: Int): Value<T> =
    values[index] as? Value<T>? ?: throw ArrayIndexOutOfBoundsException("value array ($this) $index out of bounds")

  /**
   * 1. Allocate a new array with [capacity] by [growCapacity];
   * 2. Copy the existing content in [values] to a new array and update itself;
   * 3. Store new capacity.
   */
  fun <T> write(value: Value<T>) {
    if (capacity < count + 1) {
      capacity = growCapacity(capacity)
      values = values.copyOf(capacity)
    }

    values[count] = value
    count++
  }

  fun write(value: Double): Unit = write(DoubleValue(value))
  fun write(value: Int): Unit = write(IntValue(value))
  fun write(value: Boolean): Unit = write(BoolValue(value))

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is ValueArray) return false

    if (capacity != other.capacity) return false
    if (count != other.count) return false
    if (!values.contentEquals(other.values)) return false

    return true
  }

  override fun hashCode(): Int {
    var result = capacity.hashCode()
    result = 31 * result + count.hashCode()
    result = 31 * result + values.contentHashCode()
    return result
  }
}
