package me.devgabi.kofl.interpreter.exceptions

import me.devgabi.kofl.compiler.common.backend.Descriptor
import me.devgabi.kofl.frontend.KoflException
import me.devgabi.kofl.interpreter.dump
import me.devgabi.kofl.interpreter.runtime.Environment

sealed class KoflRuntimeException(
  message: String,
  val environment: Environment
) : KoflException("runtime", message) {
  class AlreadyDeclaredVar(name: String, callSite: Environment) :
    KoflRuntimeException("var $name already declared", callSite)

  class UndefinedVar(name: String, callSite: Environment) :
    KoflRuntimeException("var $name does not exists", callSite)

  class UndefinedFunction(name: String, callSite: Environment) :
    KoflRuntimeException("function $name does not exists", callSite)

  class ReassignImmutableVar(name: String, callSite: Environment) :
    KoflRuntimeException("var $name is immutable", callSite)

  class MissingReturn(descriptor: Descriptor, callSite: Environment) :
    KoflRuntimeException("missing return in descriptor ${descriptor.dump()}", callSite)

  class InvalidType(expected: Any, current: Any, callSite: Environment) :
    KoflRuntimeException("invalid $current expected $expected", callSite)
}
