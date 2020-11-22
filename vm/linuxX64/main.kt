package com.lorenzoog.kofl.vm

import kotlinx.cinterop.alloc
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.memScoped

// TODO: use a own heap
fun main(): Unit = memScoped {
  val vm = KVM(this)
  vm.start()

  val chunk = Chunk()
  chunk.write(OpCode.OpConstant, 123)
  chunk.write(chunk.addConstant(1.2), 123)
  chunk.write(OpCode.OpReturn, 123)
//  chunk.disassemble("test chunk")

  vm.interpret(arrayOf(chunk))
}