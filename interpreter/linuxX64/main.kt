package com.lorenzoog.kofl.interpreter

import com.lorenzoog.kofl.frontend.ParseException
import com.lorenzoog.kofl.frontend.SyntaxException
import platform.posix.exit
import platform.posix.fopen

fun main(args: Array<String>) = try {
  repl(args.contains("-debug"))
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
