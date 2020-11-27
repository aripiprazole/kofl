package com.lorenzoog.kofl.interpreter

import com.lorenzoog.kofl.frontend.ParseException
import com.lorenzoog.kofl.frontend.SyntaxException
import kotlinx.cinterop.toKString
import platform.posix.exit
import platform.posix.fopen
import platform.posix.getenv

fun main(args: Array<String>) = try {
  repl(
    debug = args.contains("-debug"),
    path = args.getOrNull(args.indexOf("-stdlib") + 1) ?: stdlibPath()
  )
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

internal fun stdlibPath(): String {
  val homePath = getenv("HOME")?.toKString().orEmpty()

  return "$homePath/kofl/stdlib/lib.kofl"
}
