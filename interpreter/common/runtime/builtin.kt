package com.lorenzoog.kofl.interpreter.runtime

import com.lorenzoog.kofl.interpreter.backend.Environment
import com.lorenzoog.kofl.interpreter.typing.KoflType

class Builtin internal constructor(environment: Environment) {
  val String = environment.createClass(KoflType.String) {
    constructor(mapOf("any" to KoflType.Any)) { _, arguments, _ ->
      val any by arguments

      any.map { it.toString() }
    }
  }
  val Int = environment.createClass(KoflType.Int)
  val Double = environment.createClass(KoflType.Double)
  val Unit = environment.createSingleton(KoflType.Unit)
}