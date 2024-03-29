package me.devgabi.kofl.frontend

enum class TokenType {
  // single-character tokens
  LeftParen, RightParen, LeftBrace,
  RightBrace, Comma, Dot, Minus, Plus,
  Semicolon, Slash, Star, At, Colon, Enter,

  // one-or-double-character tokens
  Bang, BangEqual,
  Equal, EqualEqual,
  Greater, GreaterEqual,
  Less, LessEqual,
  SlashSlash, CommentStart, CommentEnd,

  // comparison tokens
  And, Or,

  // literals tokens
  Identifier, String, Double, Int,

  // keywords tokens
  If, Else, This, False, True, While,
  Func, Val, Var, Return, Record,
  Type, External, Then, Module, Use,

  // special tokens
  Eof
}

data class Token(
  val type: TokenType,
  val lexeme: String,
  val literal: Any? = null,
  val line: Int
) {
  override fun toString() = when {
    literal != null -> "$literal"
    type == TokenType.Identifier -> lexeme
    type == TokenType.Eof -> "EOF"
    else -> "$type"
  }
}

val Token.location: String
  get() = if (type == TokenType.Eof) "EOF" else "line $line"
