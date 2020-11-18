sealed class LanguageError(val type: String, message: String) : RuntimeException(message) {
  open fun report() {
    printout("\n")
    printerr("[$type] $message\n")
  }
}

class ParseError(
  token: Token, message: String = "invalid token: $token"
) : LanguageError("parse error", "invalid `${token.lexeme}` at ${token.location}: $message")

class SyntaxError(
  val line: Int, message: String = "Unexpected character"
) : LanguageError("syntax error", message)

class LexError(
  line: Int, lexeme: String,
  message: String = "unexpected character"
) : LanguageError("lex error", "unexpected `$lexeme` in line $line: $message")
