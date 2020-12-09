package com.lorenzoog.kofl.interpreter

import kotlinx.cinterop.cstr
import kotlinx.cinterop.toKString
import sample.globalstate.doSomething

private val consoleSender: ConsoleSender = ErrorHandlerImpl()

fun main(args: Array<String>) {
  println(doSomething("Hello, kotlin native interop".cstr)?.toKString())

  try {
    startRepl(consoleSender,
      debug = args.contains("-debug"),
      path = args.getOrNull(args.indexOf("-stdlib") + 1) ?: Platform.stdlibPath
    )
  } catch (error: Throwable) {
    consoleSender.handleError(error)
  }
}
