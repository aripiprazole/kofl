package com.lorenzoog.kofl.vm

import com.lorenzoog.kofl.compiler.common.backend.AstConverter
import com.lorenzoog.kofl.compiler.common.backend.Descriptor
import com.lorenzoog.kofl.compiler.common.typing.KoflType
import com.lorenzoog.kofl.compiler.common.typing.TypeContainer
import com.lorenzoog.kofl.frontend.Parser
import com.lorenzoog.kofl.frontend.Scanner
import com.lorenzoog.kofl.frontend.Stack
import com.lorenzoog.kofl.vm.compiler.BytecodeCompiler
import kotlinx.cinterop.memScoped

const val MAX_STACK = 250

val builtinTypeContainer = TypeContainer().apply {
  defineType("Any", KoflType.Any)
  defineType("String", KoflType.String)
  defineType("Int", KoflType.Int)
  defineType("Double", KoflType.Double)
  defineType("Boolean", KoflType.Boolean)
  defineType("Unit", KoflType.Unit)
}

fun main(): Unit = memScoped {
  val locals = mutableMapOf<Descriptor, Int>()

  val compiler = BytecodeCompiler()
  val lexer = Scanner(
    """
      val x: String = "";
      val y: String = x;
      "";
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

  println("== COMPILED ==")
  compiler.compile(descriptors).disassemble("CODE")
  println("== -------- ==")
}

