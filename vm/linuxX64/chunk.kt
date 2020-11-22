@file:OptIn(ExperimentalUnsignedTypes::class)

package com.lorenzoog.kofl.vm

/**
 * This is the possible instructions at execution time
 */
object OpCode {
  const val OpReturn: UByte = 0u // size: 1
  const val OpConstant: UByte = 1u // size: 2
}

/**
 * This can be used to show which opcode is unused/useless, when the
 * total is the [capacity], and the amount allocated is [count], and
 * this count the amount opcodes are stored in a [Chunk]
 */
data class Chunk(
  var count: Int = 0,
  var capacity: Int = 0,
  var code: Array<UByte?> = arrayOfNulls(0),
  var constants: ValueArray = ValueArray()
) {

  // TODO: use something like run-length encoding instead of current line encoding
  var lines = arrayOfNulls<Int>(0)

  /**
   * 1. Allocate a new array with [capacity] by [growCapacity];
   * 2. Copy the existing content in [code] to a new array and update itself;
   * 3. Store new capacity.
   */
  fun write(byte: UByte, line: Int) {
    if (capacity < count + 1) {
      capacity = growCapacity(capacity)
      code = code.copyOf(capacity)
      lines = lines.copyOf(capacity)
    }

    code[count] = byte
    lines[count] = line
    count++
  }

  fun write(n: Int, line: Int): Unit = write(n.toUByte(), line)
  fun write(n: Long, line: Int): Unit = write(n.toUByte(), line)
  fun write(n: Short, line: Int): Unit = write(n.toUByte(), line)

  fun addConstant(constant: Value): Int {
    constants.write(constant)
    return constants.count - 1
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is Chunk) return false

    if (count != other.count) return false
    if (capacity != other.capacity) return false
    if (!code.contentEquals(other.code)) return false

    return true
  }

  override fun hashCode(): Int {
    var result = count.hashCode()
    result = 31 * result + capacity.hashCode()
    result = 31 * result + code.contentHashCode()
    return result
  }
}
