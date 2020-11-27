package com.lorenzoog.kofl.interpreter

import com.lorenzoog.kofl.frontend.KoflException
import com.lorenzoog.kofl.frontend.Token

open class KoflRuntimeException(message: String) : KoflException("runtime", message)

class TypeException(got: String, expected: Any? = null) :
  KoflRuntimeException("expected type: $expected but got $got")

class IllegalOperationException(identifier: String, operation: String) :
  KoflRuntimeException("illegal operation: $operation at $identifier") {
  constructor(token: Token, operation: String) : this(token.lexeme, operation)
}

// compile exceptions
open class CompileException(message: String) : KoflException("compile", message)

open class CompileTypeException(message: String) : KoflException("static type", message)

class NameNotFoundException(name: String) :
  CompileTypeException("name $name not found!")

class TypeNotFoundException(name: String) :
  CompileTypeException("type $name not found!")

class InvalidDeclaredTypeException(current: Any, expected: Any) :
  CompileTypeException("excepted $expected but got $current")

class InvalidTypeException(value: Any) :
  CompileTypeException("invalid kofl type in $value")

class MissingReturnException :
  CompileTypeException("missing return function body")
