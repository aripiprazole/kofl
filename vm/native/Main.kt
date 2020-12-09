package com.lorenzoog.kofl.vm

import com.lorenzoog.kofl.frontend.Parser
import com.lorenzoog.kofl.frontend.Scanner
import com.lorenzoog.kofl.frontend.dump
import kotlinx.cinterop.memScoped

// TODO: use a own heap
fun main(): Unit = memScoped {
  val compiler = Compiler()
  val code = """
    val x: String = "!";
    val y: String = x;
    "";
  """.trimIndent()
  val scanner = Scanner(code)
  val parser = Parser(scanner.scan(), repl = false)
  val stmts = parser.parse().also {
    dump(it)
  }

  println("COMPILED:")
  val chunk = compiler.compile(stmts).first().apply {
    disassemble("CODE")
  }
  println("==-==-==-==")

  val vm = KVM(this)
  vm.start()
  vm.interpret(arrayOf(chunk))
}