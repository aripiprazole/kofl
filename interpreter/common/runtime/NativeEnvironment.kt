package me.devgabi.kofl.interpreter.runtime

import me.devgabi.kofl.compiler.common.backend.Descriptor
import me.devgabi.kofl.interpreter.exceptions.KoflRuntimeException

typealias KoflNativeCallable = (
  callSite: Descriptor,
  arguments: Map<String, KoflObject>,
  environment: Environment
) -> KoflObject

internal val EMPTY_CONSTRUCTOR: KoflNativeCallable = { _, _, _ ->
  KoflObject.Unit
}

class NativeEnvironment {
  private val functions = mapOf<String, KoflNativeCallable>(
    "println" to { _, arguments, _ ->
      val first = arguments.entries.first()

      println(first.value)

      KoflObject.Unit
    }
  )

  fun call(
    nativeCall: String,
    callSite: Descriptor,
    arguments: Map<String, KoflObject>,
    environment: Environment
  ) {
    val call = functions[nativeCall] ?: throw KoflRuntimeException.UndefinedFunction(
      nativeCall,
      environment
    )

    throw ReturnException(call(callSite, arguments, environment))
  }
}
