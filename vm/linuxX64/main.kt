package com.lorenzoog.kofl.vm

import com.lorenzoog.kofl.interpreter.Interpreter
import com.lorenzoog.kofl.interpreter.MutableEnvironment
import com.lorenzoog.kofl.interpreter.NativeEnvironment
import kotlinx.cinterop.memScoped

@ThreadLocal
private val globalEnvironment = MutableEnvironment(NativeEnvironment())

@ThreadLocal
val interpreter = Interpreter()

// TODO: use a own heap
fun main(): Unit = memScoped {
  val compiler = Compiler()
  val code = """
    val x: String = "!";
    val y: String = x;
    "";
  """.trimIndent()
  val stmts = interpreter.parse(code, repl = false)
  println("AST: $stmts")

  println("COMPILED:")
  val chunk = compiler.compile(stmts).first()

  chunk.disassemble("CODE")
  println("==-==-==-==")

  val vm = KVM(this)
  vm.start()
  vm.interpret(arrayOf(chunk))
}