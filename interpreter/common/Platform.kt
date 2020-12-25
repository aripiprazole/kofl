package com.lorenzoog.kofl.interpreter

expect object Platform {
  val stdlibPath: String

  fun exit(code: Int)
}
