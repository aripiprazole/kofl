package com.lorenzoog.kofl.interpreter.exceptions

import com.lorenzoog.kofl.frontend.KoflException
import com.lorenzoog.kofl.interpreter.backend.Descriptor

sealed class KoflRuntimeException(message: String) : KoflException("runtime", message) {
  class AlreadyDeclaredVar(name: String) : KoflRuntimeException("var $name already declared")
  class UndefinedVar(name: String) :  KoflRuntimeException("var $name does not exists")
  class UndefinedFunction(name: String) :  KoflRuntimeException("function $name does not exists")
  class ReassignImmutableVar(name: String) : KoflRuntimeException("var $name is immutable")
  class MissingReturn(descriptor: Descriptor) : KoflRuntimeException("missing return in descriptor $descriptor")
  class InvalidType(expected: Any, current: Any) : KoflRuntimeException("invalid $current expected $expected")
}