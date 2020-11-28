package com.lorenzoog.kofl.interpreter.exceptions

import com.lorenzoog.kofl.frontend.KoflException

sealed class KoflCompileTimeException(message: String) : KoflException("compile", message) {
  class UnresolvedVar(name: String) : KoflCompileTimeException("variable $name not found")
}