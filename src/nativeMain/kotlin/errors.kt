package com.lorenzoog.kofl.interpreter

sealed class KoflError(private val type: String, message: String) : RuntimeException(message) {
  open fun report() {
    printerr("[$type error] $message")
  }
}

open class KoflRuntimeError(message: String) : KoflError("runtime", message)
class TypeError(expected: Any) : KoflRuntimeError("expected type: $expected")
class IllegalOperationError(
  identifier: String,
  operation: String
) : KoflRuntimeError("illegal operation: $operation at $identifier") {
  constructor(token: Token, operation: String) : this(token.lexeme, operation)
}

// parse errors
open class ParseError(
  token: Token,
  message: String = "invalid token: `$token`"
) : KoflError("parse", "invalid `$token` at ${token.location}: $message")

// syntax errors
open class SyntaxError(message: String) : KoflError("syntax error", message)

class LexError(
  line: Int,
  lexeme: String,
  message: String = "unexpected character"
) : SyntaxError("unexpected `$lexeme` in line $line: $message")
