package com.lorenzoog.kofl.interpreter

actual object Platform {
  actual val stdlibPath: String
    get() {
      val homePath = System.getenv("HOME").orEmpty()

      return "$homePath/kofl/stdlib/lib.kofl"
    }
}
