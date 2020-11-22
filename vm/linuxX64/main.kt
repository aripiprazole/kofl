package com.lorenzoog.kofl.vm

// TODO: use a own heap
fun main() {
  val chunk = Chunk()
  chunk.write(OpCode.OpConstant, 123)
  chunk.write(chunk.addConstant(1.2), 123)
  chunk.write(OpCode.OpReturn, 123)
  chunk.disassemble("test chunk")
}