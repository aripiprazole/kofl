package com.lorenzoog.kofl.interpreter

import com.lorenzoog.kofl.compiler.common.KoflCompileException
import com.lorenzoog.kofl.frontend.KoflException
import com.lorenzoog.kofl.frontend.KoflParseException
import com.lorenzoog.kofl.frontend.escape
import com.lorenzoog.kofl.interpreter.exceptions.KoflRuntimeException
import com.lorenzoog.kofl.interpreter.module.MainNotFoundException
import com.lorenzoog.kofl.interpreter.module.MainReturnedNotInt
import com.lorenzoog.kofl.interpreter.runtime.Environment

private const val ERROR_COLOR = "\u001b[31m"
private const val WARN_COLOR = "\u001b[33m"
private const val TRACE_COLOR = "\u001b[34m"
private const val RESET_COLOR = "\u001b[37m"

interface Logger {
  fun println(message: Any = "")
  fun print(message: Any)

  fun trace(message: Any)

  fun reportParseError(error: KoflParseException)
  fun reportCompileError(error: KoflCompileException)
  fun reportRuntimeError(error: KoflRuntimeException)
  fun reportError(error: KoflException)
  fun reportNativeError(error: Throwable)

  fun handleError(error: Throwable) {
    when (error) {
      is KoflCompileException -> reportCompileError(error)
      is KoflRuntimeException -> reportRuntimeError(error)
      is KoflParseException -> reportParseError(error)
      is KoflException -> reportError(error)
      else -> reportNativeError(error)
    }
  }
}

class ReplLogger(private val debug: Boolean) : Logger {
  override fun print(message: Any) {
    kotlin.io.print(RESET_COLOR + message)
  }

  override fun trace(message: Any) {
    kotlin.io.println(TRACE_COLOR + message)
  }

  override fun println(message: Any) {
    kotlin.io.println(RESET_COLOR + message)
  }

  override fun reportParseError(error: KoflParseException) {
    println(ERROR_COLOR + "[parse error] ${error.message.escape()}")

    if (debug) reportNativeError(error)
  }

  override fun reportCompileError(error: KoflCompileException) {
    println(ERROR_COLOR + "[compile error] ${error.message.escape()}")

    if (debug) reportNativeError(error)
  }

  override fun reportRuntimeError(error: KoflRuntimeException) {
    println(WARN_COLOR + "[runtime error] ${error.message.escape()}")
    error.environment.callTree().forEach { location ->
      println("$WARN_COLOR  $location")
    }
  }

  override fun reportError(error: KoflException) {
    println(ERROR_COLOR + "[unexpected error] ${error.message.escape()}")

    if (debug) reportNativeError(error)
  }

  override fun reportNativeError(error: Throwable) {
    println(ERROR_COLOR + "[kotlin error] ${error.cause}: ${error.message.escape()}")

    if (error is MainNotFoundException || error is MainReturnedNotInt) {
      if (debug) {
        println(ERROR_COLOR + error.stackTraceToString())
      }

      Platform.exit(1)
    } else {
      println(ERROR_COLOR + error.stackTraceToString())
    }
  }
}

fun Environment.callTree(): List<String> = mutableListOf<String>().also { stack ->
  var environment = this@callTree.also { (callSite) ->
    stack.add("at ${callSite.dump()}.")
  }

  while (environment.enclosing != null) {
    environment = (environment.enclosing ?: continue).also { (callSite) ->
      stack.add("at ${callSite.dump()}.")
    }
  }
}
