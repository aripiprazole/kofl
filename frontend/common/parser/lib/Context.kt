package com.lorenzoog.kofl.frontend.parser.lib

val EmptyContext = Context(input = "", index = 0)

data class Context(val input: String, val index: Int = 0) {
  val location: Location = Location.Offset(index)
}

fun Context.map(f: (String) -> String): Context {
  return copy(input = f(input), index = index + (input.length - f(input).length))
}