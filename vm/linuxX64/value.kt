package com.lorenzoog.kofl.vm

import platform.posix.printf

typealias Value = Double

fun Value.print() {
  printf("%g", this)
}

/**
 * This is a [Value] constant array, to be stored
 * in [Chunk]
 */
class ValueArray {
  var capacity = 0
  var count = 0
  var values = arrayOfNulls<Value>(0)

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
}