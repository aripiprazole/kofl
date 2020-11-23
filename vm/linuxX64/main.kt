package com.lorenzoog.kofl.vm

import com.lorenzoog.kofl.interpreter.Interpreter
import com.lorenzoog.kofl.interpreter.MutableEnvironment
import com.lorenzoog.kofl.interpreter.NativeEnvironment
import kotlinx.cinterop.memScoped

@ThreadLocal
private val globalEnvironment = MutableEnvironment(NativeEnvironment())

// TODO: use a own heap
fun main(): Unit = memScoped {
  val compiler = Compiler()
  val code = """
    val x = "!";
    val y = x;
    "";
  """.trimIndent()
  val stmts = Interpreter.parse(code)
  println("AST: $stmts")

  println("COMPILED:")
  val chunk = compiler.compile(stmts, globalEnvironment).first()

  chunk.disassemble("CODE")
  println("==-==-==-==")

  val vm = KVM(this)
  vm.start()
  vm.interpret(arrayOf(chunk))
}