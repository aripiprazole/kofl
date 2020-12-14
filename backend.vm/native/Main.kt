package com.lorenzoog.kofl.vm

import com.lorenzoog.kofl.compiler.common.backend.AstConverter
import com.lorenzoog.kofl.compiler.common.backend.Descriptor
import com.lorenzoog.kofl.compiler.common.typing.KoflType
import com.lorenzoog.kofl.compiler.common.typing.TypeContainer
import com.lorenzoog.kofl.frontend.Parser
import com.lorenzoog.kofl.frontend.Scanner
import com.lorenzoog.kofl.frontend.Stack
import com.lorenzoog.kofl.vm.compiler.BytecodeCompiler
import com.lorenzoog.kofl.vm.interop.Chunk
import com.lorenzoog.kofl.vm.interop.Vm
import com.lorenzoog.kofl.vm.interop.eval
import kotlinx.cinterop.StableRef
import kotlinx.cinterop.pointed

const val MAX_STACK = 250

val builtinTypeContainer = TypeContainer().apply {
  defineType("Any", KoflType.Any)
  defineType("String", KoflType.String)
  defineType("Int", KoflType.Int)
  defineType("Double", KoflType.Double)
  defineType("Boolean", KoflType.Boolean)
  defineType("Unit", KoflType.Unit)
}

private fun emitBytecode(): Chunk {
  val locals = mutableMapOf<Descriptor, Int>()

  val compiler = BytecodeCompiler()
  val lexer = Scanner(
    // 16
    """
      2 + 4 * 4 - 4 / 2;
    """.trimIndent()
  )
  val parser = Parser(lexer.scan(), repl = true)
  val converter = AstConverter(locals, Stack<TypeContainer>(MAX_STACK).also { container ->
    container.push(builtinTypeContainer.copy())
  })

  val stmts = parser.parse()
  val descriptors = converter.compile(stmts)

  println("== PARSED ==")
  println("STMTS = $stmts")
  println("DESCRIPTORS = $descriptors")
  println("== ------ ==")

  return compiler.compile(descriptors).also { chunk ->
    chunk.disassemble("CODE")
  }
}

fun main() {
  println("DEBUGGING KOFL VM")
  println()

  val vm = Vm(verbose = true, memory = 512)

  println("INTERPRET RESULT = ${vm.eval(emitBytecode())}")
}

