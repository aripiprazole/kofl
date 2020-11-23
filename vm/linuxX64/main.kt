package com.lorenzoog.kofl.vm

import com.lorenzoog.kofl.interpreter.*
import kotlinx.cinterop.memScoped

@ThreadLocal
private val globalEnvironment = MutableEnvironment(NativeEnvironment())

// TODO: use a own heap
fun main(): Unit = memScoped {
  val compiler = Compiler()
  val expr = Expr.Binary(
    left = Expr.Literal(10, 1),
    op = Token(TokenType.Plus, "+", null, 1),
    right = Expr.Literal(20, 1),
    line = 1
  )

  println("COMPILED:")
  val chunk = compiler.compile(listOf(expr), globalEnvironment).first()

  chunk.disassemble("CODE")
  println("==    ==")

  val vm = KVM(this)
  vm.start()
//
//  val chunk = Chunk()
//  chunk.write(OpCode.OpConstant, 123)
//  chunk.write(chunk.addConstant(1.2), 123)
//
//  chunk.write(OpCode.OpConstant, 123)
//  chunk.write(chunk.addConstant(13.2), 123)
//
//  chunk.write(OpCode.OpSum, 123)
//  chunk.write(OpCode.OpReturn, 123)
//
  vm.interpret(arrayOf(chunk))
}