package com.lorenzoog.kofl.frontend

abstract class KoflError(private val type: String, message: String) : RuntimeException(message) {
  open fun report() {
    println("[$type error] $message")
  }
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
