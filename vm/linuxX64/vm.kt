@file:OptIn(ExperimentalUnsignedTypes::class)

package com.lorenzoog.kofl.vm

import kotlinx.cinterop.NativePlacement

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
          println("RETURN: ${pop()}")
        }
        OpCode.OpNegate -> push(-pop())
        OpCode.OpDivide -> push(pop() / pop())
        OpCode.OpMultiply -> push(pop() * pop())
        OpCode.OpSum -> push(pop() + pop())
        OpCode.OpSubtract -> push(pop() - pop())
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