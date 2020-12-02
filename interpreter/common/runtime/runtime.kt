package com.lorenzoog.kofl.interpreter.runtime

import com.lorenzoog.kofl.interpreter.backend.Descriptor
import com.lorenzoog.kofl.interpreter.backend.Environment
import com.lorenzoog.kofl.interpreter.backend.KoflObject
import com.lorenzoog.kofl.interpreter.backend.ReturnException
import com.lorenzoog.kofl.interpreter.exceptions.KoflRuntimeException

typealias KoflNativeCallable = (callSite: Descriptor, arguments: Map<String, KoflObject>, environment: Environment) -> KoflObject

class NativeEnvironment {
  private val functions = mapOf<String, KoflNativeCallable>(
    "println" to { _, arguments, _ ->
      println(arguments.entries.first().value.unwrap())

      null!! // TODO fix me
    }
  )

  fun call(nativeCall: String, callSite: Descriptor, arguments: Map<String, KoflObject>, environment: Environment) {
    val call = functions[nativeCall] ?: throw KoflRuntimeException.UndefinedFunction(nativeCall, environment)

    throw ReturnException(call(callSite, arguments, environment))
  }
}
