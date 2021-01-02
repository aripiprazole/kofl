package com.lorenzoog.kofl.interpreter.runtime

import com.lorenzoog.kofl.compiler.common.typing.KfType

class Builtin internal constructor(private val environment: Environment) {
  fun setup() {
    environment.createClass(KfType.String) {
      constructor("any" to KfType.Any) { _, arguments, _ ->
        val any by arguments

        any.map { it.toString() }
      }
    }
    environment.createClass(KfType.Int)
    environment.createClass(KfType.Double)
    environment.createSingleton(KfType.Unit)
  }
}