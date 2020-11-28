package com.lorenzoog.kofl.interpreter

import com.lorenzoog.kofl.frontend.ParseException
import com.lorenzoog.kofl.frontend.SyntaxException
import com.lorenzoog.kofl.interpreter.exceptions.KoflRuntimeException
import platform.posix.exit

fun main(args: Array<String>) = try {
  startRepl(
    debug = args.contains("-debug"),
    path = args.getOrNull(args.indexOf("-stdlib") + 1) ?: Platform.stdlibPath
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
