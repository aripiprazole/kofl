package com.lorenzoog.kofl.interpreter

import kotlin.system.exitProcess

actual object Platform {
  actual val stdlibPath: String
    get() {
      val homePath = System.getenv("HOME").orEmpty()

      return "$homePath/kofl/stdlib/lib.kofl"
    }
}

actual fun exit(code: Int) {
  exitProcess(code)
}