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
open class SyntaxError(message: String) : KoflError("syntax", message)

class LexError(
  line: Int,
  lexeme: String,
  message: String = "unexpected character"
) : SyntaxError("unexpected `$lexeme` in line $line: $message")

open class CompileException(message: String) : KoflError("type", message)

class TypeNotFoundException(name: String) : CompileException("type $name not found!")
class InvalidDeclaredTypeException(current: String, expected: String) :
  CompileException("excepted $expected but got $current")

class InvalidTypeException(value: Any) : CompileException("invalid kofl type in $value")
class MissingReturnException : CompileException("missing return function body")
