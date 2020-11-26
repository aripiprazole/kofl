package com.lorenzoog.kofl.interpreter

import com.lorenzoog.kofl.frontend.ParseException
import com.lorenzoog.kofl.frontend.SyntaxException
import platform.posix.exit
import platform.posix.fopen

fun main(args: Array<String>) = try {
  when {
    args.size > 1 -> {
      printerr("Usage: kofl [script]")
      exit(64)
    }
    args.size == 1 -> file(args[0])
    else -> repl()
  }
} catch (exception: ParseException) {
  exception.report()

  exit(65)
} catch (exception: SyntaxException) {
  exception.report()

  exit(65)
} catch (exception: KoflRuntimeException) {
  exception.report()

  exit(70)
}

fun file(name: String) {
  val file = fopen(name, "r")

  if (file == null) {
    printerr("File do not exists")
    return exit(66)
  }

  TODO("Handle file not implemented")
}

@ThreadLocal
private val interpreter = Interpreter()

// TODO: typechecking
fun eval(code: String): Any? {
  if (code.isEmpty()) return null

  return interpreter.eval(code)
}
