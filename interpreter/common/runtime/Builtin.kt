package com.lorenzoog.kofl.interpreter.runtime

import com.lorenzoog.kofl.compiler.common.typing.KoflType

class Builtin internal constructor(private val environment: Environment) {
  fun setup() {
    environment.createClass(KoflType.String) {
      constructor("any" to KoflType.Any) { _, arguments, _ ->
        val any by arguments

        any.map { it.toString() }
      }
    }
    environment.createClass(KoflType.Int)
    environment.createClass(KoflType.Double)
    environment.createSingleton(KoflType.Unit)
  }
}