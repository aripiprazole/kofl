package com.lorenzoog.kofl.compiler.vm

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.help
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.help
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.int
import com.lorenzoog.kofl.compiler.common.backend.AstConverter
import com.lorenzoog.kofl.compiler.common.backend.Descriptor
import com.lorenzoog.kofl.compiler.common.typing.KoflType
import com.lorenzoog.kofl.compiler.common.typing.TypeContainer
import com.lorenzoog.kofl.frontend.Parser
import com.lorenzoog.kofl.frontend.Scanner
import com.lorenzoog.kofl.frontend.Stack
import pw.binom.ByteBuffer
import pw.binom.io.file.File
import pw.binom.io.file.write
import pw.binom.io.use
import pw.binom.writeByte

class Koflc : CliktCommand() {
  private val file by argument().help("The file that will be compiled")
  private val target by argument().help("The target that will have the compiled bytecode of file")

  private val verbose by option().flag().help("Enables the verbose mode: TODO")

  private val maxStack by option()
    .help("Max stack size on type definitions")
    .int()
    .default(512_000)

  @OptIn(ExperimentalUnsignedTypes::class)
  override fun run() {
    val file = File(file)
    val target = File(target)

    val locals = mutableMapOf<Descriptor, Int>()
    val container = TypeContainer().apply {
      defineType("Any", KoflType.Any)
      defineType("String", KoflType.String)
      defineType("Int", KoflType.Int)
      defineType("Double", KoflType.Double)
      defineType("Boolean", KoflType.Boolean)
      defineType("Unit", KoflType.Unit)
    }

    val parser = Parser(file.readContents().decodeToString(), repl = true)
    val converter = AstConverter(locals, Stack<TypeContainer>(maxStack).also { stack ->
      stack.push(container)
    })
    val compiler = Compiler(verbose, converter.compile(parser.parse()).toList())

    target.write(append = false).use { channel ->
      val buffer = compiler.compile()
      val pool = ByteBuffer.alloc(10)
      val bytecode = buffer.toByteArray()

      bytecode.forEach {
        channel.writeByte(pool, it)
      }

      channel.flush()

      if (verbose) {
        echo("BYTECODE:")
        bytecode.toList().chunked(20) { code ->
          code.forEach {
            echo(it.toUInt().toString(16), lineSeparator = " ")
          }
          echo()
        }
      }
    }

    echo("Successfully compiled bytecode from ${file.path} to file ${target.path}")
  }
}