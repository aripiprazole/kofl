package com.lorenzoog.kofl.interpreter.runtime

import com.lorenzoog.kofl.interpreter.backend.Environment
import com.lorenzoog.kofl.interpreter.typing.KoflType

class Builtin internal constructor(environment: Environment) {
  val string = environment.createClass(KoflType.String) {
    constructor(mapOf("any" to KoflType.Any)) { _, arguments, _ ->
      val any by arguments

      any.map { it.toString() }
    }
  }
  val int = environment.createClass(KoflType.Int)
  val double = environment.createClass(KoflType.Double)
  val unit = environment.createSingleton(KoflType.Unit)
}