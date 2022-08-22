package me.devgabi.kofl.compiler.vm

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.help
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.help
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.int
import me.devgabi.kofl.compiler.common.backend.Descriptor
import me.devgabi.kofl.compiler.common.backend.TreeDescriptorMapper
import me.devgabi.kofl.compiler.common.typing.KfType
import me.devgabi.kofl.compiler.common.typing.TypeScope
import me.devgabi.kofl.frontend.Parser
import me.devgabi.kofl.frontend.Stack
import pw.binom.ByteBuffer
import pw.binom.io.file.File
import pw.binom.io.file.write
import pw.binom.io.use
import pw.binom.writeByte
import kotlin.contracts.ExperimentalContracts

class Koflc : CliktCommand() {
  private val file by argument().help("The file that will be compiled")
  private val target by argument().help("The target that will have the compiled bytecode of file")

  private val verbose by option().flag().help("Enables the verbose mode: TODO")

  private val maxStack by option()
    .help("Max stack size on type definitions")
    .int()
    .default(512_000)

  @ExperimentalUnsignedTypes
  @ExperimentalContracts
  override fun run() {
    val file = File(file)
    val target = File(target)

    val locals = mutableMapOf<Descriptor, Int>()
    val container = TypeScope().apply {
      defineType("Any", KfType.Any)
      defineType("String", KfType.String)
      defineType("Int", KfType.Int)
      defineType("Double", KfType.Double)
      defineType("Boolean", KfType.Boolean)
      defineType("Unit", KfType.Unit)
    }

    val parser = Parser(file.readContents().decodeToString(), repl = true)
    val converter = TreeDescriptorMapper(
      locals,
      Stack<TypeScope>(maxStack).also { stack ->
        stack.push(container)
      }
    )
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
