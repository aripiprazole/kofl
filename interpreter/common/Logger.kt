package com.lorenzoog.kofl.interpreter

import com.lorenzoog.kofl.compiler.common.KoflCompileException
import com.lorenzoog.kofl.frontend.KoflException
import com.lorenzoog.kofl.interpreter.exceptions.KoflRuntimeException
import com.lorenzoog.kofl.interpreter.runtime.Environment

private const val ERROR_COLOR = "\u001b[31m"
private const val WARN_COLOR = "\u001b[33m"
private const val TRACE_COLOR = "\u001b[34m"
private const val RESET_COLOR = "\u001b[37m"

interface Logger {
  fun println(message: Any = "")
  fun print(message: Any)

  fun trace(message: Any)

  fun reportCompileError(error: KoflCompileException)
  fun reportRuntimeError(error: KoflRuntimeException)
  fun reportError(error: KoflException)
  fun reportNativeError(error: Throwable)

  fun handleError(error: Throwable) {
    when (error) {
      is KoflCompileException -> reportCompileError(error)
      is KoflRuntimeException -> reportRuntimeError(error)
      is KoflException -> reportError(error)
      else -> reportNativeError(error)
    }
  }
}

class ReplLogger : Logger {
  override fun print(message: Any) {
    kotlin.io.print(RESET_COLOR + message)
  }

  override fun trace(message: Any) {
    kotlin.io.println(TRACE_COLOR + message)
  }

  override fun println(message: Any) {
    kotlin.io.println(RESET_COLOR + message)
  }

  override fun reportCompileError(error: KoflCompileException) {
    println(ERROR_COLOR + "[compile error] ${error.message}")
  }

  override fun reportRuntimeError(error: KoflRuntimeException) {
    println(WARN_COLOR + "[runtime error] ${error.message}")
    error.environment.stackTrace().forEach { location ->
      println("$WARN_COLOR  $location")
    }
  }

  override fun reportError(error: KoflException) {
    println(ERROR_COLOR + "[unexpected error] ${error.message}")
  }

  override fun reportNativeError(error: Throwable) {
    println(ERROR_COLOR + "[kotlin error] ${error.cause}: ${error.message}")
    println(ERROR_COLOR + error.stackTraceToString())
  }
}

@OptIn(ExperimentalStdlibApi::class)
fun Environment.stackTrace(): List<String> = buildList {
  var environment = this@stackTrace.also { (callSite) ->
    add("at ${callSite.dump()}.")
  }

  while (environment.enclosing != null) {
    environment = (environment.enclosing ?: continue).also { (callSite) ->
      add("at ${callSite.dump()}.")
    }
  }
}
