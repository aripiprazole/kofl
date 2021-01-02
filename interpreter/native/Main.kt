package com.lorenzoog.kofl.interpreter

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.help
import com.github.ajalt.clikt.parameters.arguments.optional
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.help
import com.github.ajalt.clikt.parameters.options.option
import pw.binom.io.file.File
import pw.binom.io.file.read
import pw.binom.io.readText
import pw.binom.io.utf8Reader

fun main(args: Array<String>) {
  Kofl().main(args)
}

class Kofl : CliktCommand() {
  private val consoleLogger: Logger by lazy {
    ReplLogger(debug)
  }

  private val file
    by argument().help("The file that will be interpreted")
      .optional()

  private val stdlib
    by option().help("The stdlib target")
      .default(Platform.stdlibPath)

  private val debug
    by option().flag()
      .help("Enables the debug mode")

  override fun run() {
    if (file != null) {
      runFile(file!!)
    } else {
      runRepl()
    }
  }

  private fun runFile(path: String) {
    val eval = createEval(Interpreter(debug, repl = true, consoleLogger))

    try {
      eval(File(stdlib).read().utf8Reader().readText())
      eval(File(path).read().utf8Reader().readText())
    } catch (error: Throwable) {
      consoleLogger.handleError(error)
    }
  }

  private fun runRepl() {
    try {
      startRepl(consoleLogger, debug, path = stdlib)
    } catch (error: Throwable) {
      consoleLogger.handleError(error)
    }
  }
}