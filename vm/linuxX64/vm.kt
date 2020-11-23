@file:OptIn(ExperimentalUnsignedTypes::class)

package com.lorenzoog.kofl.vm

import kotlinx.cinterop.NativePlacement

class StackOutOfBoundsError : RuntimeException()
class MissingValueInStackError(val index: Int) : RuntimeException()

const val STACK_MAX = 256

enum class InterpreterResult {
  Ok, CompileError, RuntimeError
}

class KVM(private val heap: NativePlacement) {
  /**
   * debugging means the program will print the chunks
   */
  private var debugging = true

  /** callstack */
  private var stack = arrayOfNulls<Value<*>>(STACK_MAX)

  /** stack index */
  private var stacki = 0

  /** stack top */
  private var stackt = 0

  private lateinit var chunks: Array<Chunk>

  /**
   * IP means Instruction Pointer, can be called also
   * as PC, Program Counter
   */
  private lateinit var ip: Array<UByte>

  /** ipi means the index in [ip] */
  private var ipi = 0

  /** ipi means the index in chunk.code in [chunks] */
  private var ci = 0

  fun interpret(chunks: Array<Chunk?>): InterpreterResult {
    this.chunks = chunks.filterNotNull().toTypedArray()
    this.ip = chunks.filterNotNull().flatMap { it.code.toList() }
      .filterNotNull().toTypedArray()

    return run()
  }

  @OptIn(ExperimentalUnsignedTypes::class)
  fun run(): InterpreterResult {
    while (true) {
      if (debugging) {
        print("          ")

        // stackli = stack local index
        for (stackli in (stacki..stackt)) {
          print("[ ${stack[stackli].print()} ]")
        }

        println()

        chunks[ci].disassembleInstructions(ipi - ci)
      }

      when (ip[ipi]) {
        OpCode.OpReturn -> return InterpreterResult.Ok.also {
          println("RETURN: ${popAny()}")
        }
        OpCode.OpNot -> push(!pop<Boolean>())
        OpCode.OpTrue -> push(BoolValue(true))
        OpCode.OpFalse -> push(BoolValue(false))
        OpCode.OpNegate -> push(-popNumber())
        OpCode.OpDivide -> push(popNumber() / popNumber())
        OpCode.OpMultiply -> push(popNumber() * popNumber())
        OpCode.OpSum -> push(popNumber() + popNumber())
        OpCode.OpSubtract -> push(popNumber() - popNumber())
        OpCode.OpConstant -> {
          ipi += 1
          push(chunks[ci].constants.values[ip[ipi].toInt()]!!.also {
            println("PUSH: ${it.print()}")
          })
        }
      }

      ipi++
    }
  }

  fun start() {
    resetStack()
  }

  private fun popNumber(): Double {
    return popOrNull<Double>() ?: pop<Int>().toDouble()
  }

  private fun push(value: Double): Unit = push(DoubleValue(value))
  private fun push(value: Int): Unit = push(IntValue(value))
  private fun push(value: Boolean): Unit = push(BoolValue(value))

  private fun <T> push(value: Value<T>) {
    stack[stackt++] = value
  }

  private fun popAny(): Value<out Any> = pop()

  @Suppress("UNCHECKED_CAST")
  private fun <T> popOrNull(): T? = try {
    stackt -= 1
    (stack[stackt] as? Value<T>?)?.value
  } catch (ignored: ArrayIndexOutOfBoundsException) {
    null
  }

  @Suppress("UNCHECKED_CAST")
  private fun <T> pop(): T = try {
    stackt -= 1
    (stack[stackt] as? Value<T>?)?.value ?: throw StackOutOfBoundsError()
  } catch (ignored: ArrayIndexOutOfBoundsException) {
    throw StackOutOfBoundsError()
  }

  private fun resetStack() {
    stackt = stacki
  }
}