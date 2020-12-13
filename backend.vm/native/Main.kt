package com.lorenzoog.kofl.vm

import com.lorenzoog.kofl.frontend.Parser
import com.lorenzoog.kofl.frontend.Scanner
import com.lorenzoog.kofl.interpreter.internal.chunk_dump
import com.lorenzoog.kofl.vm.interop.OpCode
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.pointed
import kotlinx.cinterop.toKString

fun main(): Unit = memScoped {
  val compiler = BytecodeCompiler()

  val lexer = Scanner(
    """
      val x: String = "";
      val y: String = x;
      "";
    """.trimIndent()
  )
  val parser = Parser(lexer.scan(), repl = true)
  val stmts = parser.parse()

  println("== PARSED ==")
  println(stmts)
  println("== ------ ==")

  println("== COMPILED ==")
  compiler.compile(stmts).forEachIndexed { i, chunk ->
    println(chunk.pointed.disassemble("CODE $i"))
  }
  println("== -------- ==")
}

