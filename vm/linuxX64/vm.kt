@file:OptIn(ExperimentalUnsignedTypes::class)

package com.lorenzoog.kofl.vm

import kotlinx.cinterop.NativePlacement
import platform.posix.printf

class StackOutOfBoundsError : RuntimeException()

const val STACK_MAX = 256

enum class InterpreterResult {
  Ok, CompileError, RuntimeError
}

class KVM(private val heap: NativePlacement) {
  /**
   * debugging means the program will print the chunks
   */
  var debugging = true

  /** callstack */
  var stack = arrayOfNulls<Value>(STACK_MAX)

  /** stack index */
  var stacki = 0

  /** stack top */
  var stackt = 0

  lateinit var chunks: Array<Chunk>

  /**
   * IP means Instruction Pointer, can be called also
   * as PC, Program Counter
   */
  lateinit var ip: Array<UByte>

  /** ipi means the index in [ip] */
  var ipi = 0

  /** ipi means the index in chunk.code in [chunks] */
  var ci = 0

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
        printf("          ")

        for(stackli in (stacki..stackt)) {
          printf("[ ")
          stack[stackli].print()
          printf(" ]")
        }
        printf("\n")

        chunks[ci].disassembleInstructions(ipi - ci)
      }

      when (val instruction = ip[ipi]) {
        OpCode.OpReturn -> {
          printf("RETURN: ")
          pop().print()
          printf("\n")
          return InterpreterResult.Ok
        }
        OpCode.OpConstant -> {
          val const = chunks[ci].constants.values[ipi++]
          push(const!!)
          printf("PUSH: ")
          const.print()
          printf("\n")
        }
      }

      ipi++
    }
  }

  fun start() {
    resetStack()
  }

  fun stop() {

  }

  fun push(value: Value) {
    stack[stackt++] = value
  }

  fun pop(): Value = try {
    stackt -= 1
    stack[stackt] ?: throw StackOutOfBoundsError()
  } catch (ignored: ArrayIndexOutOfBoundsException) {
    throw StackOutOfBoundsError()
  }

  fun resetStack() {
    stackt = stacki
  }
}