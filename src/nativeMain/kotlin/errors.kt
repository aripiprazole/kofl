sealed class KoflError(private val type: String, message: String) : RuntimeException(message) {
  open fun report() {
    printerr("[$type error] $message")
  }
}

open class RuntimeError(message: String) : KoflError("runtime", message)

// parse errors
open class ParseError(
  token: Token,
  message: String = "invalid token: `$token`"
) : KoflError("parse", "invalid `$token` at ${token.location}: $message")

class TypeError(
  expected: Any
) : KoflError("type error", "expected type: $expected")

// syntax errors
open class SyntaxError(
  type: String,
  message: String
) : KoflError("(syntax) $type", message)

class UnsolvedReferenceError(
  identifier: Token
) : SyntaxError("unsolved reference", "Unsolved reference to $identifier")

class IllegalOperationError(
  identifier: Token,
  operation: String
) : SyntaxError("illegal access", "Trying to do illegal: $operation at ${identifier.location}")

class LexError(
  line: Int,
  lexeme: String,
  message: String = "unexpected character"
) : SyntaxError("lex", "unexpected `$lexeme` in line $line: $message")

class UnsupportedOpError(
  op: Token,
  whenTrying: String? = null
) : SyntaxError("unsupported", "unsupported op when trying to do: $op ${whenTrying?.let { "" }}")
