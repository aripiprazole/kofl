package com.lorenzoog.kofl.vm

import platform.posix.printf

typealias Value = Double

fun Value?.print() {
  if(this == null) {
    printf("null")
    return
  }
  @Suppress("UNNECESSARY_NOT_NULL_ASSERTION")
  printf("%g", this!!)
}

/**
 * This is a [Value] constant array, to be stored
 * in [Chunk]
 */
data class ValueArray(
  var capacity: Int = 0,
  var count: Int = 0,
  var values: Array<Value?> = arrayOfNulls(0)
) {

  /**
   * 1. Allocate a new array with [capacity] by [growCapacity];
   * 2. Copy the existing content in [values] to a new array and update itself;
   * 3. Store new capacity.
   */
  fun write(value: Value) {
    if (capacity < count + 1) {
      capacity = growCapacity(capacity)
      values = values.copyOf(capacity)
    }

    values[count] = value
    count++
  }

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
