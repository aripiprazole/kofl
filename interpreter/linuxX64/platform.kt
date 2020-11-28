package com.lorenzoog.kofl.interpreter

import kotlinx.cinterop.toKString
import platform.posix.getenv

actual object Platform {
  actual val stdlibPath: String
    get() {
      val homePath = getenv("HOME")?.toKString().orEmpty()

      return "$homePath/kofl/stdlib/lib.kofl"
    }
}