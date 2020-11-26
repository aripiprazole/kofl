package com.lorenzoog.kofl.interpreter

import com.lorenzoog.kofl.frontend.KoflException
import com.lorenzoog.kofl.frontend.Token

open class KoflRuntimeException(message: String) : KoflException("runtime", message)
class TypeException(got: String, expected: Any? = null) : KoflRuntimeException("expected type: $expected but got $got")
class IllegalOperationException(
  identifier: String,
  operation: String
) : KoflRuntimeException("illegal operation: $operation at $identifier") {
  constructor(token: Token, operation: String) : this(token.lexeme, operation)
}

