package com.lorenzoog.kofl.frontend

abstract class KoflException(private val type: String, message: String) : RuntimeException(message) {
  open fun report() {
    println("[$type error] $message")
  }
}

// parse errors
open class ParseException(
  token: Token,
  message: String = "invalid token: `$token`"
) : KoflException("parse", "invalid `$token` at ${token.location}: $message")

// syntax errors
open class SyntaxException(message: String) : KoflException("syntax", message)

class LexException(
  line: Int,
  lexeme: String,
  message: String = "unexpected character"
) : SyntaxException("unexpected `$lexeme` in line $line: $message")
