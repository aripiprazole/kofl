package com.lorenzoog.kofl.frontend

import com.lorenzoog.kofl.frontend.parser.lib.Location

abstract class KoflException(private val type: String, message: String) : RuntimeException(message) {
  open fun report() {
    println("[$type error] $message")
  }
}

// parse errors
class ParseException(location: Location, expected: String, actual: String) : KoflException("parse", "$location: $expected but got $actual")

open class ParseExceptionOld(
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
