package com.lorenzoog.kofl.interpreter.exceptions

import com.lorenzoog.kofl.frontend.KoflException

sealed class KoflCompileTimeException(message: String) : KoflException("compile", message) {
  class UnresolvedVar(name: String) : KoflCompileTimeException("variable $name not found")
  class UnresolvedParameter(index: Int) : KoflCompileTimeException("unresolved parameter $index")
  class MissingReturn : KoflCompileTimeException("missing return function body")

  sealed class Type(message: String) : KoflException("static type", message) {
    class NameNotFound(name: String) : Type("name $name not found!")
    class TypeNotFound(name: String) : Type("type $name not found!")
    class InvalidType(value: Any) : Type("invalid kofl type in $value")
    class AlreadyResolvedVar(name: String) : Type("already resolved var $name")
    class InvalidDeclaredType(current: Any, expected: Any) : Type("excepted $expected but got $current")
  }
}
