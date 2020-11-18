sealed class LanguageError(private val type: String, message: String) : RuntimeException(message) {
  open fun report() {
    printerr("[$type error] $message\n")
  }
}

class UnsupportedOpError(op: Token) : LanguageError("unsupported", "unsupported op $op")

class RuntimeError(message: String) : LanguageError("runtime", message)

class TypeError(
  token: Token,
  expected: Any
) : LanguageError("type", "expected type: $expected, at ${token.location}")

class ParseError(
  token: Token,
  message: String = "invalid token: $token"
) : LanguageError("parse", "invalid `${token.lexeme}` at ${token.location}: $message")

class SyntaxError(
  line: Int,
  message: String = "unexpected character at line $line"
) : LanguageError("syntax", message)

class LexError(
  line: Int,
  lexeme: String,
  message: String = "unexpected character"
) : LanguageError("lex", "unexpected `$lexeme` in line $line: $message")
