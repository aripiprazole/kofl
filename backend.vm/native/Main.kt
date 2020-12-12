package com.lorenzoog.kofl.vm

import com.lorenzoog.kofl.frontend.Parser
import com.lorenzoog.kofl.frontend.Scanner
import kotlinx.cinterop.memScoped

fun main(): Unit = memScoped {
  val bytecodeCompiler = BytecodeCompiler()

  val lexer = Scanner(
    """
      val x: String = "";
      val y: String = x;
    """.trimIndent()
  )
  val parser = Parser(lexer.scan(), repl = true)

  bytecodeCompiler.compile(parser.parse())
}

