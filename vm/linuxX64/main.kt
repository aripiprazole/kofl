package com.lorenzoog.kofl.vm

import com.lorenzoog.kofl.interpreter.Interpreter
import com.lorenzoog.kofl.interpreter.MutableEnvironment
import com.lorenzoog.kofl.interpreter.NativeEnvironment
import com.lorenzoog.kofl.interpreter.Stmt
import kotlinx.cinterop.memScoped

@ThreadLocal
private val globalEnvironment = MutableEnvironment(NativeEnvironment())

// TODO: use a own heap
fun main(): Unit = memScoped {
  val compiler = Compiler()
  val code = "!true;"
  val (expr, _) = Interpreter.parse(code).first() as? Stmt.ExprStmt ?: error("$code must be Stmt.ExprStmt")

  println("EXPR: $expr")
  println("COMPILED:")
  val chunk = compiler.compile(listOf(expr), globalEnvironment).first()

  chunk.disassemble("CODE")
  println("==    ==")

  val vm = KVM(this)
  vm.start()
  vm.interpret(arrayOf(chunk))
}