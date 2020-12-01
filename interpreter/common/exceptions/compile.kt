package com.lorenzoog.kofl.interpreter.exceptions

import com.lorenzoog.kofl.frontend.KoflException

sealed class KoflCompileTimeException(message: String) : KoflException("compile", message) {
  class UnresolvedVar(name: String) : KoflCompileTimeException("variable $name not found")
  class UnresolvedParameter(index: Int) : KoflCompileTimeException("unresolved parameter $index")
  class MissingReturn : KoflCompileTimeException("missing return function body")
  class AlreadyResolvedVar(name: String) : KoflCompileTimeException("already resolved var $name")
  class InvalidType(value: Any) : KoflCompileTimeException("invalid kofl type in $value")
  class UnexpectedType(current: Any, expected: Any) : KoflCompileTimeException("excepted $expected but got $current")
}
