package com.lorenzoog.kofl.vm

import com.lorenzoog.kofl.frontend.Parser
import com.lorenzoog.kofl.frontend.Scanner
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.pointed

fun main(): Unit = memScoped {
  val compiler = BytecodeCompiler()

  val lexer = Scanner(
    """
      val x: String = "";
      val y: String = x;
    """.trimIndent()
  )
  val parser = Parser(lexer.scan(), repl = true)

  println("== COMPILED ==")
  compiler.compile(parser.parse()).forEachIndexed { index, chunk ->
    chunk.pointed.disassemble("CHUNK[$index]")
  }
  println("== -------- ==")
}

