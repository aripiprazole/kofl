package me.devgabi.kofl.compiler.common

import me.devgabi.kofl.compiler.common.typing.KfType
import me.devgabi.kofl.frontend.KoflException

sealed class KoflCompileException(message: String) : KoflException("compile", message) {
  class UnresolvedVar(name: String) : KoflCompileException("variable $name not found")
  class UnresolvedFunction(name: String) : KoflCompileException("function $name not found")
  class UnresolvedParameter(index: Int) : KoflCompileException("unresolved parameter $index")
  class MissingReturn : KoflCompileException("missing return function body")
  class AlreadyResolvedVar(name: String) : KoflCompileException("already resolved var $name")
  class InvalidType(value: Any) : KoflCompileException("invalid kofl type in $value")
  class UnexpectedType(current: Any, expected: Any) : KoflCompileException(
    "excepted $expected but got $current"
  )

  class ClassMissingName(definition: KfType.Class) : KoflCompileException(
    "$definition missing name"
  )
}
