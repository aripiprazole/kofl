package com.lorenzoog.kofl.interpreter

private val consoleSender: ConsoleSender = ErrorHandlerImpl()

fun main(args: Array<String>) {
  try {
    startRepl(
      consoleSender,
      debug = args.contains("-debug"),
      path = args.getOrNull(args.indexOf("-stdlib") + 1) ?: Platform.stdlibPath
    )
  } catch (error: Throwable) {
    consoleSender.handleError(error)
  }
}
