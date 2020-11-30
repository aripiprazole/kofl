package com.lorenzoog.kofl.interpreter.exceptions

import com.lorenzoog.kofl.frontend.KoflException

sealed class KoflCompileTimeException(message: String) : KoflException("compile", message) {
  class UnresolvedVar(name: String) : KoflCompileTimeException("variable $name not found")
  class UnresolvedParameter(index: Int) : KoflCompileTimeException("unresolved parameter $index")
  class InvalidType(expected: Any, current: Any) : KoflCompileTimeException("invalid $current expected $expected")
}