@file:OptIn(ExperimentalUnsignedTypes::class)

package com.lorenzoog.kofl.vm

import kotlinx.cinterop.NativePlacement
import platform.posix.printf

enum class InterpreterResult {
  Ok, CompileError, RuntimeError
}

class KVM(private val heap: NativePlacement) {
  /**
   * debugging means the program will print the chunks
   */
  var debugging = true

  lateinit var chunks: Array<Chunk>

  /**
   * IP means Instruction Pointer, can be called also
   * as PC, Program Counter
   */
  lateinit var ip: Array<UByte>

  /**
   * ipi means the index in [ip]
   */
  var ipi = 0

  /**
   * ipi means the index in chunk.code in [chunks]
   */
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
      ipi++
      if (debugging) {
        chunks[ci].disassembleInstructions(ipi - ci)
      }

      when (val instruction = ip[ipi]) {
        OpCode.OpReturn -> return InterpreterResult.Ok
        OpCode.OpConstant -> {
          val const = chunks[ci].constants.values[ipi++]
          const!!.print()
          printf("\n")
          break
        }
      }
    }

    return InterpreterResult.Ok
  }

  fun start() {

  }

  fun stop() {

  }
}