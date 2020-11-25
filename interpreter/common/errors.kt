package com.lorenzoog.kofl.interpreter

import com.lorenzoog.kofl.frontend.KoflError
import com.lorenzoog.kofl.frontend.Token

open class KoflRuntimeError(message: String) : KoflError("runtime", message)
class TypeError(expected: Any) : KoflRuntimeError("expected type: $expected")
class IllegalOperationError(
  identifier: String,
  operation: String
) : KoflRuntimeError("illegal operation: $operation at $identifier") {
  constructor(token: Token, operation: String) : this(token.lexeme, operation)
}

