val RESERVED_KEYWORDS = mapOf(
  "struct" to TokenType.Struct,
  "func" to TokenType.Func,
  "this" to TokenType.This,
  "if" to TokenType.If,
  "else" to TokenType.Else,
  "false" to TokenType.False,
  "true" to TokenType.True,
  "val" to TokenType.Val,
  "return" to TokenType.Return,
  "extern" to TokenType.Extern,
)

enum class TokenType {
  // single-character tokens
  LeftParen, RightParen, LeftBrace,
  RightBrace, Comma, Dot, Minus, Plus,
  Semicolon, Slash, Star, At, Colon,

  // one-or-double-character tokens
  Bang, BangEqual,
  Equal, EqualEqual,
  Greater, GreaterEqual,
  Less, LessEqual,

  // comparison tokens
  And, Or,

  // literals tokens
  Identifier, String, Number,

  // keywords tokens
  If, Else, This, False, True,
  Func, Val, Return, Struct, Extern,

  // eof
  Eof
}

data class Token(
  val type: TokenType,
  val lexeme: String,
  val literal: Any?,
  val line: Int
) {
  override fun toString() = when {
    literal != null -> "($type `$literal`)"
    type == TokenType.Eof -> "EOF"
    else -> "$type"
  }
}

val Token.location: String
  get() = if (type == TokenType.Eof) "end" else "line $line"
