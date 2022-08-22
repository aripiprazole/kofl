package me.devgabi.kofl.interpreter

expect object Platform {
  val stdlibPath: String

  fun exit(code: Int)
}
