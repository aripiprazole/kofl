@file:OptIn(ExperimentalUnsignedTypes::class)

package com.lorenzoog.kofl.vm

/**
 * This is the possible instructions at execution time
 */
@Suppress("EXPERIMENTAL_UNSIGNED_LITERALS")
object OpCode {
  const val OpReturn: UByte = 0u // size: 1
  const val OpConstant: UByte = 1u // size: 2
  const val OpNegate: UByte = 2u // size: 1
  const val OpSum: UByte = 3u // size: 1
  const val OpSubtract: UByte = 4u // size: 1
  const val OpMultiply: UByte = 5u // size: 1
  const val OpDivide: UByte = 6u // size: 1
  const val OpTrue: UByte = 7u // size: 1
  const val OpFalse: UByte = 8u // size: 1
  const val OpNot: UByte = 9u // size: 1
  const val OpConcat: UByte = 10u // size: 1
  const val OpPop: UByte = 11u // size: 1
  const val OpStoreGlobal: UByte = 12u // size: 1
  const val OpAccessGlobal: UByte = 13u // size: 1
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

  fun addConstant(constant: Double) = addConstant(DoubleValue(constant))
  fun addConstant(constant: Int) = addConstant(IntValue(constant))
  fun addConstant(constant: Boolean) = addConstant(BoolValue(constant))

  fun <T> addConstant(constant: Value<T>): Int {
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
